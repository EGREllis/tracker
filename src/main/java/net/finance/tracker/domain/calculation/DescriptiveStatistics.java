package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.concurrent.Callable;

public class DescriptiveStatistics implements Axis {
    private final Axis axis;
    private final BigDecimal mean;
    private final BigDecimal standardDeviation;

    public DescriptiveStatistics(Axis axis, BigDecimal mean, BigDecimal standardDeviation) {
        this.axis = axis;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    public Axis getAxis() {
        return axis;
    }

    public BigDecimal getMean() {
        return mean;
    }

    public BigDecimal getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public String getSymbol() {
        return axis.getSymbol();
    }

    @Override
    public int getLength() {
        return axis.getLength();
    }

    @Override
    public BigDecimal getValue(int i) {
        return axis.getValue(i);
    }

    @Override
    public Date getDate(int i) {
        return axis.getDate(i);
    }

    public static class DescriptiveStatisticBuilder implements Callable<DescriptiveStatistics> {
        private MathContext mathContext;
        private Axis axis;

        public DescriptiveStatisticBuilder(Axis axis, MathContext mathContext) {
            this.axis = axis;
            this.mathContext = mathContext;
        }

        private BigDecimal calculateMean() {
            BigDecimal total = axis.getValue(0);
            for (int i = 1; i < axis.getLength(); i++) {
                total = total.add(axis.getValue(i));
            }
            return total.divide(new BigDecimal(axis.getLength()-1), mathContext);
        }

        private BigDecimal calculateStandardDeviation(BigDecimal mean) {
            BigDecimal tally = mean.subtract(axis.getValue(0)).pow(2);
            for (int i = 0; i < axis.getLength(); i++) {
                tally.add(mean.subtract(axis.getValue(i)).pow(2));
            }
            return tally.sqrt(mathContext);
        }

        @Override
        public DescriptiveStatistics call() throws Exception {
            BigDecimal mean = calculateMean();
            BigDecimal stddev = calculateStandardDeviation(mean);
            return new DescriptiveStatistics(axis, mean, stddev);
        }
    }
}
