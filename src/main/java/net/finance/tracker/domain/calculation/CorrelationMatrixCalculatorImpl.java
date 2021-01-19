package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.util.pattern.Listener;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.*;

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
                try {
                    Axis xAxis = axes.get(x);
                    Axis yAxis = axes.get(y);
                    Callable<CorrelationResult> calc = new CorrelationMatrixCalculatorImpl.CorrelationCalculator(xAxis, yAxis, axisCleaner, mathContext, latch);
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

        try {
            System.out.println(String.format("Main thread parked, awaiting %1$d calculation tasks", tasks));
            latch.await();
        } catch (InterruptedException ie) {
            throw new RuntimeException("Calculation was interrupted", ie);
        }

        for (Future<CorrelationResult> calculatedResult : futures) {
            CorrelationResult result;
            try {
                result = calculatedResult.get();
                if (result == null) {
                    System.out.println("Future returned a null result!");
                }
                int xIndex = getIndexOfAxisWithSymbolFromAxes(result.symbolX, axes);
                int yIndex = getIndexOfAxisWithSymbolFromAxes(result.symbolY, axes);
                data[yIndex][xIndex] = result.getResult();
                data[xIndex][yIndex] = result.getResult();
            } catch (InterruptedException | ExecutionException e) {
                exceptionListener.listen(e);
            }
        }
        long stopTime = System.currentTimeMillis();
        System.out.println(String.format("Calculated %1$d correlations in %2$f/s", numberOfTasks, (stopTime - startTime)/1000.0));
    }

    private int getIndexOfAxisWithSymbolFromAxes(String symbol, List<Axis> axes) {
        for (int i = 0; i < axes.size(); i++) {
            if (symbol.equals(axes.get(i).getSymbol())) {
                return i;
            }
        }
        throw new IllegalArgumentException(String.format("Symbol %1$s could not be found in axes", symbol));
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
        private final CountDownLatch latch;
        private final MathContext mathContext;
        private final AxisCleaner axisCleaner;
        private final Axis xAxis;
        private final Axis yAxis;

        public CorrelationCalculator(Axis xAxis, Axis yAxis, AxisCleaner cleaner, MathContext mathContext, CountDownLatch latch) throws CanNotCalculateException {
            this.latch = latch;
            this.mathContext = mathContext;
            this.axisCleaner = cleaner;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }

        @Override
        public CorrelationResult call() throws Exception {
            System.out.println(String.format("\tCorrelating %1$s : %2$s", xAxis.getSymbol(), yAxis.getSymbol()));
            System.out.flush();
            CleanedAxes cleaned = axisCleaner.cleanAxes(xAxis, yAxis);
            DescriptiveStatistics xStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(cleaned.getAClean(), mathContext).call();
            DescriptiveStatistics yStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(cleaned.getBClean(), mathContext).call();

            if (xStats.getStandardDeviation().equals(new BigDecimal(0))) {
                latch.countDown();
                throw new CanNotCalculateException("Can not calculate correlation - standard deviation of the xAxis is zero");
            } else if (yStats.getStandardDeviation().equals(new BigDecimal(0))) {
                latch.countDown();
                throw new CanNotCalculateException("Can not calculate correlation - standard deviation of the yAxis is zero");
            }

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
                        yTime = yAxis.getDate(--yIndex).getTime();
                    }
                    while (yTime < xTime && yIndex > 0) {
                        xTime = xAxis.getDate(--xIndex).getTime();
                    }
                    if (xTime == yTime && xIndex >= 0 && yIndex >= 0) {
                        BigDecimal stdX = xStats.getMean().subtract(xAxis.getValue(xIndex)).divide(xStats.getStandardDeviation(), mathContext);
                        BigDecimal stdY = xStats.getMean().subtract(yAxis.getValue(yIndex)).divide(xStats.getStandardDeviation(), mathContext);
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
