package ru.roborox.crawler.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.CorsRegistration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfiguration implements WebFluxConfigurer {
    public static final Logger logger = LoggerFactory.getLogger(WebConfiguration.class);

    @Autowired(required = false)
    private List<ExposedHeadersProvider> providers;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired(required = false)
    private AbstractJackson2Decoder jackson2Decoder;
    @Autowired(required = false)
    private AbstractJackson2Encoder jackson2Encoder;

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        if (jackson2Decoder == null) {
            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
        } else {
            configurer.defaultCodecs().jackson2JsonDecoder(jackson2Decoder);
        }
        if (jackson2Encoder == null) {
            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
        } else {
            configurer.defaultCodecs().jackson2JsonEncoder(jackson2Encoder);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration registration = registry.addMapping("/**")
            .allowCredentials(true)
            .allowedHeaders("*")
            .allowedMethods("*")
            .allowedOrigins("*");
        if (providers != null) {
            String[] headers = providers.stream().flatMap(p -> p.getExposedHeaders().stream()).toArray(String[]::new);
            logger.info("registering exposedHeaders: {} from providers: {}", Arrays.toString(headers), providers);
            registration.exposedHeaders(headers);
        }
    }
}
