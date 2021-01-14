package net.finance.tracker.domain.axis;

import java.math.BigDecimal;
import java.util.Date;

public interface Axis {
    String getSymbol();
    int getLength();
    BigDecimal getValue(int i);
    Date getDate(int i);
}
