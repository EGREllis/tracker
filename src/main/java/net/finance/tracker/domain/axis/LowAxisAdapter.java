package net.finance.tracker.domain.axis;

import net.finance.tracker.domain.Series;

import java.math.BigDecimal;
import java.util.Date;

public class LowAxisAdapter implements Axis {
    private final Series series;

    public LowAxisAdapter(Series series) {
        this.series = series;
    }

    @Override
    public String getSymbol() {
        return series.getSymbol();
    }

    @Override
    public int getLength() {
        return series.getLength();
    }

    @Override
    public BigDecimal getValue(int i) {
        return series.getLow(i);
    }

    @Override
    public Date getDate(int i) {
        return series.getDate(i);
    }
}
