package ru.roborox.crawler

import reactor.core.publisher.Mono
import ru.roborox.crawler.domain.LoaderTask
import ru.roborox.crawler.domain.Page
import kotlin.reflect.KClass

interface Loader {
    fun load(page: Page): Mono<LoadResult>
}

interface TaskScheduler {
    fun submit(task: LoaderTask): Mono<Void>
}

fun Class<out Loader>.newTask(taskId: String): LoaderTask {
    return LoaderTask(taskId, this.name)
}

fun KClass<out Loader>.newTask(taskId: String): LoaderTask {
    return this.java.newTask(taskId)
}

fun Loader.newTask(taskId: String): LoaderTask {
    return this.javaClass.newTask(taskId)
}