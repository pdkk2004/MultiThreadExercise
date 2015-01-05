package exercise.code;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OneLockBlockingQueue<T> {

	private static final int DEFAULT_CAPACITY = 30;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition notFull = lock.newCondition();
	private final Condition notEmpty = lock.newCondition();
	private int capacity;
	private LinkedList<T> queue = new LinkedList<>();
	
	public OneLockBlockingQueue() {
		this.capacity = DEFAULT_CAPACITY;
	}
	
	public OneLockBlockingQueue(int capacity) {
		this.capacity = capacity;
	}
	
	public void put(T object) throws InterruptedException {
		lock.lock();
		try {
			while (queue.size() == capacity) {
				notFull.wait();
			}
			queue.add(object);
			notEmpty.signal();
		}
		finally {
			lock.unlock();
		}
	}
	
	public T take() throws InterruptedException {
		lock.lock();
		T ret = null;
		try {
			while (queue.size() == 0) {
				notEmpty.await();
			}
			ret = queue.poll();
			notFull.signal();
			return ret;
		}
		finally {
			lock.unlock();
		}
	}
	
	public int size() {
		lock.lock();
		try {
			return queue.size();
		}
		finally {
			lock.unlock();
		}
	
	}
	
}
