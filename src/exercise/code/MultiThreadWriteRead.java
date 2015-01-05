package exercise.code;

import java.util.concurrent.Semaphore;

public class MultiThreadWriteRead {
	private int readerNum;
	private Semaphore mutex;
	private Semaphore wrt;
	
	public MultiThreadWriteRead() {
		readerNum = 0;
		mutex = new Semaphore(1);
		wrt = new Semaphore(1);
	}
	
	public void read() {
		try {
			mutex.acquire();
		}
		catch (InterruptedException ie) {
			ie.printStackTrace();
			return;
		}
		
		try { 
			readerNum++;
			if (readerNum == 1) {
				try {
					wrt.acquire();
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
					return;
				}
			}
		}
		finally {
			mutex.release();
		}
		
		System.out.println(Thread.currentThread().getName() + " is reading.");
		
		mutex.acquireUninterruptibly();
		try {
			readerNum--;
			if (readerNum == 0) {
				wrt.release();			
			}
		}
		finally {
			mutex.release();
		}
	}
	
	public void write() {
		try {
			wrt.acquire();
		}
		catch (InterruptedException ie) {
			ie.printStackTrace();
			return;
		}		
		System.out.println(Thread.currentThread().getName() + " is writing.");
		wrt.release();
	}
}