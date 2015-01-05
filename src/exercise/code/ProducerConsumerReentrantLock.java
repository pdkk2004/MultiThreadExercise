package exercise.code;

import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerReentrantLock {

	private int cycle = 30;
	private int capacity = 10;
	private final SharedResource sr = new SharedResource(capacity);
	private final CountDownLatch consumbarrier = new CountDownLatch(cycle);
	private final CountDownLatch prodBarrier = new CountDownLatch(cycle);

	
	public static void main(String[] args) throws InterruptedException {
		ProducerConsumerReentrantLock pc = new ProducerConsumerReentrantLock();
		pc.run();
	}
	
	private class SharedResource {
		private final Stack<Integer> queue = new Stack<Integer>();
		private int capacity;
		private final ReentrantLock lock = new ReentrantLock();
		private final AtomicInteger count = new AtomicInteger(0);
		private final Condition notEmpty = lock.newCondition();
		private final Condition notFull = lock.newCondition();
		
		public SharedResource(int c) {
			this.capacity = c;
		}
		
		public int size() {
			return count.get();
		}
		
		public void produce() throws InterruptedException {
			lock.lock();
			try {
				while (count.get() == capacity) {
					try {
						notFull.await();
					}
					catch (InterruptedException e) {
						notFull.signal();
						throw e;
					}
				}
				int num = count.incrementAndGet();
				queue.push(num);
				syncPrint("Producer produced:" + num);
				Thread.sleep(100);
				notEmpty.signal();
			}
			finally {
				lock.unlock();
			}
		}
		
		public Integer consume() throws InterruptedException {
			lock.lock();
			try {
				while (count.get() == 0) {
					try {
						notEmpty.await();
					}
					catch (InterruptedException e) {
						notEmpty.signal();
						throw e;
					}
				}
				int ret = queue.pop();
				count.decrementAndGet();
				syncPrint("Consumer consumed:" + ret);
				Thread.sleep(10);
				notFull.signal();
				return ret;
			}
			finally {
				lock.unlock();
			}
		}
	}
	
	private class Producer implements Runnable {
		private SharedResource resource;
		
		public Producer(SharedResource resource) {
			this.resource = resource;
		}
		
		public void run() {
			for (int i = 0; i < cycle; i++) {
				try {
					resource.produce();
					prodBarrier.countDown();
				}
				catch (InterruptedException e) {
					syncPrint("Failed to produce " + i + ". Reason:" + e.getMessage());
				}
			}
		}
	}
	
	private class Consumer implements Runnable {
		private SharedResource resource;
		
		public Consumer(SharedResource resource) {
			this.resource = resource;
		}
		
		public void run() {
			for (int i = 0; i < cycle; i++) {
				try {
					resource.consume();
					consumbarrier.countDown();
				}
				catch (InterruptedException e) {
					System.out.println("Failed to consume " + i + ". Reason:" + e.getMessage());
				}
			}
		}
	}
	
	private void syncPrint(String str) {
		synchronized (System.out) {
			System.out.println(str);
		}
	}
	
	public void run() throws InterruptedException{
		Thread producerThread = new Thread(new Producer(sr));
		producerThread.setPriority(1);
		Thread consumerThread = new Thread(new Consumer(sr));
		consumerThread.setPriority(10);
		producerThread.start();
		consumerThread.start();
		consumbarrier.await();
		prodBarrier.await();
		syncPrint("All producer and consumer tasks are completed.");
	}
}