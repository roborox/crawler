package ru.roborox.crawler.anotation;

import kotlin.Pair;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AnnotatedBeanFinder {

    @Autowired
    private ConfigurableListableBeanFactory factory;

    @SuppressWarnings("unchecked")
    public <B> List<Pair<B, Map<String, Object>>> find(Class<? extends Annotation> annotationClass) {
        final List<Pair<B, Map<String, Object>>> beans = new ArrayList<>();
        Map<String, Object> beanNames = factory.getBeansWithAnnotation(annotationClass);
        for (String beanName: beanNames.keySet()) {
            final BeanDefinition bd = factory.getBeanDefinition(beanName);
            final Map<String, Object> attributes = ((AnnotatedBeanDefinition) bd).getMetadata().getAnnotationAttributes(annotationClass.getName());
            final B bean = (B) factory.getBean(beanName);
            beans.add(new Pair(bean, attributes));
        }
        return beans;
    }
}
