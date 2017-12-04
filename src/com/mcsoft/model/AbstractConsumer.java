package com.mcsoft.model;

/**
 * 消费者抽象类
 * Created by MC on 2017/12/4.
 */
public abstract class AbstractConsumer implements Consumer,Runnable {
    @Override
    public void run() {
        while (true){
            try {
                consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
