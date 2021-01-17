package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.SimpleAxis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.finance.tracker.util.logging.NoopListener;
import net.finance.tracker.util.pattern.Listener;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

public class CorrelationMatrixCalculatorTest {
    private static final BigDecimal TOLERANCE = new BigDecimal("0.000000000000001");

    /*
    @Test
    public void when_calculatingCorrelation_given_misMatchedDates_then_onlyIncludeMatchingDates() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] aData = bigDecimalArray(-1, 10, 1);
        BigDecimal[] bData = bigDecimalArray(-1, 1);
        Date[] aDates = new Date[] {new Date(0), new Date(1), new Date(2)};
        Date[] bDates = new Date[] {new Date(0), new Date(2)};

        Axis aAxis = new SimpleAxis("A", aData, aDates);
        Axis bAxis = new SimpleAxis("B", bData, bDates);
        DescriptiveStatistics aStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(aAxis, mathContext).call();
        DescriptiveStatistics bStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(bAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlation = new CorrelationMatrixCalculatorImpl.CorrelationCalculator(aStats, bStats, mathContext, latch);
        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlation.call();

        assertThat(result.getSymbolX(), equalTo("A"));
        assertThat(result.getSymbolY(), equalTo("B"));
        assertEquals(result.getResult(), new BigDecimal(1));
    }
     */

