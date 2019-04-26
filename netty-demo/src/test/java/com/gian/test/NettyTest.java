package com.gian.test;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Unit test for simple App.
 */
public class NettyTest {

    @Test
    public void test(){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Thread thread = new Thread(new SocketClientRequestThread(countDownLatch, 1));
        thread.start();

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {

        }

        countDownLatch.countDown();

        try {
            Thread.sleep(300000000L);
        } catch (InterruptedException e) {

        }
    }


}
