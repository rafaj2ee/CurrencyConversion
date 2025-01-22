package com.rafaj2ee.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.service.PurchaseTransactionService;

public class PurchaseTransactionControllerTest {

    @Mock
    private PurchaseTransactionService service;

    @InjectMocks
    private PurchaseTransactionController controller;

    private MockMvc mockMvc;

    public PurchaseTransactionControllerTest() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testSaveTransaction() throws Exception {
        PurchaseTransactionDTO dto = new PurchaseTransactionDTO();
        dto.setDescription("Test Transaction");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setTransactionDate(LocalDateTime.now());

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setTransactionDate(LocalDateTime.now());

        when(service.saveTransaction(dto)).thenReturn(transaction);

        mockMvc.perform(post("/api/v1/purchase-transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"description\": \"Test Transaction\", \"amount\": 100.00, \"transactionDate\": \"2025-01-21T10:15:30\" }"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"description\":\"Test Transaction\",\"amount\":100.00,\"transactionDate\":\"2025-01-21T10:15:30\"}"));
    }

    @Test
    public void testGetTransactions() throws Exception {
        LocalDateTime startDate = LocalDateTime.parse("2024-07-01T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse("2024-12-31T23:59:59");

        PurchaseTransaction transaction1 = new PurchaseTransaction();
        transaction1.setDescription("Transaction 1");
        transaction1.setAmount(BigDecimal.valueOf(50.00));
        transaction1.setTransactionDate(startDate.plusDays(1));

        PurchaseTransaction transaction2 = new PurchaseTransaction();
        transaction2.setDescription("Transaction 2");
        transaction2.setAmount(BigDecimal.valueOf(75.00));
        transaction2.setTransactionDate(startDate.plusDays(2));

        List<PurchaseTransaction> transactions = Arrays.asList(transaction1, transaction2);

        when(service.findTransactions(startDate, endDate)).thenReturn(transactions);

        mockMvc.perform(get("/api/v1/purchase-transactions")
                .param("startDate", "2024-07-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"description\":\"Transaction 1\",\"amount\":50.00,\"transactionDate\":\"2024-07-02T00:00:00\"},{\"description\":\"Transaction 2\",\"amount\":75.00,\"transactionDate\":\"2024-07-03T00:00:00\"}]"));
    }

    @Test
    public void testConvertTransactionCurrency() throws Exception {
        LocalDateTime transactionDate = LocalDateTime.parse("2024-12-31T10:15:30");

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setTransactionDate(transactionDate);

        PurchaseTransactionConversionDTO convertedDTO = new PurchaseTransactionConversionDTO();
        convertedDTO.setDescription("Test Transaction");
        convertedDTO.setAmount(BigDecimal.valueOf(618.40));  // Assuming exchange rate is 6.184
        convertedDTO.setTransactionDate(transactionDate);

        when(service.findById(1L)).thenReturn(Optional.of(transaction));
        when(service.convertTransactionCurrency(transaction, "USD")).thenReturn(convertedDTO);

        mockMvc.perform(get("/api/v1/purchase-transactions/1/convert")
                .param("targetCurrency", "USD"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"description\":\"Test Transaction\",\"amount\":618.40,\"transactionDate\":\"2024-12-31T10:15:30\"}"));
    }
}
