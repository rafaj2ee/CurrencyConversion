package com.rafaj2ee.util;

import java.time.format.DateTimeFormatter;

public class Constant {
	public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final String URL = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?sort=-record_date&format=json&page[number]=1&page[size]=10&filter=record_date:lte:%s,record_date:gte:%s";
}
