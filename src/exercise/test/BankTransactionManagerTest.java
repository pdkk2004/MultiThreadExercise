package exercise.test;

import java.rmi.UnexpectedException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exercise.code.BankTransactionManager;
import exercise.code.BankTransactionManager.Account;
import exercise.code.BankTransactionManager.InsufficientFundException;

public class BankTransactionManagerTest {

	private static enum TransactionType {
		deposit, withdraw, transfer, query;
	}

	public static void main(String[] args) throws UnexpectedException,
			InterruptedException, InsufficientFundException {
		runTransferTest();
		runWithdrawDepositTest();
		runWithdrawTest();
	}

	private static BankTransactionManager manager = new BankTransactionManager();

	private static Account createAccount(long acctNum, String acctName,
			double balance) {
		return new Account(acctNum, acctName, balance);
	}

	private static void runWithdrawTest() throws InterruptedException,
			InsufficientFundException, UnexpectedException {
		Account fromAcct = createAccount(10000, "fromAcct", 500);
		Client client = new Client(manager, fromAcct, null);
		client.withdraw(200);
		client.printBalance();
		client.withdraw(300);
		client.printBalance();
	}

	private static void runWithdrawDepositTest() throws InterruptedException,
			InsufficientFundException, UnexpectedException {
		Account fromAcct = createAccount(10000, "fromAcct", 500);
		Account toAcct = createAccount(20000, "toAcct", -200);
		Client client = new Client(manager, fromAcct, toAcct);
		client.withdraw(500);
		client.deposit(200);
		client.printBalance();
		client.withdraw(700);
		client.printBalance();
	}

	private static void runTransferTest() throws InterruptedException,
			InsufficientFundException, UnexpectedException {
		Account fromAcct = createAccount(10000, "fromAcct", -200);
		Account toAcct = createAccount(20000, "toAcct", 0);
		Client client = new Client(manager, fromAcct, toAcct);
		client.deposit(300);
		client.transfer(100);
		client.printBalance();
	}

	public static class Client {

		private final ExecutorService executor = Executors
				.newCachedThreadPool();
		private Account fromAccount;
		private Account toAccount = null;
		private BankTransactionManager manager;

		public Client(BankTransactionManager manager, Account fromAccount,
				Account toAccount) {
			if (fromAccount == null || manager == null) {
				throw new IllegalArgumentException(
						"From Account or TransactionManager cannot be null.");
			}
			this.manager = manager;
			this.fromAccount = fromAccount;
			if (toAccount != null) {
				this.toAccount = toAccount;
			}
		}

		public void withdraw(double amount) throws UnexpectedException {
			executor.execute(createTransactionTask(TransactionType.withdraw,
					amount));
		}

		public void deposit(double amount) throws UnexpectedException {
			executor.execute(createTransactionTask(TransactionType.deposit,
					amount));
		}

		public void transfer(double amount) throws UnexpectedException {
			executor.execute(createTransactionTask(TransactionType.transfer,
					amount));
		}

		public void printBalance() throws InterruptedException {
			double fromBalance = this.fromAccount.getBalance();
			System.out.println(String.format("Account:%s had balance: %f",
					fromAccount.getAccountNum(),
					fromBalance));
			if (toAccount != null) {
				double toBalance = this.toAccount.getBalance();
				System.out.println(String.format("Account:%s had balance: %f",
						toAccount.getAccountNum(),
						toBalance));
			}
		}

		private Runnable createTransactionTask(TransactionType type,
				final double amount) throws UnexpectedException {
			switch (type) {
			case withdraw:
				return new Runnable() {

					@Override
					public void run() {
						try {
							manager.withdraw(fromAccount, amount);
						} catch (Exception e) {
							synchronized (System.out) {
								System.out.println(e.getMessage());
							}
						}
					}
				};
			case deposit:
				return new Runnable() {
					@Override
					public void run() {
						try {
							manager.deposit(fromAccount, amount);
						} catch (Exception e) {
							synchronized (System.out) {
								System.out.println(e.getMessage());
							}
						}
					}
				};
			case transfer:
				return new Runnable() {
					public void run() {
						try {
							manager.transfer(fromAccount, toAccount, amount);
						} catch (Exception e) {
							synchronized (System.out) {
								System.out.println(e.getMessage());
							}
						}
					}
				};
			default:
				throw new UnexpectedException(type.toString());
			}
		}
	}
}
