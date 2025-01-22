package com.rafaj2ee.DTO;

import java.math.BigDecimal;

public class PurchaseTransactionConversionDTO extends PurchaseTransactionDTO {
	private Long id;
	private BigDecimal originalAmount;
	private BigDecimal exchangeRate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public BigDecimal getOriginalAmount() {
		return originalAmount;
	}
	public void setOriginalAmount(BigDecimal originalAmount) {
		this.originalAmount = originalAmount;
	}
	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	
}
