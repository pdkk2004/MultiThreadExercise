package exercise.code;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WriteReadExecutorFramework {

	private ExecutorService executor;
	private static int cycle = 20;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		WriteReadExecutorFramework wr = new WriteReadExecutorFramework();
		wr.init();
		
		final Queue<Future<?>> resultQueue = new LinkedList<Future<?>>();
		
		for (int i = 0; i < cycle; i++) {
			resultQueue.add(wr.read());
			resultQueue.add(wr.write(Math.round(Math.random() * 100)));
		}

		if (wr.terminate()) {
			syncPrint("Succeessfully terminated.");
		}
		
		Thread processResultThread = new Thread() {
			
			public void run() {
				Iterator<Future<?>> iter = resultQueue.iterator();
				while (iter.hasNext()) {
					Future<?> f = iter.next();
					try {
						syncPrint(f.get().toString());
					} catch (Exception e) {
					
					}
				}
			}
		};
		
		processResultThread.start();
	}
	
	public static void syncPrint(String str) {
		synchronized (System.out) {
			System.out.println(str);
		}
	}
	
	public void init() {
		executor = Executors.newCachedThreadPool();
	}
	
	public Future<Object> read() throws InterruptedException, ExecutionException {
		Callable<Object> task = new ReadTask();
		return executor.submit(task);
	}
	
	public Future<Boolean> write(Object obj) {
		Callable<Boolean> task = new WriteTask(new Object());
		return executor.submit(task);
	}
	
	private class ReadTask implements Callable<Object> {
		
		@Override
		public Object call() throws Exception {
			String ret = "Random readed object:" + Math.round(Math.random() * 100);
			Thread.sleep(10);
			return ret;
		}
	}
	
	private class WriteTask implements Callable<Boolean> {
		private Object objToWrite;
		
		public WriteTask(Object obj) {
			this.objToWrite = obj;
		}

		@Override
		public Boolean call() throws Exception {
			synchronized(System.out) {
				System.out.println("writing object:" + objToWrite.toString());
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					return false;
				}
				return true;
			}
		}
	}
	
	public boolean terminate() {
		executor.shutdown();
		try {
			return executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
}
