package ru.roborox.logging.reactive;

import kotlin.Pair;
import net.logstash.logback.marker.LogstashMarker;
import net.logstash.logback.marker.Markers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogstashMarkers {
    public static <T> Mono<T> withMarker(Function<LogstashMarker, Mono<T>> action) {
        return ReactiveLoggerContext.loggerContext()
            .map(LogstashMarkers::get)
            .flatMap(action);
    }

    public static <T> Flux<T> withMarkerFlux(Function<LogstashMarker, Flux<T>> action) {
        return ReactiveLoggerContext.loggerContext()
            .map(LogstashMarkers::get)
            .flatMapMany(action);
    }

    private static LogstashMarker get(ReactiveLoggerContext reactiveContext) {
        Map<String, String> map = reactiveContext.getValues().stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return Markers.appendEntries(map);
    }
}
