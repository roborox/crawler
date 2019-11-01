package ru.roborox.crawler.common;

public interface PostProcessor<T> {
    void postprocess(T bean);
}
