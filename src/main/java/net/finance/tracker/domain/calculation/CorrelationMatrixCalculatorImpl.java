package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.util.pattern.Listener;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.*;

/**
 * This needs a significant re-write:
 *  1) We only calculate correlations for dates present on both Axes
 *  2) The mean and standard deviation are based on the complete sample
 *  3) The mean and standard deviation are based on a different population from the correlation
 *
 * Solutions:
 *  1) Add a filter step before the call to the correlation matrix calculator (weak - a coder that forgets this issue could still fall foul of the bug)
 *  2) Modify correlation matrix calculator to receive Axes, filter them, calculate descriptive statistics then calculate correlations
 */
public class CorrelationMatrixCalculatorImpl implements CorrelationMatrixCalculator {
    private final Listener<Exception> exceptionListener;
    private final AxisCleaner axisCleaner;
    private final MathContext mathContext;

    public CorrelationMatrixCalculatorImpl(Listener<Exception> exceptionListener, AxisCleaner axisCleaner, MathContext mathContext) {
        this.exceptionListener = exceptionListener;
        this.axisCleaner = axisCleaner;
        this.mathContext = mathContext;
    }

    @Override
    public CorrelationMatrix calculate(List<Axis> axes, ExecutorService service) {
        try {
            long startTime = System.currentTimeMillis();
            int nCorrelations = correlationsToCalculate(axes.size());
            System.out.println(String.format("Calculating %1$dx%1$d matrix using %2$d correlations", axes.size(), nCorrelations));
            List<String> labels = buildAxes(axes);
            BigDecimal[][] data = buildEmptyData(axes.size());

            populateCorrelationMatrix(data, axes, service);
            long stopTime = System.currentTimeMillis();
            System.out.println(String.format("Calculated %1$d correlations in %2$f/s", nCorrelations, (stopTime - startTime) / 1000.0));

            return new CorrelationMatrixImpl(labels, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> buildAxes(List<Axis> axes) {
        List<String> labels = new ArrayList<>(axes.size());
        for (Axis axis : axes) {
            labels.add(axis.getSymbol());
        }
        return labels;
    }

    private BigDecimal[][] buildEmptyData(int size) {
        BigDecimal[][] data = new BigDecimal[size][];
        for (int i = 0; i < size; i++) {
            data[i] = new BigDecimal[size];
        }
        return data;
    }

    public int correlationsToCalculate(int axesSize) {
        return ((int)Math.pow(axesSize, 2) - axesSize) / 2;
    }

    private void populateCorrelationMatrix(BigDecimal[][] data, List<Axis> axes, ExecutorService service) throws Exception {
        long startTime = System.currentTimeMillis();
        int numberOfTasks = correlationsToCalculate(axes.size());

        CountDownLatch latch = new CountDownLatch(numberOfTasks);
        Set<String> missingSymbols = new TreeSet<>();
        List<Future<CorrelationResult>> futures = new ArrayList<>(numberOfTasks);
        System.out.println(String.format("Dispatching correlation calculation tasks, expecting %1$d", numberOfTasks));
        long tasks = 0;
        for (int y = 0; y < axes.size(); y++) {
            for (int x = y + 1; x < axes.size(); x++) {
                CleanedAxes cleaned = axisCleaner.cleanAxes(axes.get(x), axes.get(y));
                DescriptiveStatistics xStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(cleaned.getAClean(), mathContext).call();
                DescriptiveStatistics yStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(cleaned.getBClean(), mathContext).call();
                try {
                    Callable<CorrelationResult> calc = new CorrelationMatrixCalculatorImpl.CorrelationCalculator(xStats, yStats, mathContext, latch);
                    futures.add(service.submit(calc));
                    tasks++;
                } catch (CanNotCalculateException e) {
                    exceptionListener.listen(e);
                }
            }
        }
        System.out.println(String.format("Dispatched %1$d tasks of the expected %2$d", tasks, numberOfTasks));
        System.out.flush();

        for (String missing : missingSymbols) {
            System.err.println(String.format("Could not find descriptive statistics for %1$s", missing));
        }
        System.err.flush();

        /*
        try {
            System.out.println(String.format("Main thread parked, awaiting %1$d calculation tasks", tasks));
            latch.await();
        } catch (InterruptedException ie) {
            throw new RuntimeException("Calculation was interrupted", ie);
        }
         */

        for (Future<CorrelationResult> calculatedResult : futures) {
            CorrelationResult result = null;
            try {
                result = calculatedResult.get();
                if (result == null) {
                    System.out.println("Future returned a null result!");
                }
                int xIndex = axes.indexOf(result.symbolX);
                int yIndex = axes.indexOf(result.symbolY);
                data[yIndex][xIndex] = result.getResult();
                data[xIndex][yIndex] = result.getResult();
            } catch (InterruptedException | ExecutionException e) {
                exceptionListener.listen(e);
            }
        }
        long stopTime = System.currentTimeMillis();
        System.out.println(String.format("Calculated %1$d correlations in %2$f/s", numberOfTasks, (stopTime - startTime)/1000.0));
    }

    static class CorrelationResult {
        private final BigDecimal result;
        private final String symbolX;
        private final String symbolY;

        public CorrelationResult(BigDecimal result, String symbolX, String symbolY) {
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
        private static final int LOG_THRESHOLD = 100;
        private final CountDownLatch latch;
        private final MathContext mathContext;
        private final DescriptiveStatistics xAxis;
        private final DescriptiveStatistics yAxis;

        public CorrelationCalculator(DescriptiveStatistics xAxis, DescriptiveStatistics yAxis, MathContext mathContext, CountDownLatch latch) throws CanNotCalculateException {
            this.latch = latch;
            this.mathContext = mathContext;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            if (xAxis.getStandardDeviation().equals(new BigDecimal(0))) {
                latch.countDown();
                throw new CanNotCalculateException("Standard deviation of xAxis is zero");
            } else if (yAxis.getStandardDeviation().equals(new BigDecimal(0))) {
                latch.countDown();
                throw new CanNotCalculateException("Standard deviation of yAxis is zero");
            }
        }

        @Override
        public CorrelationResult call() throws Exception {
            System.out.println(String.format("\tCorrelating %1$s : %2$s", xAxis.getSymbol(), yAxis.getSymbol()));
            System.out.flush();
            BigDecimal tally = new BigDecimal(0);
            int points = 0;
            try {
                // We can only calculate correlation where we have a pair of values.
                int maxIndex = Math.min(xAxis.getLength(), yAxis.getLength());
                int xIndex = maxIndex-1;
                int yIndex = maxIndex-1;

                while (xIndex >= 0 && yIndex >= 0) {
                    long xTime = xAxis.getDate(xIndex).getTime();
                    long yTime = yAxis.getDate(yIndex).getTime();
                    while (xTime < yTime && xIndex > 0) {
                        xTime = xAxis.getDate(--xIndex).getTime();
                    }
                    while (yTime < xTime && yIndex > 0) {
                        yTime = yAxis.getDate(--yIndex).getTime();
                    }
                    if (xTime == yTime && xIndex >= 0 && yIndex >= 0) {
                        BigDecimal stdX = xAxis.getMean().subtract(xAxis.getValue(xIndex)).divide(xAxis.getStandardDeviation(), mathContext);
                        BigDecimal stdY = yAxis.getMean().subtract(yAxis.getValue(yIndex)).divide(yAxis.getStandardDeviation(), mathContext);
                        tally = tally.add(stdX.multiply(stdY));
                        points++;
                        System.out.println(String.format("\t\tCalculated %1$d of %2$d for %3$s : %4$s", points, maxIndex, xAxis.getSymbol(), yAxis.getSymbol()));
                        xIndex--;
                        yIndex--;
                    } else {
                        System.out.println(String.format("%1$s : %2$s xTime %3$d yTime %4$d xIndex %5$d yIndex %6$d", xAxis.getSymbol(), yAxis.getSymbol(), xTime, yTime, xIndex, yIndex));
                    }
                    System.out.flush();
                }
            } finally {
                latch.countDown();
            }
            System.out.println(String.format("\tCorrelated %1$s : %2$s", xAxis.getSymbol(), yAxis.getSymbol()));
            System.out.flush();
            return new CorrelationResult(tally.divide(new BigDecimal(points -1), mathContext), xAxis.getSymbol(), yAxis.getSymbol());
        }
    }
}
