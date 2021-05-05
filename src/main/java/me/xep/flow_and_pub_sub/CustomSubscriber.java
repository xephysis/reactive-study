package me.xep.flow_and_pub_sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Flow;

public class CustomSubscriber implements Flow.Subscriber {

    private final Logger logger = LoggerFactory.getLogger(CustomSubscriber.class.getName());
    private static final int DEMAND = 3;
    private static final Random RANDOM = new Random();
    private String name;
    private Flow.Subscription subscription;
    private int count;

    public CustomSubscriber(String name) {
        this.name = name;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        logger.debug("[SUBSCRIBER {}] onSubscribe", this.name);
        this.subscription = subscription;
        count = DEMAND;
        requestItem(DEMAND);
    }

    private void requestItem(int n) {
        logger.debug("[SUBSCRIBER {}] requestItem {}", this.name, n);
        subscription.request(n);
    }

    @Override
    public void onNext(Object item) {
        if (Objects.isNull(item)) {
            logger.warn("[SUBSCRIBER {}] onNext item null", this.name);
            return;
        }
        logger.debug("[SUBSCRIBER {}] onNext {}", this.name, item);
        synchronized (this) {
            count--;

            if (count == 0) {
                if (RANDOM.nextBoolean()) {
                    count = DEMAND;
                    requestItem(count);
                } else {
                    count = 0;
                    logger.debug("[SUBSCRIBER {}] stop, cancel subscription", this.name);
                    subscription.cancel();
                }
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.debug("[SUBSCRIBER {}] onError {}", this.name, throwable);
    }

    @Override
    public void onComplete() {
        logger.debug("[SUBSCRIBER {}] onComplete", this.name);
    }
}
