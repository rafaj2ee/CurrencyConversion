package com.rafaj2ee.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.repository.PurchaseTransactionRepository;
import com.rafaj2ee.util.Constant;

@Service
public class PurchaseTransactionService {

    @Autowired
    private PurchaseTransactionRepository repository;

    public PurchaseTransaction saveTransaction(PurchaseTransactionDTO dto) throws Exception {
        PurchaseTransaction transaction = new PurchaseTransaction();
        BeanUtils.copyProperties(transaction, dto);
        return repository.save(transaction);
    }

    public List<PurchaseTransaction> findTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByTransactionDateBetween(startDate, endDate);
    }

    public Optional<PurchaseTransaction> findById(Long id) {
        return repository.findById(id);
    }

    public Map<String, BigDecimal> getCurrencyConversionRates(LocalDateTime transactionDate) {
        RestTemplate restTemplate = new RestTemplate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = transactionDate.format(formatter);

        LocalDateTime sixMonthsAgo = transactionDate.minusMonths(6);
        String sixMonthsAgoFormatted = sixMonthsAgo.format(formatter);

        String apiUrl = String.format(
            Constant.URL,
            formattedDate,
            sixMonthsAgoFormatted
        );

        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        List<Map<String, String>> rates = (List<Map<String, String>>) response.get("data");

        return rates.stream()
            .collect(Collectors.toMap(
                rate -> rate.get("currency"),
                rate -> new BigDecimal(rate.get("exchange_rate")),
                (existing, replacement) -> existing,
                HashMap::new
            ));
    }

    public PurchaseTransactionConversionDTO convertTransactionCurrency(PurchaseTransaction transaction, String targetCurrency) {
        Map<String, BigDecimal> rates = getCurrencyConversionRates(transaction.getTransactionDate());
        BigDecimal conversionRate = rates.get(targetCurrency);

        if (conversionRate == null) {
            throw new IllegalArgumentException("The purchase cannot be converted to the target currency");
        }

        PurchaseTransactionConversionDTO dto = new PurchaseTransactionConversionDTO();
        dto.setDescription(transaction.getDescription());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setAmount(transaction.getAmount().multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP));
        dto.setId(transaction.getId());
        dto.setOriginalAmount(transaction.getAmount());
        dto.setExchangeRate(conversionRate);
        return dto;
    }
}
