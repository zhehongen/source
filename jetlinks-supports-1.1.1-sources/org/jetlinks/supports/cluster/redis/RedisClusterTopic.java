package org.jetlinks.supports.cluster.redis;

import org.jetlinks.core.cluster.ClusterTopic;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.Disposable;
import reactor.core.publisher.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class RedisClusterTopic<T> implements ClusterTopic<T> {

    private final String topicName;

    private final ReactiveRedisOperations<Object, T> operations;

    private final FluxProcessor<TopicMessage<T>, TopicMessage<T>> processor;

    private final FluxSink<TopicMessage<T>> sink;

    private final AtomicBoolean subscribed = new AtomicBoolean();

    public RedisClusterTopic(String topic, ReactiveRedisOperations<Object, T> operations) {
        this.topicName = topic;
        this.operations = operations;
        processor = EmitterProcessor.create(false);
        sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);
    }

    private Disposable disposable;

    private void doSubscribe() {
        if (subscribed.compareAndSet(false, true)) {
            disposable = operations
                    .listenToPattern(topicName)
                    .subscribe(data -> {
                        if (!processor.hasDownstreams()) {
                            disposable.dispose();
                            subscribed.compareAndSet(true, false);
                        } else {
                            sink.next(new TopicMessage<T>() {
                                @Override
                                public String getTopic() {
                                    return data.getChannel();
                                }

                                @Override
                                public T getMessage() {
                                    return data.getMessage();
                                }
                            });
                        }
                    });
        }
    }

    @Override
    public Flux<TopicMessage<T>> subscribePattern() {
        return processor
                .doOnSubscribe((r) -> doSubscribe());
    }

    @Override
    public Mono<Integer> publish(Publisher<? extends T> publisher) {
        return Flux.from(publisher)
                .flatMap(data -> operations.convertAndSend(topicName, data))
                .last(1L)
                .map(Number::intValue);
    }
}
