package com.rafaj2ee.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.exception.CurrencyConversionException;
import com.rafaj2ee.exception.ErrorResponse;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.service.PurchaseTransactionService;
import com.rafaj2ee.util.Constant;

@Validated
@RestController
@RequestMapping("/api/v1/purchase-transactions")
public class PurchaseTransactionController {

    @Autowired
    private PurchaseTransactionService service;

    @PostMapping
    public PurchaseTransaction saveTransaction(@Valid @RequestBody PurchaseTransactionDTO dto) throws Exception {
        return service.saveTransaction(dto);
    }

    @GetMapping
    public List<PurchaseTransaction> getTransactions(
        @RequestParam("startDate")
        @NotBlank(message = "Start date cannot be blank.")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in the format 'yyyy-MM-dd'.")
        String startDate,

        @RequestParam("endDate")
        @NotBlank(message = "End date cannot be blank.")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in the format 'yyyy-MM-dd'.")
        String endDate
    ) {
        LocalDateTime startDateTime = LocalDate.parse(startDate, Constant.FORMAT).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate, Constant.FORMAT).atStartOfDay();
        return service.findTransactions(startDateTime, endDateTime);
    }

    @GetMapping("/{id}/convert")
    public ResponseEntity<?> convertTransactionCurrency(@PathVariable Long id,
                                                        @RequestParam String currency,
                                                        @RequestParam(required = false) String country) {
        try {
            PurchaseTransaction transaction = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));
            PurchaseTransactionConversionDTO convertedTransaction = service.convertTransactionCurrency(transaction, currency, country);
            if (convertedTransaction == null) {
                throw new CurrencyConversionException(Constant.CUSTOM_ERROR);
            }
            return ResponseEntity.ok(convertedTransaction);
        } catch (CurrencyConversionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Currency Conversion Error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Illegal Argument", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An unexpected error occurred"));
        }
    }
}
