package com.rafaj2ee.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.rafaj2ee.util.Constant;

public class PurchaseTransactionDTO {
    private String description;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = LocalDate.parse(transactionDate,Constant.FORMAT).atStartOfDay();
	}

}
