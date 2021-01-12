package net.finance.tracker.domain;

import net.finance.tracker.pattern.Builder;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockSeries implements Series {
    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final int DEFAULT_LINE_SIZE = 100;
    private final String symbol;
    private final Date[] date;
    private final BigDecimal[] open;
    private final BigDecimal[] high;
    private final BigDecimal[] low;
    private final BigDecimal[] close;
    private final BigDecimal[] adjClose;
    private final Long[] volume;

    public StockSeries(String symbol, Date[] date, BigDecimal[] open, BigDecimal[] high, BigDecimal[] low, BigDecimal[] close, BigDecimal[] adjClose, Long[] volume) {
        this.symbol = symbol;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.adjClose = adjClose;
        this.volume = volume;
    }

    public int getLength() {
        return date.length;
    }

    public String getSymbol() {
        return symbol;
    }

    public Date getDate(int i) {
        return date[i];
    }

    public BigDecimal getOpen(int i) {
        return open[i];
    }

    public BigDecimal getHigh(int i) {
        return high[i];
    }

    public BigDecimal getLow(int i) {
        return low[i];
    }

    public BigDecimal getClose(int i) {
        return close[i];
    }

    public BigDecimal getAdjClose(int i) {
        return adjClose[i];
    }

    public Long getVolume(int i) {
        return volume[i];
    }

    @Override
    public String toString() {
        if (date.length > 0) {
            return String.format("%1$d records for stock %2$s starting from %3$s to %4$s", getLength(), symbol, SIMPLE_DATE_FORMAT.format(date[0]), SIMPLE_DATE_FORMAT.format(date[getLength() - 1]));
        } else {
            return String.format("%1$d records for stock %2$s, no dates", getLength(), symbol);
        }
    }

    public static class StockSeriesBuilder implements Builder<Series> {
        private final String symbol;
        private List<Object[]> dataLines = new ArrayList<>(DEFAULT_LINE_SIZE);

        public StockSeriesBuilder(String symbol) {
            this.symbol = symbol;
        }

        public void addLine(Date date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal adjClose, Long volume) {
            dataLines.add(new Object[] {date, open, high, low, close, adjClose, volume});
        }

        @Override
        public Series build() {
            int nRecord = dataLines.size();
            Date[] date = new Date[nRecord];
            BigDecimal[] open = new BigDecimal[nRecord];
            BigDecimal[] high = new BigDecimal[nRecord];
            BigDecimal[] low = new BigDecimal[nRecord];
            BigDecimal[] close = new BigDecimal[nRecord];
            BigDecimal[] adjClose = new BigDecimal[nRecord];
            Long[] volume = new Long[nRecord];

            for (int i = 0; i < dataLines.size(); i++) {
                Object[] line = dataLines.get(i);
                date[i] = (Date)line[0];
                open[i] = (BigDecimal)line[1];
                high[i] = (BigDecimal)line[2];
                low[i] = (BigDecimal)line[3];
                close[i] = (BigDecimal)line[4];
                adjClose[i] = (BigDecimal)line[5];
                volume[i] = (Long)line[6];
            }

            return new StockSeries(symbol, date, open, high, low, close, adjClose, volume);
        }
    }
}
