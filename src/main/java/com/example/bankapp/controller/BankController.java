package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService; 

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("customers", bankService.searchCustomers(keyword));
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("latestTransactions", bankService.getLatestTransactions());
        model.addAttribute("successCount", bankService.getSuccessCount());
        model.addAttribute("failedCount", bankService.getFailedCount());
        
        
        model.addAttribute("transferRequest", new TransferRequest());
        model.addAttribute("newCustomer", new Customer());
        return "bank"; 
    }

    
    @PostMapping("/transfer")
    @ResponseBody
    public String transfer(@Valid @RequestBody TransferRequest request) {
        try {
            
            bankService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount(), request.isSimulateFailure());
            
           
            return "Transfer Successful!"; 
            
        } catch (Exception e) {
            auditService.recordFailure(request.getFromAccount(), request.getToAccount(), request.getAmount(), e.getMessage());
            
            
            return "Failed: " + e.getMessage();
        }
    }

    
    @PostMapping("/customers")
    @ResponseBody
    public String addCustomer(@Valid @RequestBody Customer customer) {
        try {
            bankService.createCustomer(customer);
            return "Customer added successfully!";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    
    @GetMapping("/accounts/{accountNumber}/history")
public String viewHistory(@PathVariable String accountNumber, Model model) {
   
    List<BankTransaction> transactions = bankService.getHistory(accountNumber);
    
    
    model.addAttribute("transactions", transactions);
    
    return "history";

}
}
