package ru.roborox.crawler.domain

import java.util.concurrent.TimeUnit

data class LoaderConfig(
    val minRate: Long,
    val minRateTimeUnit: TimeUnit,
    val reloadType: ReloadType,
    val countReload: Long,
    val reloadEvery: TimeUnit
    )