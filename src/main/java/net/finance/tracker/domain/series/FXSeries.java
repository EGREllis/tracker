package net.finance.tracker.domain.series;

import net.finance.tracker.util.pattern.Builder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FXSeries implements Series {
    private final String symbol;
    private final Date[] date;
    private final BigDecimal[] open;
    private final BigDecimal[] close;
    private final BigDecimal[] high;
    private final BigDecimal[] low;
    private final BigDecimal[] adjClose;
    private final int nDataQualityIssues;

    private FXSeries(String symbol, Date[] date, BigDecimal[] open, BigDecimal[] close, BigDecimal[] high, BigDecimal[] low, BigDecimal[] adjClose, int nDataQualityIssues) {
        this.symbol = symbol;
        this.date = date;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.adjClose = adjClose;
        this.nDataQualityIssues = nDataQualityIssues;
    }

    @Override
    public int getNDataQualityIssues() {
        return nDataQualityIssues;
    }

    @Override
    public int getLength() {
        return date.length;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public Date getDate(int i) {
        return date[i];
    }

    @Override
    public BigDecimal getOpen(int i) {
        return open[i];
    }

    @Override
    public BigDecimal getClose(int i) {
        return close[i];
    }

    @Override
    public BigDecimal getHigh(int i) {
        return high[i];
    }

    @Override
    public BigDecimal getLow(int i) {
        return low[i];
    }

    @Override
    public BigDecimal getAdjClose(int i) {
        return adjClose[i];
    }

    @Override
    public String toString() {
        return String.format("An Fx series for symbol %1$s with %2$d records", symbol, getLength());
    }

    public static class FXSeriesBuilder implements Builder<FXSeries> {
        private final List<Object[]> data = new ArrayList<>();
        private final String symbol;
        private final List<Exception> dataQualityIssues = new ArrayList<>();

        public FXSeriesBuilder(String symbol) {
            this.symbol = symbol;
        }

        public void addRow(Date date, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal adjClose) {
            data.add(new Object[]{date, open, close, high, low, adjClose});
        }

        public void addDataQualityIssue(Exception e) {
            dataQualityIssues.add(e);
        }

        public String getSummary() {
            return String.format("Loaded %1$d lines for %2$s with %3$d data issues", data.size(), symbol, dataQualityIssues.size());
        }

        @Override
        public FXSeries build() {
            Date[] dates = new Date[data.size()];
            BigDecimal[] open = new BigDecimal[data.size()];
            BigDecimal[] close = new BigDecimal[data.size()];
            BigDecimal[] high = new BigDecimal[data.size()];
            BigDecimal[] low = new BigDecimal[data.size()];
            BigDecimal[] adjClose = new BigDecimal[data.size()];
            for (int i = 0; i < data.size(); i++) {
                Object[] row = data.get(i);
                dates[i] = (Date)row[0];
                open[i] = (BigDecimal)row[1];
                close[i] = (BigDecimal)row[2];
                high[i] = (BigDecimal)row[3];
                low[i] = (BigDecimal)row[4];
                adjClose[i] = (BigDecimal)row[5];
            }
            return new FXSeries(symbol, dates, open, close, high, low, adjClose, dataQualityIssues.size());
        }
    }
}
