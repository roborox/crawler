package ru.roborox.crawler.scheduler

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import ru.roborox.crawler.Loader
import ru.roborox.crawler.anotation.AnnotatedBeanFinder
import ru.roborox.crawler.anotation.PageLoader
import ru.roborox.crawler.domain.LoaderConfig
import ru.roborox.crawler.domain.ReloadType
import java.util.*
import java.util.concurrent.TimeUnit

abstract class PageLoaderAware(
    private val annotatedBeanFinder: AnnotatedBeanFinder
    ) : ApplicationListener<ContextRefreshedEvent> {

    private val mapTasks = HashMap<Class<out Loader>, LoaderConfig>()

    @Suppress("UNCHECKED_CAST")
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val beans = annotatedBeanFinder.find<Loader>(PageLoader::class.java)
        beans.forEach {
            val minRateMap = it.second["minRate"] as HashMap<String, Any>
            val reloadMap = it.second["reload"] as HashMap<String, Any>
            val rateMap = reloadMap["rate"] as HashMap<String, Any>
            val config = LoaderConfig(
                minRate = minRateMap["value"] as Long,
                minRateTimeUnit = minRateMap["unit"] as TimeUnit,
                reloadType = reloadMap["value"] as ReloadType,
                countReload = rateMap["value"] as Long,
                reloadEvery = rateMap["unit"] as TimeUnit
            )
            mapTasks[it.first.javaClass] = config
        }
    }

    fun getLoaderConfig(clazz: Class<out Loader>): LoaderConfig {
        return mapTasks[clazz] ?: LoaderConfig(0, TimeUnit.MILLISECONDS, ReloadType.NEVER, 0, TimeUnit.MILLISECONDS)
    }

    fun getAllLoaders(): Collection<Class<out Loader>> {
        return mapTasks.keys
    }
}