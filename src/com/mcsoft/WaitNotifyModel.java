package com.mcsoft;

import com.mcsoft.model.AbstractConsumer;
import com.mcsoft.model.AbstractProducer;
import com.mcsoft.model.ModelFactory;
import com.mcsoft.model.Task;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 利用Wait-Notify模型实现的生产者-消费者模型
 * 该方法利用了锁机制，在缓冲区队列长度为0时阻塞消费者，在缓冲区队列达到上限时阻塞生产者
 * 同时提供了一个队列作为缓冲区
 * 锁机制是：当线程拿到一个对象的对象级锁时，则进行执行，当对象调用wait()方法时，则线程释放该对象的对象锁，
 * 并进入等待池，当对象调用notify()方法时，则随机唤醒一个等待池中的线程，线程进入锁池，此时线程可以竞争拿
 * 到对象的锁，当对象调用notifyAll()方法时，则唤醒所有该对象的等待池中的线程，全部进入锁池竞争
 * Created by Mc on 2017/12/4.
 */
public class WaitNotifyModel implements ModelFactory {
    private final Object BUFFER_LOCK = new Object();
    private final Queue<Task> buffer = new LinkedList<>();
    private final int cap;
    private final AtomicInteger increTaskNo = new AtomicInteger(0);

    public WaitNotifyModel(int cap) {
        this.cap = cap;
    }

    @Override
    public Runnable newRunnableConsumer() {
        return new ComsumerImpl();
    }

    @Override
    public Runnable newRunnableProducer() {
        return new ProducerImpl();
    }

    private class ComsumerImpl extends AbstractConsumer {
        @Override
        public void consume() throws InterruptedException {
            synchronized (BUFFER_LOCK) {
                while (buffer.size() == 0) {
                    //线程进入锁等待池，等待被唤醒进入锁竞争池
                    BUFFER_LOCK.wait();
                }
                //从队列中弹出一个元素
                Task task = buffer.poll();
                //定期的消费，模拟稳定的服务器
                Thread.sleep((long) (500 + Math.random() * 500));
                System.out.println("consume:" + task.getNo());
                BUFFER_LOCK.notifyAll();
            }
        }
    }

    private class ProducerImpl extends AbstractProducer {
        @Override
        public void produce() throws InterruptedException {
            //不定期生产，模拟随机的用户请求
            Thread.sleep((long) (Math.random() * 1000));
            synchronized (BUFFER_LOCK) {
                while (buffer.size() == cap) {
                    BUFFER_LOCK.wait();
                }
                Task task = new Task(increTaskNo.getAndIncrement());
                buffer.offer(task);
                System.out.println("produce:" + task.getNo());
                BUFFER_LOCK.notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        ModelFactory model = new WaitNotifyModel(3);
        for (int i = 0; i < 2; i++) {
            new Thread(model.newRunnableConsumer()).start();
        }
        for (int i = 0; i < 5; i++) {
            new Thread(model.newRunnableProducer()).start();
        }
    }
}
