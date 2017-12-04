package com.mcsoft;

import com.mcsoft.model.AbstractConsumer;
import com.mcsoft.model.AbstractProducer;
import com.mcsoft.model.ModelFactory;
import com.mcsoft.model.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阻塞式队列实现的生产者-消费者模型
 * 在使用时可以保证队列未满时顺利的添加内容，当队列满时，使用put()方式会阻塞生产者，使用add()方式
 * 则将抛出异常java.lang.IllegalStateException: Queue full，使用offer()方式则返回false
 * 但无论如何提取的操作一定会发生在添加之前，可以保证执行顺序
 * Created by Mc on 2017/12/4.
 */
public class BlockingQueueModel implements ModelFactory {
    private final BlockingQueue<Task> queue;
    private final AtomicInteger increTaskNo = new AtomicInteger(0);

    public BlockingQueueModel(int cap) {
        this.queue = new LinkedBlockingQueue<>(cap);
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
            Task task = queue.take();
            //定期的消费，模拟稳定的服务器
            Thread.sleep((long) (500 + Math.random() * 500));
            System.out.println("consume:" + task.getNo());
        }
    }

    private class ProducerImpl extends AbstractProducer {
        @Override
        public void produce() throws InterruptedException {
            //不定期生产，模拟随机的用户请求
            Thread.sleep((long) (Math.random() * 1000));
            Task task = new Task(increTaskNo.getAndIncrement());
            queue.put(task);
            System.out.println("produce:" + task.getNo());
        }
    }

    public static void main(String[] args) {
        ModelFactory model = new BlockingQueueModel(3);
        for (int i = 0; i < 2; i++) {
            new Thread(model.newRunnableConsumer()).start();
        }
        for (int i = 0; i < 5; i++) {
            new Thread(model.newRunnableProducer()).start();
        }
    }
}
