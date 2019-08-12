package ru.roborox.crawler.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;
import ru.roborox.crawler.test.persist.AsyncMongoHelper;
import ru.roborox.crawler.test.ports.Ports;

import java.util.List;

@Test(groups = "integration")
@ContextConfiguration(classes = AsyncMongoHelper.class)
public class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {
    protected final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    protected static List<Integer> ports = Ports.nextPorts(3);

    @Autowired
    protected AsyncMongoHelper mongoHelper;

    protected String baseUrl;

    @Override
    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextBeforeTestClass")
    protected void springTestContextPrepareTestInstance() throws Exception {
        logger.info("using httpPort: " + ports.get(0));
        System.setProperty("httpPort", "" + ports.get(0));
        logger.info("using jmsPort: " + ports.get(1));
        System.setProperty("jms-brokerUrls", "127.0.0.1:" + ports.get(1));
        super.springTestContextPrepareTestInstance();
    }

    @BeforeClass
    public void setUp() throws Exception {
        baseUrl = "http://127.0.0.1:" + ports.get(0);
        mongoHelper.cleanup().then().block();
    }

    @AfterClass
    public void tearDown() throws Exception {
    }

    @BeforeMethod
    public void before() throws Exception {
    }

    @AfterMethod
    public void after() throws Exception {
    }
}
