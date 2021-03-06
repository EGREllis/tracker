package net.finance.tracker.domain.calculation;

import java.math.BigDecimal;

public interface CorrelationMatrix {
    int size();
    String getAxisLabel(int i);
    BigDecimal getCell(int i, int j);
}
