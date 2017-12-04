package com.mcsoft.model;

/**
 * 生产者接口
 * Created by MC on 2017/12/4.
 */
public interface Producer {
    void produce() throws InterruptedException;
}
