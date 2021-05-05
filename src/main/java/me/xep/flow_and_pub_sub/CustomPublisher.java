package me.xep.flow_and_pub_sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

//https://grokonez.com/java/java-9-flow-api-example-publisher-and-subscriber
public class CustomPublisher implements Flow.Publisher {

    private final Logger logger = LoggerFactory.getLogger(CustomPublisher.class.getName());

    final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private List<Flow.Subscription> subscriptions = Collections.synchronizedList(new ArrayList<>());
    private final CompletableFuture terminated = new CompletableFuture<>();

    @Override
    public void subscribe(Flow.Subscriber subscriber) {
        logger.debug("[PUBLISHER] subscribe");
        CustomSubscription subscription = new CustomSubscription(executorService, subscriber);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void waitUntilTerminated() {
        try {
            terminated.get();
        } catch (ExecutionException | InterruptedException e) {
            System.out.println(e);
        }
    }

    private class CustomSubscription implements Flow.Subscription {

        private final Logger logger = LoggerFactory.getLogger(CustomSubscription.class.getName());

        private ExecutorService executorService;
        private final Flow.Subscriber subscriber;
        private final AtomicInteger value;
        private AtomicBoolean isCanceled;


        public CustomSubscription(ExecutorService executorService, Flow.Subscriber subscriber) {
            this.executorService = executorService;
            this.subscriber = subscriber;
            this.value = new AtomicInteger();
            this.isCanceled = new AtomicBoolean(false);
        }

        @Override
        public void request(long n) {
            logger.debug("[SUBSCRIPTION] request {}", n);
            if (isCanceled.get()) {
                return;
            }
            if (n < 0) {
                executorService.execute(() -> {
                    subscriber.onError(new IllegalArgumentException());
                });
            } else {
                publishItem(n);
            }
        }

        @Override
        public void cancel() {
            logger.debug("[SUBSCRIPTION] cancel");
            isCanceled.set(true);
            synchronized (subscriptions) {
                subscriptions.remove(this);
                logger.debug("[SUBSCRIPTION] size {}", subscriptions.size());
                if (subscriptions.size() == 0) {
                    shutdown();
                }
            }
        }

        private void publishItem(long n) {
            for (int i = 0; i < n; i++) {
                executorService.execute(() -> {
                    int v = value.incrementAndGet();
                    logger.debug("[SUBSCRIPTION] publishItem {}", v);
                    subscriber.onNext(v);
                });
            }
        }

        private void shutdown() {
            logger.debug("[SUBSCRIPTION] Shutdown in subscription");
            executorService.shutdown();
            newSingleThreadExecutor().submit(() -> {
                logger.debug("[SUBSCRIPTION] Shutdown in subscription complete");
                terminated.complete(null);
            });
        }
    }
}
