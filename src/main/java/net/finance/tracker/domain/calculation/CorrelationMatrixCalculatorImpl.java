package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.pattern.Listener;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.*;

public class CorrelationMatrixCalculatorImpl implements CorrelationMatrixCalculator {
    private final Listener<Exception> exceptionListener;
    private final MathContext mathContext;

    public CorrelationMatrixCalculatorImpl(Listener<Exception> exceptionListener, MathContext mathContext) {
        this.exceptionListener = exceptionListener;
        this.mathContext = mathContext;
    }

    @Override
    public CorrelationMatrix calculate(List<Axis> axes, ExecutorService service) {
        List<String> labels = buildAxes(axes);
        BigDecimal[][] data = buildEmptyData(axes.size());

        Map<String,DescriptiveStatistics> stats = calculateDescriptiveStatistics(axes, service);
        populateCorrelationMatrix(data, labels, stats, service);

        return new CorrelationMatrixImpl(labels, data);
    }

    private List<String> buildAxes(List<Axis> axes) {
        List<String> labels = new ArrayList<>(axes.size());
        for (Axis axis : axes) {
            labels.add(axis.getSymbol());
        }
        Collections.sort(labels);
        return labels;
    }

    private BigDecimal[][] buildEmptyData(int size) {
        BigDecimal[][] data = new BigDecimal[size][];
        for (int i = 0; i < size; i++) {
            data[i] = new BigDecimal[size];
        }
        return data;
    }

    private Map<String, DescriptiveStatistics> calculateDescriptiveStatistics(List<Axis> axes, ExecutorService service) {
        Map<String, DescriptiveStatistics> stats = new ConcurrentHashMap<>();
        List<Future<DescriptiveStatistics>> futures = new ArrayList<>(axes.size());
        for (Axis axis : axes) {
            futures.add(service.submit(new DescriptiveStatistics.DescriptiveStatisticBuilder(axis, MathContext.DECIMAL64)));
        }
        for (Future<DescriptiveStatistics> calc : futures) {
            try {
                DescriptiveStatistics stat = calc.get();
                stats.put(stat.getAxis().getSymbol(), stat);
            } catch (InterruptedException | ExecutionException e) {
                exceptionListener.listen(e);
            }
        }
        return stats;
    }

    private void populateCorrelationMatrix(BigDecimal[][] data, List<String> axes, Map<String, DescriptiveStatistics> stats, ExecutorService service) {

    }

    static class CorrelationResult {
        private final MathContext mathContext;
        private final BigDecimal result;
        private final String symbolX;
        private final String symbolY;

        public CorrelationResult(BigDecimal result, String symbolX, String symbolY, MathContext mathContext) {
            this.mathContext = mathContext;
            this.result = result;
            this.symbolX = symbolX;
            this.symbolY = symbolY;
        }

        public BigDecimal getResult() {
            return result;
        }

        public String getSymbolX() {
            return symbolX;
        }

        public String getSymbolY() {
            return symbolY;
        }
    }

    static class CorrelationCalculator implements Callable<CorrelationResult> {
        private final MathContext mathContext;
        private final DescriptiveStatistics xAxis;
        private final DescriptiveStatistics yAxis;

        public CorrelationCalculator(DescriptiveStatistics xAxis, DescriptiveStatistics yAxis, MathContext mathContext) {
            this.mathContext = mathContext;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }

        @Override
        public CorrelationResult call() throws Exception {
            // We can only calculate correlation where we have a pair of values.
            BigDecimal tally = new BigDecimal(0);
            int maxIndex = Math.min(xAxis.getLength(), yAxis.getLength());
            int xIndex = maxIndex-1;
            int yIndex = maxIndex-1;
            int points = 0;
            while (xIndex >= 0 && yIndex >= 0) {
                long xTime = xAxis.getDate(xIndex).getTime();
                long yTime = yAxis.getDate(yIndex).getTime();
                while (xTime < yTime && xIndex >= 0) {
                    xTime = xAxis.getDate(--xIndex).getTime();
                }
                while (yTime < xTime && yIndex >= 0) {
                    yTime = yAxis.getDate(--yIndex).getTime();
                }
                if (xTime == yTime && xIndex >= 0 && yIndex >= 0) {
                    BigDecimal stdX = xAxis.getMean().subtract(xAxis.getValue(xIndex)).divide(xAxis.getStandardDeviation(), mathContext);
                    BigDecimal stdY = yAxis.getMean().subtract(yAxis.getValue(yIndex)).divide(yAxis.getStandardDeviation(), mathContext);
                    tally = tally.add(stdX.multiply(stdY));
                    points++;
                    xIndex++;
                    yIndex++;
                }
            }
            return new CorrelationResult(tally.divide(new BigDecimal(points -1), mathContext), xAxis.getSymbol(), yAxis.getSymbol(), mathContext);
        }
    }
}
