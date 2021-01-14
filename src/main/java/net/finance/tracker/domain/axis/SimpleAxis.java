package net.finance.tracker.domain.axis;

import java.math.BigDecimal;
import java.util.Date;

public class SimpleAxis implements Axis {
    private final String symbol;
    private final BigDecimal[] data;
    private final Date[] dates;

    public SimpleAxis(String symbol, BigDecimal[] data, Date[] dates) {
        this.symbol = symbol;
        this.data = data;
        this.dates = dates;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public int getLength() {
        return dates.length;
    }

    @Override
    public BigDecimal getValue(int i) {
        return data[i];
    }

    @Override
    public Date getDate(int i) {
        return dates[i];
    }
}