    @Test
    public void when_calculatingCorrelation_given_positiveCorrelation_then_returnsOne() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] data = bigDecimalArray(-1, 1, 0);
        Date[] dates = makeDates(data);

        Axis axis = new SimpleAxis("TEST", data, dates);
        DescriptiveStatistics stats = new DescriptiveStatistics.DescriptiveStatisticBuilder(axis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlation = new CorrelationMatrixCalculatorImpl.CorrelationCalculator(stats, stats, MathContext.DECIMAL64, latch);
        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlation.call();

        assertThat(result.getSymbolX(), equalTo("TEST"));
        assertThat(result.getSymbolY(), equalTo("TEST"));
        assertEquals(result.getResult(), new BigDecimal(1));
    }

    @Test
    public void when_calculateCorrelation_given_negatigveCorrelation_then_returnsMinusOne() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] positiveData = bigDecimalArray(-1, 1);
        BigDecimal[] negativeData = bigDecimalArray(1, -1);
        Date[] dates = new Date[] {new Date(0), new Date(1)};

        Axis positiveAxis = new SimpleAxis("POSITIVE", positiveData, dates);
        Axis negativeAxis = new SimpleAxis("NEGATIVE", negativeData, dates);
        DescriptiveStatistics positiveStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(positiveAxis, mathContext).call();
        DescriptiveStatistics negativeStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(negativeAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlationCalculator =
                new CorrelationMatrixCalculatorImpl.CorrelationCalculator(positiveStatistics, negativeStatistics, mathContext, latch);

        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlationCalculator.call();

        assertThat(result.getSymbolX(), equalTo("POSITIVE"));
        assertThat(result.getSymbolY(), equalTo("NEGATIVE"));
        assertEquals(result.getResult(), new BigDecimal(-1));
    }

    @Test(expected = CanNotCalculateException.class)
    public void when_calculateCorrelation_given_flatSample_then_exceptionThrown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] vertical = bigDecimalArray(0, 0, 0);
        BigDecimal[] horiztonal = bigDecimalArray(1, 1, 1);
        Date[] dates = makeDates(vertical);

        Axis verticalAxis = new SimpleAxis("VERTICAL", vertical, dates);
        Axis horizontalAxis = new SimpleAxis("HORIZONTAL", horiztonal, dates);
        DescriptiveStatistics verticalStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(verticalAxis, mathContext).call();
        DescriptiveStatistics horizontalStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(horizontalAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlationCalculator =
                new CorrelationMatrixCalculatorImpl.CorrelationCalculator(verticalStatistics, horizontalStatistics, mathContext, latch);
        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlationCalculator.call();

        assert false : "A CanNotCalculateException should not have been thrown.";
    }

    @Test
    public void when_calcuateCorrelation_given_unCorrelatedButCalculable_then_returnsZero() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        MathContext mathContext = MathContext.DECIMAL32;
        BigDecimal[] vertical = bigDecimalArray(0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1);
        BigDecimal[] horizontal = bigDecimalArray(-1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0);
        Date[] dates = makeDates(vertical);

        Axis verticalAxis = new SimpleAxis("VERTICAL", vertical, dates);
        Axis horizontalAxis = new SimpleAxis("HORIZONTAL", horizontal, dates);
        DescriptiveStatistics verticalStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(verticalAxis, mathContext).call();
        DescriptiveStatistics horizontalStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(horizontalAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlationCalculator =
                new CorrelationMatrixCalculatorImpl.CorrelationCalculator(verticalStats, horizontalStats, mathContext, latch);

        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlationCalculator.call();

        assertThat("VERTICAL", equalTo("VERTICAL"));
        assertThat("HORIZONTAL", equalTo("HORIZONTAL"));
        assertEquals(result.getResult(), new BigDecimal(0));
    }

    @Test
    public void when_calculatingMatrix_given_easyInputs_then_returnExpectedResult() {
        AxisCleaner cleaner = new AxisCleanerImpl();
        Listener<Exception> listener = new NoopListener();
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] positiveData = bigDecimalArray(-1, 1);
        BigDecimal[] negativeData = bigDecimalArray(1, -1);
        Date[] dates = makeDates(positiveData);

        Axis positiveAxis = new SimpleAxis("POSITIVE", positiveData, dates);
        Axis negativeAxis = new SimpleAxis("NEGATIVE", negativeData, dates);
        List<Axis> axes = Arrays.asList(positiveAxis, negativeAxis);
        CorrelationMatrixCalculator calculator = new CorrelationMatrixCalculatorImpl(listener, cleaner, mathContext);
        int nCalculations = calculator.correlationsToCalculate(axes.size());
        ExecutorService service = Executors.newFixedThreadPool(nCalculations);

        try {
            CorrelationMatrix matrix = calculator.calculate(axes, service);
            assertThat(matrix.getCell(0, 0), nullValue());
            assertEquals(matrix.getCell(0, 1), new BigDecimal(-1));
            assertEquals(matrix.getCell(1,0), new BigDecimal(-1));
            assertThat(matrix.getCell(1, 1), nullValue());
        } finally {
            service.shutdown();
        }
    }

    @Test
    public void when_calculatingMatrix_given_threeAxes_then_calculatesExpectedResults() {
        AxisCleaner cleaner = new AxisCleanerImpl();
        Listener<Exception> listener = new NoopListener();
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] positiveData = bigDecimalArray(-1, 1);
        BigDecimal[] negativeData = bigDecimalArray(1, -1);
        Date[] dates = makeDates(positiveData);

        Axis positiveAxis = new SimpleAxis("POSITIVE", positiveData, dates);
        Axis negativeAxis = new SimpleAxis("NEGATIVE", negativeData, dates);
        Axis secondPositiveAxis = new SimpleAxis("SECOND_POSITIVE", positiveData, dates);

        List<Axis> axes = Arrays.asList(positiveAxis, negativeAxis, secondPositiveAxis);
        CorrelationMatrixCalculator calculator = new CorrelationMatrixCalculatorImpl(listener, cleaner, mathContext);
        int nCalculations = calculator.correlationsToCalculate(axes.size());
        ExecutorService service = Executors.newFixedThreadPool(nCalculations);

        try {
            CorrelationMatrix matrix = calculator.calculate(axes, service);
            assertThat(matrix.getCell(0, 0), nullValue());
            assertThat(matrix.getCell(1, 1), nullValue());
            assertThat(matrix.getCell(2, 2), nullValue());
            assertEquals(matrix.getCell(0, 1), new BigDecimal(-1));
            assertEquals(matrix.getCell(1,0), new BigDecimal(-1));
            assertEquals(matrix.getCell(0, 2), new BigDecimal(1));
            assertEquals(matrix.getCell(2, 0), new BigDecimal(1));
            assertEquals(matrix.getCell(1, 2), new BigDecimal(-1));
            assertEquals(matrix.getCell(2, 1), new BigDecimal(-1));
        } finally {
            service.shutdown();
        }
    }

    private BigDecimal[] bigDecimalArray(int... data) {
        BigDecimal[] result = new BigDecimal[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = new BigDecimal(data[i]);
        }
        return result;
    }

    private Date[] makeDates(BigDecimal[] data) {
        Date[] result = new Date[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = new Date(i);
        }
        return result;
    }

    private void assertEquals(BigDecimal actual, BigDecimal expected) {
        BigDecimal LOWER_TOLERANCE = expected.subtract(TOLERANCE);
        BigDecimal HIGHER_TOLERANCE = expected.add(TOLERANCE);
        assert actual.compareTo(LOWER_TOLERANCE) >= 0 : String.format("Actual %1$s is less than lower tolerance %2$s", actual, LOWER_TOLERANCE);
        assert actual.compareTo(HIGHER_TOLERANCE) <= 0 : String.format("Actual %1$s is greater than higher tolerance %2$s", actual, HIGHER_TOLERANCE);
    }
}
