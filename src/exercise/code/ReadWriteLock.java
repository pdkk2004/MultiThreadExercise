package exercise.code;

import java.util.concurrent.Semaphore;

public class ReadWriteLock {

	private Semaphore rLock = new Semaphore(1);
	private Semaphore wLock = new Semaphore(1);
	private int numReads = 0;
	
	public void read() {
		try {
			rLock.acquire();
		}catch (InterruptedException e) {
			return;
		}
		if (numReads == 0) {
			try {
				wLock.acquire();
				numReads++;
			}catch (InterruptedException e) {
				rLock.release();
				return;
			}
		}
		rLock.release();
		//do real read;
		rLock.acquireUninterruptibly();
		numReads--;
		if (numReads == 0) {
			wLock.release();
		}
		rLock.release();
	}
}
