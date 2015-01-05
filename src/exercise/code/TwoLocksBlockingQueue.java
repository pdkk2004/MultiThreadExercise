package exercise.code;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TwoLocksBlockingQueue<T> {
	
	private static final int DEFAULT_CAPACITY = 100;
	
	private final LinkedList<T> queue =  new LinkedList<T>();
	private final ReentrantLock putLock = new ReentrantLock();
	private final ReentrantLock takeLock = new ReentrantLock();
	private final Condition notFullCondition = putLock.newCondition();
	private final Condition notEmpty = takeLock.newCondition();
	private int capacity;
	private final AtomicInteger count = new AtomicInteger();
	
	public TwoLocksBlockingQueue() {
		this.capacity = DEFAULT_CAPACITY;
	}
	
	public TwoLocksBlockingQueue(int capacity) {
		this.capacity = capacity;
	}
	
	public boolean isEmpty() {
		return count.get() == 0;
	}
	
	public int size() {
		return count.get();
	}
	
	public void put(T object) throws InterruptedException {
		putLock.lockInterruptibly();
		int c = -1;
		try {
			while (count.get() == capacity) {
				try {
					notFullCondition.await();
				}
				catch (InterruptedException e) {
					notFullCondition.signal();
					throw e;
				}
			}
			queue.add(object);
			c = count.getAndIncrement();
			if (c + 1 < capacity) {
				notFullCondition.signal();
			}
		}
		finally {
			putLock.unlock();
		}
		if (c == 0) {
			//signalNotEmpty needs to acquire takeLock first. Hence, avoid the possiblity
			// of deadlock right after take() method check count.get() == 0 call. 
			signalNotEmpty();
		}
	}
	
	public boolean offer(T object) {
		int c = -1;
		if (count.get() == capacity) {
			return false;
		}
		putLock.lock();
		try {
			if (count.get() < capacity) {
				queue.add(object);
				c = count.getAndIncrement();				
			}
			if (c + 1 < capacity) {
				notFullCondition.signal();
			}
		}
		finally {
			putLock.unlock();
		}
		
		if (c == 0) {
			signalNotEmpty();
		}
		return true;
	}
	
	public T take() throws InterruptedException {
		T ret = null;
		takeLock.lockInterruptibly();
		int c = -1;
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
			ret = queue.removeFirst();
			c = count.getAndDecrement();
			if (c - 1 > 0) {
				notEmpty.signal();
			}
		}
		finally {
			takeLock.unlock();
		}
		if (c == capacity) {
			//signalNotFull needs to acquire takeLock first. Hence, avoid the possiblity
			// of deadlock right after put() method check count.get() == capacity call. 
			signalNotFull();
		}
		return ret;
	}
	
	private void signalNotFull() {
		putLock.lock();
		try {
			notFullCondition.signal();
		}
		finally {
			putLock.unlock();
		}
	}
	
	private void signalNotEmpty() {
		takeLock.lock();
		try {
			notEmpty.signal();
		}
		finally {
			takeLock.unlock();
		}
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}
