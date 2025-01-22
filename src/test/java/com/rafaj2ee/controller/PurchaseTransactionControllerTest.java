package com.rafaj2ee.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.exception.CurrencyConversionException;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.service.PurchaseTransactionService;

@WebMvcTest(PurchaseTransactionController.class)
public class PurchaseTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseTransactionService service;

    @InjectMocks
    private PurchaseTransactionController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testSaveTransaction_ValidInput() throws Exception {
        PurchaseTransactionDTO dto = new PurchaseTransactionDTO();
        dto.setDescription("Test Transaction");
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setTransactionDate("2022-01-01");

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100).setScale(2, BigDecimal.ROUND_HALF_UP));
        transaction.setTransactionDate(LocalDate.parse("2022-01-01").atStartOfDay());

        when(service.saveTransaction(any(PurchaseTransactionDTO.class))).thenReturn(transaction);

        mockMvc.perform(post("/api/v1/purchase-transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Transaction"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    public void testSaveTransaction_InvalidInput() throws Exception {
        PurchaseTransactionDTO dto = new PurchaseTransactionDTO();
        dto.setDescription(""); // Invalid description
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setTransactionDate("2022-01-01");

        mockMvc.perform(post("/api/v1/purchase-transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetTransactions_ValidInput() throws Exception {
        mockMvc.perform(get("/api/v1/purchase-transactions")
                .param("startDate", "2022-01-01")
                .param("endDate", "2022-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetTransactions_InvalidInput() throws Exception {
        mockMvc.perform(get("/api/v1/purchase-transactions")
                .param("endDate", "2022-01-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testConvertTransactionCurrency_ValidInput() throws Exception {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setTransactionDate(LocalDate.parse("2022-01-01").atStartOfDay());

        PurchaseTransactionConversionDTO conversionDTO = new PurchaseTransactionConversionDTO();
        conversionDTO.setDescription("Test Transaction");
        conversionDTO.setAmount(BigDecimal.valueOf(618.40).setScale(2, BigDecimal.ROUND_HALF_UP)); // Valor corrigido para Real
        conversionDTO.setExchangeRate(BigDecimal.valueOf(6.184)); // Exemplo de taxa de câmbio atualizada

        when(service.findById(1L)).thenReturn(Optional.of(transaction));
        when(service.convertTransactionCurrency(eq(transaction), eq("Real"), eq(""))).thenReturn(conversionDTO);

        mockMvc.perform(get("/api/v1/purchase-transactions/1/convert")
                .param("currency", "Real")
                .param("country", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Transaction"))
                .andExpect(jsonPath("$.amount").value(618.40));
    }

    @Test
    public void testConvertTransactionCurrency_InvalidCurrency() throws Exception {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setTransactionDate(LocalDate.parse("2022-01-01").atStartOfDay());

        when(service.findById(1L)).thenReturn(Optional.of(transaction));
        when(service.convertTransactionCurrency(eq(transaction), eq("XYZ"), eq(""))).thenThrow(new CurrencyConversionException("The purchase cannot be converted to the target currency"));

        mockMvc.perform(get("/api/v1/purchase-transactions/1/convert")
                .param("currency", "XYZ")
                .param("country", "ASD"))
                .andExpect(status().isBadRequest());
    }
}
