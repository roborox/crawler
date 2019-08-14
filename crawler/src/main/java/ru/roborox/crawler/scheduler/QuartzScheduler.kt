package ru.roborox.crawler.scheduler

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.roborox.crawler.LoaderTask
import ru.roborox.crawler.TaskScheduler

@Service
class QuartzScheduler : TaskScheduler {
    override fun submit(task: LoaderTask<*, *>): Mono<Void> {
        return Mono.empty()
    }
}