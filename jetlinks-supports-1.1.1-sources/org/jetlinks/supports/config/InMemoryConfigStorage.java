package org.jetlinks.supports.config;

import org.jetlinks.core.Value;
import org.jetlinks.core.Values;
import org.jetlinks.core.config.ConfigStorage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InMemoryConfigStorage implements ConfigStorage {

    private final ConcurrentMap<String, Object> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Value> getConfig(String key) {
        return Mono.justOrEmpty(Optional.of(key).map(storage::get))
                .map(Value::simple)
                .cache();
    }


    @Override
    public Mono<Values> getConfigs(Collection<String> key) {
        return Flux.fromIterable(key)
                .filter(storage::containsKey)
                .map((k) -> Tuples.of(k, storage.get(k)))
                .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2))
                .map(Values::of)
                ;
    }

    @Override
    public Mono<Boolean> setConfigs(Map<String, Object> values) {
        return Mono.fromRunnable(() -> storage.putAll(values)).thenReturn(true);
    }

    @Override
    public Mono<Boolean> setConfig(String key, Object value) {
        return Mono.fromRunnable(() -> storage.put(key, value)).thenReturn(true);
    }

    @Override
    public Mono<Boolean> remove(String key) {
        return Mono.justOrEmpty(key)
                .map(storage::remove)
                .then().thenReturn(true);
    }

    @Override
    public Mono<Value> getAndRemove(String key) {
        return getConfig(key)
                .flatMap(v -> remove(key).thenReturn(v));
    }

    @Override
    public Mono<Boolean> remove(Collection<String> key) {
        return Flux.fromIterable(key)
                .doOnNext(storage::remove)
                .then().thenReturn(true);
    }

    @Override
    public Mono<Boolean> clear() {
        return Mono.fromRunnable(storage::clear);
    }
}
