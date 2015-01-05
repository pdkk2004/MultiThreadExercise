package exercise.code;

import java.util.Stack;

public class ProducerConsumerIntrsincLock {
	
	private int cycle = 30;
	private int capacity = 10;
	private final SharedResource sr = new SharedResource(capacity);
	
	public static void main(String[] args) {
		ProducerConsumerIntrsincLock pc = new ProducerConsumerIntrsincLock();
		pc.run();
	}
	
	private class SharedResource {
		private final Stack<Integer> queue = new Stack<Integer>();
		private int capacity;

		
		public SharedResource(int c) {
			this.capacity = c;
		}
		
		public synchronized int size() {
			return queue.size();
		}
		
		public synchronized void produce() throws InterruptedException {
			while (queue.size() == capacity) {
				wait();
			}
			int num = queue.size() + 1;
			queue.push(num);
			System.out.println("Producer produced:" + num);
			Thread.sleep(10);
			this.notify();
		}
		
		public synchronized Integer consume() throws InterruptedException {
			while (queue.size() == 0) {
				wait();
			}
			int ret = queue.pop();
			System.out.println("Consumer consumed:" + ret);
			Thread.sleep(10);
			this.notify();
			return ret;
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
				}
				catch (InterruptedException e) {
					System.out.println("Failed to produce " + i + ". Reason:" + e.getMessage());
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
				}
				catch (InterruptedException e) {
					System.out.println("Failed to consume " + i + ". Reason:" + e.getMessage());
				}
			}
		}
	}
	
	public void run() {
		Thread producerThread = new Thread(new Producer(sr));
		producerThread.setPriority(1);
		Thread consumerThread = new Thread(new Consumer(sr));
		consumerThread.setPriority(10);
		producerThread.start();
		consumerThread.start();
	}
}
