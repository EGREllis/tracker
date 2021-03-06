package net.finance.tracker.domain.axis;

import net.finance.tracker.domain.series.Series;

import java.math.BigDecimal;
import java.util.Date;

public class OpenAxisAdapter implements Axis {
    private final Series series;

    public OpenAxisAdapter(Series series) {
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
        return series.getOpen(i);
    }

    @Override
    public Date getDate(int i) {
        return series.getDate(i);
    }
}
