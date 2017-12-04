package com.mcsoft;

import com.mcsoft.model.AbstractConsumer;
import com.mcsoft.model.AbstractProducer;
import com.mcsoft.model.ModelFactory;
import com.mcsoft.model.Task;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于Lock&Condition模型实现的生产者-消费者模型
 * 实际上和Wait-Notify模型类似，只是不再使用对象锁，而是换成了并发包内提供的锁
 * Created by Mc on 2017/12/4.
 */
public class LockConditionModel implements ModelFactory {
    private final Lock BUFFER_LOCK = new ReentrantLock();
    //Condition由锁创建，单独提出了wait/notify的操作，使其可以基于锁来对线程进行挂起/唤醒
    private final Condition BUFFER_COND = BUFFER_LOCK.newCondition();
    private final Queue<Task> buffer = new LinkedList<>();
    private final int cap;
    private final AtomicInteger increTaskNo = new AtomicInteger(0);

    public LockConditionModel(int cap) {
        this.cap = cap;
    }

    @Override
    public Runnable newRunnableConsumer() {
        return new ConsumerImpl();
    }

    @Override
    public Runnable newRunnableProducer() {
        return new ProducerImpl();
    }

    private class ConsumerImpl extends AbstractConsumer {
        @Override
        public void consume() throws InterruptedException {
            BUFFER_LOCK.lockInterruptibly();
            try {
                while (buffer.size() == 0) {
                    BUFFER_COND.await();
                }
                Task task = buffer.poll();
                //定期的消费，模拟稳定的服务器
                Thread.sleep((long) (500 + Math.random() * 500));
                System.out.println("consume:" + task.getNo());
                BUFFER_COND.signalAll();
            } finally {
                BUFFER_LOCK.unlock();
            }
        }
    }

    private class ProducerImpl extends AbstractProducer {
        @Override
        public void produce() throws InterruptedException {
            Thread.sleep((long) (Math.random() * 1000));
            BUFFER_LOCK.lockInterruptibly();
            try {
                while (buffer.size() == cap) {
                    BUFFER_COND.await();
                }
                Task task = new Task(increTaskNo.getAndIncrement());
                buffer.offer(task);
                System.out.println("produce:" + task.getNo());
                BUFFER_COND.signalAll();
            } finally {
                BUFFER_LOCK.unlock();
            }
        }
    }

    public static void main(String[] args) {
        ModelFactory model = new LockConditionModel(3);
        for (int i = 0; i < 2; i++) {
            new Thread(model.newRunnableConsumer()).start();
        }
        for (int i = 0; i < 5; i++) {
            new Thread(model.newRunnableProducer()).start();
        }
    }
}
