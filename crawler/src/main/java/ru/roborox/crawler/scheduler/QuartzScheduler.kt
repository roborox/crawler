package ru.roborox.crawler.scheduler

import org.quartz.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.roborox.crawler.Crawler
import ru.roborox.crawler.Loader
import ru.roborox.crawler.TaskScheduler
import ru.roborox.crawler.anotation.AnnotatedBeanFinder
import ru.roborox.crawler.domain.LoaderTask
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.ReloadType
import ru.roborox.crawler.domain.Status
import ru.roborox.crawler.persist.PageRepository
import java.util.*
import java.util.concurrent.Callable

@Service
class QuartzScheduler(
    annotatedBeanFinder: AnnotatedBeanFinder,
    private val scheduler: Scheduler,
    private val pageRepository: PageRepository
) : TaskScheduler, PageLoaderAware(annotatedBeanFinder) {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        super.onApplicationEvent(event)
        for (clazz in getAllLoaders()) {
            schedule(clazz)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun submit(task: LoaderTask): Mono<Void> {
        val schedule = Callable { schedule(Class.forName(task.loaderClass) as Class<out Loader>) }.blockingToMono().then()
        return pageRepository.findByLoaderClassAndTaskId(task.loaderClass, task.taskId)
            .map { if (it.status == Status.NEW) it.copy(status = Status.SCHEDULED, nextStartDate = Date()) else it }
            .flatMap { pageRepository.save(it) }
            .then(schedule)
    }

    fun reschedule(clazz: Class<out Loader>, page: Page) {
        val config = getLoaderConfig(clazz)
        val reloadEvery = config.reloadEvery.toMillis(config.countReload)
        if (config.reloadType != ReloadType.NEVER && reloadEvery != 0L) {
            pageRepository.save(page.copy(nextStartDate = Date(Date().time + reloadEvery))).block()
        } else {
            pageRepository.save(page.copy(nextStartDate = null)).block()
        }
        schedule(clazz, true)
    }

    fun schedule(clazz: Class<out Loader>, forceSchedule: Boolean = false) {
        MDC.put("loaderClass", clazz.name)
        try {
            synchronized(clazz) {
                scheduleInternal(clazz, forceSchedule)
            }
        } finally {
            MDC.remove("loaderClass")
            MDC.remove("taskId")
        }
    }

    private fun scheduleInternal(clazz: Class<out Loader>, forceSchedule: Boolean) {
        val key = TriggerKey(clazz.name)
        logger.info("schedule $clazz $key")
        if (forceSchedule || scheduler.getTrigger(key) == null) {
            logger.info("creating trigger force: $forceSchedule")
            val config = getLoaderConfig(clazz)
            val minRate = config.minRateTimeUnit.toMillis(config.minRate)
            val startDate = Date(Date().time + minRate)
            val page = pageRepository
                .findTopByLoaderClassAndNextStartDateIsNotNullOrderByNextStartDateAsc(clazz.name)
                .block()
            logger.info("found page: $page")
            if (page != null) {
                MDC.put("taskId", page.taskId)
                val nextDate = maxOf(page.nextStartDate!!, startDate)
                logger.info("Scheduling trigger $key on $nextDate")
                val trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(key)
                    .forJob(initJob(clazz))
                    .usingJobData("taskId", page.taskId)
                    .usingJobData("className", clazz.name)
                    .startAt(nextDate)
                    .build()
                val result = scheduler.rescheduleJob(key, trigger)
                logger.info("Scheduling result is $result")
                if (result == null) {
                    logger.info("Creating new trigger $key")
                    scheduler.scheduleJob(trigger)
                }
            } else {
                logger.info("not found page to schedule")
            }
        } else {
            logger.info("trigger exists $key")
        }
    }

    private fun initJob(clazz: Class<out Loader>): JobKey {
        val key = JobKey(clazz.name)
        if (scheduler.getJobDetail(key) == null) {
            logger.info("creating new job for ${clazz.name}")
            val job = JobBuilder.newJob(QuartzJob::class.java)
                .withIdentity(key)
                .storeDurably()
                .build()
            scheduler.addJob(job, false)
            logger.info("success for adding job for ${clazz.name}")
        }
        return key
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(QuartzScheduler::class.java)
    }
}

class QuartzJob(
    private var applicationContext: ApplicationContext,
    private val crawler: Crawler,
    private val quartzScheduler: QuartzScheduler,
    private val pageRepository: PageRepository
) : Job {
    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var className: String

    @Suppress("UNCHECKED_CAST")
    fun getClassName(): Class<out Loader> {
        return Class.forName(className) as Class<out Loader>
    }

    override fun execute(p0: JobExecutionContext?) {
        MDC.put("loaderClass", className)
        try {
            logger.info("start $className")
            val clazz = getClassName()
            val bean = applicationContext.getBean(clazz)
            val page = pageRepository.findTopByLoaderClassAndNextStartDateIsNotNullOrderByNextStartDateAsc(className).block()
            logger.info("starting crawl for page: $page")
            if (page != null) {
                MDC.put("taskId", page.taskId)
                quartzScheduler.reschedule(clazz, page)
                crawler.crawl(page.parent, page.taskId, bean).subscribe(
                    {},
                    { logger.error("got exception while executing crawl", it) }
                )
            }
        } finally {
            MDC.remove("loaderClass")
            MDC.remove("taskId")
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(QuartzJob::class.java)
    }
}
