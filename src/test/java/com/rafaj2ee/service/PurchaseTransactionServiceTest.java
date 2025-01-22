package com.rafaj2ee.service;

import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PurchaseTransactionServiceTest {

    @Mock
    private PurchaseTransactionRepository repository;

    @InjectMocks
    private PurchaseTransactionService service;

    public PurchaseTransactionServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveTransaction() throws Exception {
        PurchaseTransactionDTO dto = new PurchaseTransactionDTO();
        dto.setDescription("Test Transaction");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setTransactionDate(LocalDateTime.now());

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setTransactionDate(LocalDateTime.now());

        when(repository.save(any(PurchaseTransaction.class))).thenReturn(transaction);

        PurchaseTransaction savedTransaction = service.saveTransaction(dto);

        assertEquals("Test Transaction", savedTransaction.getDescription());
        verify(repository, times(1)).save(any(PurchaseTransaction.class));
    }

    @Test
    public void testFindTransactions() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6);
        LocalDateTime endDate = LocalDateTime.now();

        PurchaseTransaction transaction1 = new PurchaseTransaction();
        transaction1.setDescription("Transaction 1");
        transaction1.setAmount(BigDecimal.valueOf(50.00));
        transaction1.setTransactionDate(startDate.plusDays(1));

        PurchaseTransaction transaction2 = new PurchaseTransaction();
        transaction2.setDescription("Transaction 2");
        transaction2.setAmount(BigDecimal.valueOf(75.00));
        transaction2.setTransactionDate(startDate.plusDays(2));

        List<PurchaseTransaction> transactions = Arrays.asList(transaction1, transaction2);

        when(repository.findByTransactionDateBetween(startDate, endDate)).thenReturn(transactions);

        List<PurchaseTransaction> result = service.findTransactions(startDate, endDate);

        assertEquals(2, result.size());
        assertEquals("Transaction 1", result.get(0).getDescription());
        assertEquals("Transaction 2", result.get(1).getDescription());
    }

    @Test
    public void testGetCurrencyConversionRates() {
        LocalDateTime transactionDate = LocalDateTime.parse("2024-12-31T00:00:00");
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Transaction 1");
        transaction.setAmount(BigDecimal.valueOf(50.00));
        transaction.setTransactionDate(transactionDate);

        // Mock the API response
        Map<String, BigDecimal> mockRates = new HashMap<>();
        mockRates.put("USD", BigDecimal.valueOf(6.184));
        PurchaseTransactionService serviceSpy = spy(service);
        doReturn(mockRates).when(serviceSpy).getCurrencyConversionRates(transactionDate);

        Map<String, BigDecimal> rates = serviceSpy.getCurrencyConversionRates(transactionDate);

        assertEquals(1, rates.size());
        assertEquals(BigDecimal.valueOf(6.184), rates.get("USD"));
    }

    @Test
    public void testConvertTransactionCurrency() {
        LocalDateTime transactionDate = LocalDateTime.parse("2024-12-31T00:00:00");
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Transaction 1");
        transaction.setAmount(BigDecimal.valueOf(50.00));
        transaction.setTransactionDate(transactionDate);

        // Mock the API response
        Map<String, BigDecimal> mockRates = new HashMap<>();
        mockRates.put("USD", BigDecimal.valueOf(6.184));
        PurchaseTransactionService serviceSpy = spy(service);
        doReturn(mockRates).when(serviceSpy).getCurrencyConversionRates(transactionDate);

        PurchaseTransactionDTO convertedDTO = serviceSpy.convertTransactionCurrency(transaction, "USD");

        assertEquals("Transaction 1", convertedDTO.getDescription());
        assertEquals(transactionDate, convertedDTO.getTransactionDate());
        assertEquals(BigDecimal.valueOf(50.00).multiply(BigDecimal.valueOf(6.184)), convertedDTO.getAmount());
    }
}
