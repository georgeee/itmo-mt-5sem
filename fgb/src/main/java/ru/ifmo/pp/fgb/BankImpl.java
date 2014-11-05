package ru.ifmo.pp.fgb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * @author Agapov
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getAmount(int index) {
        Account account = accounts[index];
        account.lock.lock();
        long amount = account.amount;
        account.lock.unlock();
        return amount;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (Account account : accounts) account.lock.lock();
        for (Account account : accounts) {
            sum += account.amount;
        }
        for (Account account : accounts) account.lock.unlock();
        return sum;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        account.lock.lock();
        if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
            account.lock.unlock();
            throw new IllegalStateException("Overflow");
        }
        long resultingAccountAmount = (account.amount += amount);
        account.lock.unlock();
        return resultingAccountAmount;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        account.lock.lock();
        if (account.amount - amount < 0) {
            account.lock.unlock();
            throw new IllegalStateException("Underflow");
        }
        long resultingAccountAmount = (account.amount -= amount);
        account.lock.unlock();
        return resultingAccountAmount;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        Lock lock1 = from.lock;
        Lock lock2 = to.lock;
        if (fromIndex > toIndex) {
            Lock lock3 = lock1;
            lock1 = lock2;
            lock2 = lock3;
        }
        lock1.lock();
        lock2.lock();
        if (amount > from.amount) {
            lock2.unlock();
            lock1.unlock();
            throw new IllegalStateException("Underflow");
        } else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
            lock2.unlock();
            lock1.unlock();
            throw new IllegalStateException("Overflow");
        }
        from.amount -= amount;
        to.amount += amount;
        lock2.unlock();
        lock1.unlock();
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        long amount;
        final Lock lock;

        private Account() {
            this.lock = new ReentrantLock();
        }
    }
}

