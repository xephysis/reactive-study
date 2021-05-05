package me.xep.flow_and_pub_sub;

public class Tester {
    public static void main(String args[]) {
        CustomPublisher customPublisher = new CustomPublisher();
        CustomSubscriber subscriberA = new CustomSubscriber("A");
        CustomSubscriber subscriberB = new CustomSubscriber("B");

        customPublisher.subscribe(subscriberA);
        customPublisher.subscribe(subscriberB);

        //blocking future get
        customPublisher.waitUntilTerminated();
    }
}
