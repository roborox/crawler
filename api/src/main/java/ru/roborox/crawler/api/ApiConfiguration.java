package ru.roborox.crawler.api;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.roborox.crawler.common.ObjectMapperUtils;
import ru.roborox.crawler.common.PostProcessor;

import java.util.Collections;
import java.util.List;

@EnableWebFlux
@Configuration
@Import({
    NettyConfiguration.class,
    WebConfiguration.class
})

public class ApiConfiguration {
    @Autowired(required = false)
    private JacksonProvider jacksonProvider;
    @Autowired(required = false)
    private List<PostProcessor<SimpleModule>> modulePostprocessors;

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper;
        if (jacksonProvider != null) {
            mapper = jacksonProvider.create();
        } else {
            mapper = objectMapperDefault();
        }
        SimpleModule module = new SimpleModule("CustomModels", new Version(1, 0, 0, "", "", ""));
        for (PostProcessor<SimpleModule> postprocessor : (modulePostprocessors != null ? modulePostprocessors : Collections.<PostProcessor<SimpleModule>>emptyList())) {
            postprocessor.postprocess(module);
        }
        mapper.registerModule(module);
        return mapper;
    }

    public static ObjectMapper objectMapperDefault() {
        return ObjectMapperUtils.defaultObjectMapper();
    }
}
