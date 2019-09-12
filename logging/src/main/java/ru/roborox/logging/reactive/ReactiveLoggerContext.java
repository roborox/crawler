package ru.roborox.logging.reactive;

import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ReactiveLoggerContext {
    public static final String LOGGER_CONTEXT = "loggerContext";

    private static final Logger logger = LoggerFactory.getLogger(ReactiveLoggerContext.class);

    private final List<Pair<String, String>> values;

    public ReactiveLoggerContext(List<Pair<String, String>> values) {
        this.values = Collections.unmodifiableList(values);
    }

    public List<Pair<String, String>> getValues() {
        return values;
    }

    public ReactiveLoggerContext add(String key, String value) {
        Map<String, String> asMap = values.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        asMap.put(key, value);
        return new ReactiveLoggerContext(asMap.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).collect(toList()));
    }

    public ReactiveLoggerContext add(Map<String, String> map) {
        Map<String, String> asMap = values.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        asMap.putAll(map);
        return new ReactiveLoggerContext(asMap.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).collect(toList()));
    }

    public static Mono<Void> withContext(Runnable action) {
        return loggerContext()
            .flatMap(context -> {
                logWithContext(context.values, action);
                return Mono.empty();
            });
    }

    public static Context with(Context context, Function<ReactiveLoggerContext, ReactiveLoggerContext> fun) {
        ReactiveLoggerContext loggerContext = context.getOrDefault(LOGGER_CONTEXT, null);
        if (loggerContext != null) {
            return context.put(LOGGER_CONTEXT, fun.apply(loggerContext));
        } else {
            return context.put(LOGGER_CONTEXT, fun.apply(new ReactiveLoggerContext(emptyList())));
        }
    }

    public static Context add(Context context, String key, String value) {
        return with(context, ctx -> ctx.add(key, value));
    }

    public static Context add(Context context, Map<String, String> map) {
        return with(context, ctx -> ctx.add(map));
    }

    public static <T> Mono<T> withContext(T value, Runnable action) {
        return loggerContext()
            .map(context -> {
                logWithContext(context.values, action);
                return value;
            });
    }

    private static void logWithContext(List<Pair<String, String>> context, Runnable action) {
        if (context.isEmpty()) {
            action.run();
        } else {
            Pair<String, String> pair = context.get(0);
            List<Pair<String, String>> tail = context.subList(1, context.size());
            try (final Closeable ignore = MDC.putCloseable(pair.getFirst(), pair.getSecond())) {
                logWithContext(tail, action);
            } catch (IOException e) {
                logger.error("unable to close MDC closeable", e);
            }
        }
    }

    public static Mono<ReactiveLoggerContext> loggerContext() {
        return Mono.subscriberContext()
            .filter(c -> c.hasKey(LOGGER_CONTEXT))
            .map(c -> c.<ReactiveLoggerContext>get(LOGGER_CONTEXT))
            .switchIfEmpty(Mono.defer(() -> Mono.just(new ReactiveLoggerContext(emptyList()))));
    }
}
