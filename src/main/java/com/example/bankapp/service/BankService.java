package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.SameAccountTransferException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Class එකටම බලපාන read-only transaction එක[cite: 7]
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository bankTransactionRepository;

    @Transactional
    public void createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new RuntimeException("Account number already exists!");
        }
        customerRepository.save(customer);
    }

   
    @Transactional(rollbackFor = Exception.class)
   
    public BankTransaction transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
        
      
        if (fromAccount.equals(toAccount)) {
            throw new SameAccountTransferException("Sender and receiver must be different accounts.");
        }
        
        customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found."));
        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found."));

      
        int updatedRows = customerRepository.debit(fromAccount, amount);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + fromAccount);
        }

       
        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure after the withdrawal");
        }

        
        customerRepository.credit(toAccount, amount);

        
        BankTransaction transaction = new BankTransaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setDescription("Transfer completed");
        transaction.setStatus(TransactionStatus.SUCCESS);
        
        return bankTransactionRepository.save(transaction);
    }

   
    public List<Customer> searchCustomers(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return customerRepository.searchByName(keyword);
        }
        return customerRepository.findAllOrdered();
    }

    public BigDecimal getTotalBalance() {
        return customerRepository.getTotalBalance();
    }

    public List<BankTransaction> getLatestTransactions() {
        return bankTransactionRepository.findAllLatest();
    }

    public long getSuccessCount() {
        return bankTransactionRepository.countByStatus(TransactionStatus.SUCCESS);
    }

    public long getFailedCount() {
        return bankTransactionRepository.countByStatus(TransactionStatus.FAILED);
    }
    public List<BankTransaction> getHistory(String accountNumber) {
    return bankTransactionRepository.findHistory(accountNumber);
}
}