package net.finance.tracker.domain.series;

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

    public String getSummary() {
        return getSummary(stocks, "Stocks") + "\n"+ getSummary(rates, "Rates");
    }

    private String getSummary(Map<String,Series> seriesMap, String label) {
        long records = 0;
        long issues = 0;
        for (Series series : seriesMap.values()) {
            records += series.getLength();
            issues += series.getNDataQualityIssues();
        }
        return String.format("%1$s loaded %2$d records with %3$d issues", label, records, issues);
    }
}
