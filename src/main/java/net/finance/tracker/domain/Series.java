package net.finance.tracker.domain;

import java.math.BigDecimal;
import java.util.Date;

public interface Series {
    int getLength();
    String getSymbol();
    Date getDate(int i);
    BigDecimal getOpen(int i);
    BigDecimal getClose(int i);
    BigDecimal getHigh(int i);
    BigDecimal getLow(int i);
    BigDecimal getAdjClose(int i);
}
