package ru.roborox.crawler.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JacksonProvider {
    ObjectMapper create();
}
