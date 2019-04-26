package com.gian.test;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;

/**
 * Created by gaojian on 2019/4/26.
 */
public class SocketClientRequestThread implements Runnable {

    private static Logger logger = Logger.getLogger(SocketClientRequestThread.class);

    private CountDownLatch countDownLatch;

    private Integer clientIndex;

    public SocketClientRequestThread(CountDownLatch countDownLatch , Integer clientIndex) {
        this.countDownLatch = countDownLatch;
        this.clientIndex = clientIndex;
    }

    @Override
    public void run() {
        Socket socket = null;
        OutputStream clientRequest = null;
        InputStream clientResponse = null;

        try{
            socket = new Socket("localhost", 83);
            clientRequest = socket.getOutputStream();
            clientResponse = socket.getInputStream();

            this.countDownLatch.wait();

            clientRequest.write(URLEncoder.encode("这是第" + this.clientIndex + " 个客户端的请求11。", "UTF-8").getBytes());
            clientRequest.flush();
            clientRequest.write(URLEncoder.encode("这是第" + this.clientIndex + " 个客户端的请求22。over","UTF-8").getBytes());

            logger.info("第" + this.clientIndex + "个客户端的请求发送完成，等待服务器返回信息");

            int maxLen = 1024;
            byte[] contentBytes = new byte[maxLen];
            int readLen;
            String message = "";

            while((readLen = clientResponse.read(contentBytes, 0, maxLen)) == -1){
                message += new String(contentBytes, 0, readLen);
            }

            logger.info("接收到来自服务器的信息:" + message);

        }catch (Exception e){
            logger.error(e);
        } finally {
            try {
                if(clientRequest != null) {
                    clientRequest.close();
                }
                if(clientResponse != null) {
                    clientResponse.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

}