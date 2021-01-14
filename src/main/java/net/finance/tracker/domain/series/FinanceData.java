package net.finance.tracker.domain.series;

import net.finance.tracker.domain.series.Series;

import java.util.Collections;
import java.util.Map;

public class FinanceData {
    private final Map<String, Series> stocks;
    private final Map<String, Series> rates;

    public FinanceData(Map<String, Series> stocks, Map<String, Series> rates) {
        this.stocks = stocks;
        this.rates = rates;
    }

    public Map<String,Series> getStocks() {
        return Collections.unmodifiableMap(stocks);
    }

    public Map<String,Series> getRates() {
        return Collections.unmodifiableMap(rates);
    }
}
