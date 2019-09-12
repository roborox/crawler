package ru.roborox.crawler

import org.springframework.context.annotation.ComponentScan

@EnableRoboroxCrawler
@ComponentScan(basePackageClasses = [MockConfiguration::class])
class MockConfiguration