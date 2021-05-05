package me.xep.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//https://mkyong.com/logging/slf4j-logback-tutorial/
public class LogbackTest {

    private static final Logger logger= LoggerFactory.getLogger(LogbackTest.class.getName());

    public static void main(String args[]) {
        logger.debug("Hello World");
        logger.debug("logger.isDebugEnabled {}", logger.isDebugEnabled());
        logger.debug("{}", getNum());
    }

    static int getNum() {
        return 5;
    }
}
