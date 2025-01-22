package com.rafaj2ee.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.rafaj2ee.DTO.PurchaseTransactionConversionDTO;
import com.rafaj2ee.DTO.PurchaseTransactionDTO;
import com.rafaj2ee.exception.CurrencyConversionException;
import com.rafaj2ee.model.PurchaseTransaction;
import com.rafaj2ee.repository.PurchaseTransactionRepository;
import com.rafaj2ee.util.Constant;

@Service
public class PurchaseTransactionService {

    @Autowired
    private PurchaseTransactionRepository repository;

    public PurchaseTransaction saveTransaction(PurchaseTransactionDTO dto) throws Exception {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setTransactionDate(LocalDate.parse(dto.getTransactionDate(), Constant.FORMAT).atStartOfDay());
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        return repository.save(transaction);
    }

    public List<PurchaseTransaction> findTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByTransactionDateBetween(startDate, endDate);
    }

    public Optional<PurchaseTransaction> findById(Long id) {
        return repository.findById(id);
    }

    public Map<String, BigDecimal> getCurrencyConversionRates(LocalDateTime transactionDate, String currency, String country) {
        RestTemplate restTemplate = new RestTemplate();
        String formattedDate = transactionDate.format(Constant.FORMAT);

        LocalDateTime sixMonthsAgo = transactionDate.minusMonths(6);
        String sixMonthsAgoFormatted = sixMonthsAgo.format(Constant.FORMAT);

        String apiUrl = String.format(
            Constant.URL,
            formattedDate,
            sixMonthsAgoFormatted,
            currency
        );
        if(country!=null && !country.isEmpty()) {
        	apiUrl = apiUrl + ",country:eq:"+ country;
        }
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

    public PurchaseTransactionConversionDTO convertTransactionCurrency(PurchaseTransaction transaction, String targetCurrency, String country) {
        Map<String, BigDecimal> rates = getCurrencyConversionRates(transaction.getTransactionDate(), targetCurrency, country);
        BigDecimal conversionRate = rates.get(targetCurrency);

        if (conversionRate == null) {
            throw new CurrencyConversionException(Constant.CUSTOM_ERROR);
        }

        PurchaseTransactionConversionDTO dto = new PurchaseTransactionConversionDTO();
        dto.setDescription(transaction.getDescription());
        dto.setTransactionDate(transaction.getTransactionDate().format(Constant.FORMAT));
        dto.setAmount(transaction.getAmount().multiply(conversionRate).setScale(2, BigDecimal.ROUND_HALF_UP));
        dto.setId(transaction.getId());
        dto.setOriginalAmount(transaction.getAmount());
        dto.setExchangeRate(conversionRate);
        return dto;
    }

}
