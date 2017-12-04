package com.mcsoft.model;

/**
 * 生产者消费者工厂接口
 * Created by Mc on 2017/12/4.
 */
public interface ModelFactory {
    Runnable newRunnableConsumer();
    Runnable newRunnableProducer();
}
