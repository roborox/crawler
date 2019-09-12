package ru.roborox.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import ru.roborox.crawler.quartz.QuartzConfiguration;

@ContextConfiguration(classes = {QuartzConfiguration.class, MockConfiguration.class})
public class AbstractIntegrationTest extends ru.roborox.crawler.test.AbstractIntegrationTest {

    @Autowired
    ReactiveMongoOperations mongo;

    @BeforeClass
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @BeforeMethod
    @Override
    public void before() throws Exception {
        super.before();
    }

    @AfterMethod
    @Override
    public void after() throws Exception {
        super.after();
    }
}
