package ru.roborox.logging.reactive;

import net.logstash.logback.marker.LogstashMarker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;
import java.util.function.Function;

public class LoggingUtils {
    public static final String ACTION_FIELD = "action";
    public static final String ACTION_HEADER = "X-ACTION-ID";

    public static <T> Mono<T> withAction(Function<String, Mono<T>> action) {
        return Mono.subscriberContext().flatMap(context -> {
            String actionId = createActionId(context);
            return action.apply(actionId);
        });
    }
    
    public static <T> Flux<T> withActionFlux(Function<String, Flux<T>> action) {
        return Mono.subscriberContext().flatMapMany(context -> {
            String actionId = createActionId(context);
            return action.apply(actionId);
        });
    }

    private static String createActionId(Context context) {
        String actionId = context.getOrDefault(ACTION_HEADER, "");
        if (actionId == null || actionId.trim().length() == 0) {
            actionId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        return actionId;
    }
    
    public static <T> Mono<T> withMarker(Function<LogstashMarker, Mono<T>> action) {
        return LogstashMarkers.withMarker(action);
    }
    
    public static <T> Flux<T> withMarkerFlux(Function<LogstashMarker, Flux<T>> action) {
        return LogstashMarkers.withMarkerFlux(action);
    }
}
