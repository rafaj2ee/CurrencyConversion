package com.rafaj2ee.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.service.PurchaseTransactionService;
import com.rafaj2ee.util.Constant;

@RestController
@RequestMapping("/api/v1/purchase-transactions")
public class PurchaseTransactionController {

    @Autowired
    private PurchaseTransactionService service;

    @PostMapping
    public PurchaseTransaction saveTransaction(@RequestBody PurchaseTransactionDTO dto) throws Exception {
        return service.saveTransaction(dto);
    }

    @GetMapping
    public List<PurchaseTransaction> getTransactions(@RequestParam("startDate") String startDateStr,
                                                     @RequestParam("endDate") String endDateStr) {
        LocalDateTime startDate = LocalDate.parse(startDateStr,Constant.FORMAT).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr,Constant.FORMAT).atStartOfDay();
        return service.findTransactions(startDate, endDate);
    }

    @GetMapping("/{id}/convert")
    public ResponseEntity<?> convertTransactionCurrency(@PathVariable Long id,
                                                        @RequestParam String targetCurrency) {
        try {
            PurchaseTransaction transaction = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));
            PurchaseTransactionConversionDTO convertedTransaction = service.convertTransactionCurrency(transaction, targetCurrency);
            return ResponseEntity.ok(convertedTransaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
