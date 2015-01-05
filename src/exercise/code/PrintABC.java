package exercise.code;

import java.util.concurrent.Semaphore;

public class PrintABC {
	
	public static void main(String[] args) {
		PrintABC abcPrinter = new PrintABC();
		abcPrinter.run();
	}
	
	private Semaphore lockA;
	private Semaphore lockB;
	private Semaphore lockC;
	
	
	private void init() {
		lockA = new Semaphore(1);
		lockB = new Semaphore(0);
		lockC = new Semaphore(0);		
	}
	
	public void run() {
		init();
		threadA.start();
		threadB.start();
		threadC.start();
	}
	
	private Thread threadA = new Thread() {
		public void run() {
			for (int i = 0; i < 5; i++) {
				try {
					lockA.acquire();
				}
				catch (InterruptedException e) {
					System.out.println(e.getMessage());
					return;
				}
				System.out.println("ThreadA: print A.");
				lockB.release();
			}
		}
	};
	
	private Thread threadB = new Thread() {
		public void run() {
			for (int i = 0; i < 5; i++) {
				try {
					lockB.acquire();
				}
				catch (InterruptedException e) {
					System.out.println(e.getMessage());
					return;
				}
				System.out.println("ThreadB: print B.");
				lockC.release();
			}
		}
	};

	private Thread threadC = new Thread() {
		public void run() {
			for (int i = 0; i < 5; i++) {
				try {
					lockC.acquire();
				}
				catch (InterruptedException e) {
					System.out.println(e.getMessage());
					return;
				}
				System.out.println("ThreadC: print C.");
				lockA.release();
			}
		}
	};

 }
