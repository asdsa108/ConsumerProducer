package com.mcsoft.model;

/**
 * 生产者抽象类
 * Created by MC on 2017/12/4.
 */
public abstract class AbstractProducer implements Producer,Runnable {
    @Override
    public void run() {
        while (true){
            try {
                produce();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
