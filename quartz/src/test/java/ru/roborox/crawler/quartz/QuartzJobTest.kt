package ru.roborox.crawler.quartz

import org.apache.commons.lang3.RandomStringUtils
import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.testng.Assert
import org.testng.annotations.Test
import ru.roborox.crawler.quartz.config.DataStore
import ru.roborox.crawler.test.wait.Wait.waitAssert

@Test
class QuartzJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var scheduler: Scheduler
    @Autowired
    private lateinit var dataStore: DataStore

    fun scheduleAndRun() {
        val testData = RandomStringUtils.randomAlphabetic(10)

        scheduler.scheduleJob(
            JobBuilder.newJob(DataJob::class.java).usingJobData("value", testData).build(),
            TriggerBuilder.newTrigger().startNow().build()
        )

        waitAssert {
            Assert.assertEquals(dataStore.value, testData)
        }
    }
}

class DataJob(
    private val dataStore: DataStore
) : Job {
    var value: String? = null

    override fun execute(context: JobExecutionContext?) {
        dataStore.value = value
    }
}