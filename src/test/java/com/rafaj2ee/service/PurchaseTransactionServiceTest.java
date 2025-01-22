package com.rafaj2ee.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.exception.CurrencyConversionException;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.repository.PurchaseTransactionRepository;
import com.rafaj2ee.util.Constant;

public class PurchaseTransactionServiceTest {

    @InjectMocks
    private PurchaseTransactionService service;

    @Mock
    private PurchaseTransactionRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveTransaction() throws Exception {
        PurchaseTransactionDTO dto = new PurchaseTransactionDTO();
        dto.setTransactionDate("2023-01-01");
        dto.setDescription("Test transaction");
        dto.setAmount(BigDecimal.valueOf(100));

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setTransactionDate(LocalDate.parse("2023-01-01", Constant.FORMAT).atStartOfDay());
        transaction.setDescription("Test transaction");
        transaction.setAmount(BigDecimal.valueOf(100));

        when(repository.save(any(PurchaseTransaction.class))).thenReturn(transaction);

        PurchaseTransaction savedTransaction = service.saveTransaction(dto);

        assertNotNull(savedTransaction);
        assertEquals("Test transaction", savedTransaction.getDescription());
        assertEquals(BigDecimal.valueOf(100), savedTransaction.getAmount());
    }

    @Test
    public void testFindTransactions() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        service.findTransactions(startDate, endDate);

        verify(repository, times(1)).findByTransactionDateBetween(startDate, endDate);
    }

    @Test
    public void testFindById() {
        Long id = 1L;
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(transaction));

        Optional<PurchaseTransaction> foundTransaction = service.findById(id);

        assertTrue(foundTransaction.isPresent());
        assertEquals(id, foundTransaction.get().getId());
    }

    @Test
    public void testConvertTransactionCurrency() {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Test transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setId(1L);

        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> rates = Arrays.asList(
            new HashMap<String, String>() {{
                put("currency", "Dollar");
                put("exchange_rate", "1.2");
            }}
        );
        response.put("data", rates);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        PurchaseTransactionConversionDTO conversionDTO = service.convertTransactionCurrency(transaction, "Dollar", null);

        assertNotNull(conversionDTO);
        assertEquals("Test transaction", conversionDTO.getDescription());
        assertEquals(BigDecimal.valueOf(161.20).setScale(2, BigDecimal.ROUND_HALF_UP), conversionDTO.getAmount());
        assertEquals(BigDecimal.valueOf(100).setScale(2, BigDecimal.ROUND_HALF_UP), conversionDTO.getOriginalAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(BigDecimal.valueOf(1.612), conversionDTO.getExchangeRate());
    }

    @Test
    public void testConvertTransactionCurrencyThrowsException() {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Test transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setId(1L);

        Map<String, Object> response = new HashMap<>();
        response.put("data", Arrays.asList());

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThrows(CurrencyConversionException.class, () -> {
            service.convertTransactionCurrency(transaction, "test", null);
        });
    }
}
