package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    
    @Query("SELECT b FROM BankTransaction b ORDER BY b.createdAt DESC")
    List<BankTransaction> findAllLatest();

    
    @Query("SELECT b FROM BankTransaction b WHERE b.fromAccount = :account OR b.toAccount = :account ORDER BY b.createdAt DESC")
    List<BankTransaction> findHistory(@Param("account") String account);

    
    @Query("SELECT b FROM BankTransaction b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<BankTransaction> findByStatusLatest(@Param("status") TransactionStatus status);

    
    long countByStatus(TransactionStatus status);
}
