package net.finance.tracker.domain.calculation;

import java.math.BigDecimal;
import java.util.List;

public class CorrelationMatrixImpl implements CorrelationMatrix {
    private final List<String> symbols;
    private final BigDecimal data[][];

    public CorrelationMatrixImpl(List<String> symbols, BigDecimal[][] data) {
        this.symbols = symbols;
        this.data = data;
    }

    @Override
    public int size() {
        return symbols.size();
    }

    @Override
    public String getAxisLabel(int i) {
        return symbols.get(i);
    }

    @Override
    public BigDecimal getCell(int x, int y) {
        return data[y][x];
    }
}
