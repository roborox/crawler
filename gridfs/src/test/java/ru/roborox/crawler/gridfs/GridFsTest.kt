package ru.roborox.crawler.gridfs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.testng.Assert.assertEquals
import ru.roborox.crawler.gridfs.dao.FileDao
import java.io.ByteArrayInputStream

class GridFsTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var fileDao: FileDao

    fun saveFile() {
        val data = DataBufferUtils.readInputStream(
            { ByteArrayInputStream("test".toByteArray()) },
            DefaultDataBufferFactory(),
            10000
        )
        val got = fileDao.saveFile("test", "text/plain", data).block()!!

        val read = fileDao.getFile(got).block()!!
        assertEquals(read.length, 4)
    }
}