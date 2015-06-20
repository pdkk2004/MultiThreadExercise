package exercise.code;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BankTransactionManager {
	
	public static class Account {
		
		private static final int SLEEP_MILLS = 100;
		private double money;
		private long accountNum;
		private String accountName;
		private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
		
		public Account(long accountNum, String accountName) {
			this.accountName = accountName;
			this.accountNum = accountNum;
			this.money = 0.0D;
		}
		
		public Account(long accountNum, String accountName, double init) {
			this(accountNum, accountName);
			this.money = init;
		}
		
		public void lockAccount() throws InterruptedException {
			lock.writeLock().lockInterruptibly();
		}
		
		public void unlockAccount() {
			lock.writeLock().unlock();
		}
		
		public void transferTo(Account to, double amount) throws InterruptedException, InsufficientFundException {
			Lock wLock = lock.writeLock();
			print(String.format("Starting transfer %f dollars from Acct %s to Acct %s.", amount, this.accountNum, to.accountNum));

			try {
				wLock.lockInterruptibly();
			}
			catch (InterruptedException e) {
				print("transfer operation was interrupted.");
			}
			
			try {
				if (money < amount) {
					throw new InsufficientFundException("Insufficient fund in source account, transfering is terminated."); 
				}
				
				money -= amount;
				print(String.format("%f dollars was withdrawn from Acct %s", amount, this.accountNum));
				try {
					to.deposit(amount);
				}
				catch (InterruptedException e) {
					print("Fail to deposit to account:" + to.getAccountNum() + ", refund " + amount + " to account " + this.accountNum);
					print("Transferring is terminated due to deposit to Acct " + to.accountNum + " failed.");
					throw e;
				}
				money -= amount;
				// Persist updated money in this account.
				Thread.sleep(SLEEP_MILLS);
				print("Transferring complete successfully.");
			}
			finally {
				wLock.unlock();
			}
		}
		
		public void withdraw(double amount) throws InterruptedException, InsufficientFundException {
			Lock wLock = lock.writeLock();
			try {
				wLock.lockInterruptibly();
			}
			catch (InterruptedException e) {
				print("withdraw operation was interrupted.");
				throw e;
			}
			
			try {
				if (money < amount) {
					throw new InsufficientFundException("Insufficient fund in source account, transfering is terminated."); 
				}
				money -= amount;
				
				Thread.sleep(SLEEP_MILLS);
				//persist operation can go here.
				print(String.format("%f dollars was withdrawn from Acct %s", amount, this.accountNum));
			}
			finally {
				wLock.unlock();
			}
		}
		
		public void deposit(double amount) throws InterruptedException {
			Lock wLock = lock.writeLock();
			try {
				wLock.lockInterruptibly();
			}
			catch (InterruptedException e) {
				print("deposit operation was interrupted.");
				throw e;
			}
			
			try {
				money += amount;
				// persist operation can go here.
				Thread.sleep(SLEEP_MILLS);
				print(String.format("%f dollars was deposited into Acct %s", amount, this.accountNum));
			}
			finally {
				wLock.unlock();
			}
		}
		
		public double getBalance() throws InterruptedException{
			lock.readLock().lockInterruptibly();
			try {
				return this.money;
			}
			finally {
				lock.readLock().unlock();
			}
		}
		
		public long getAccountNum() {
			return accountNum;
		}
		public void setAccountNum(long accountNum) {
			this.accountNum = accountNum;
		}
		public String getAccountName() {
			return accountName;
		}
		public void setAccountName(String accountName) {
			this.accountName = accountName;
		}
	}
	
	public BankTransactionManager() {
		
	}
	
	public void deposit(Account toAccount, double amount) throws InterruptedException {
		toAccount.deposit(amount);
	}
	
	public void withdraw(Account fromAccount, double amount) throws InterruptedException, InsufficientFundException {
		fromAccount.withdraw(amount);
	}
	
	public void transfer(Account from, Account to, double amount) throws InterruptedException, InsufficientFundException {
		from.transferTo(to, amount);
	}
	
	private static void print(String str) {
		synchronized (System.out) {
			System.out.println(str);
		}
	}
	
	@SuppressWarnings("serial")
	public static class InsufficientFundException extends Exception {
		
		public InsufficientFundException() {
			super("Insufficient fund in source account.");
		}
		
		public InsufficientFundException(String msg) {
			super(msg);
		}
	}
}
