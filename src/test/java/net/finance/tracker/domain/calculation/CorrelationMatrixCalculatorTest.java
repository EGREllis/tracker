package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.SimpleAxis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class CorrelationMatrixCalculatorTest {
    private static final BigDecimal TOLERANCE = new BigDecimal("0.000000000000001");

    @Test
    public void when_calculatingCorrelation_given_positiveCorrelation_then_returnsOne() throws Exception {
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] data = bigDecimalArray(-1, 1, 0);
        Date[] dates = makeDates(data);

        Axis axis = new SimpleAxis("TEST", data, dates);
        DescriptiveStatistics stats = new DescriptiveStatistics.DescriptiveStatisticBuilder(axis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlation = new CorrelationMatrixCalculatorImpl.CorrelationCalculator(stats, stats, MathContext.DECIMAL64);
        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlation.call();

        assertThat(result.getSymbolX(), equalTo("TEST"));
        assertThat(result.getSymbolY(), equalTo("TEST"));
        assertEquals(result.getResult(), new BigDecimal(1));
    }

    @Test
    public void when_calculateCorrelation_given_negatigveCorrelation_then_returnsMinusOne() throws Exception {
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] positiveData = bigDecimalArray(-1, 1);
        BigDecimal[] negativeData = bigDecimalArray(1, -1);
        Date[] dates = new Date[] {new Date(0), new Date(1)};

        Axis positiveAxis = new SimpleAxis("POSITIVE", positiveData, dates);
        Axis negativeAxis = new SimpleAxis("NEGATIVE", negativeData, dates);
        DescriptiveStatistics positiveStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(positiveAxis, mathContext).call();
        DescriptiveStatistics negativeStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(negativeAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlationCalculator =
                new CorrelationMatrixCalculatorImpl.CorrelationCalculator(positiveStatistics, negativeStatistics, mathContext);

        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlationCalculator.call();

        assertThat(result.getSymbolX(), equalTo("POSITIVE"));
        assertThat(result.getSymbolY(), equalTo("NEGATIVE"));
        assertEquals(result.getResult(), new BigDecimal(-1));
    }

    @Test(expected = CanNotCalculateException.class)
    public void when_calculateCorrelation_given_flatSample_then_exceptionThrown() throws Exception {
        MathContext mathContext = MathContext.DECIMAL64;
        BigDecimal[] vertical = bigDecimalArray(0, 0, 0);
        BigDecimal[] horiztonal = bigDecimalArray(1, 1, 1);
        Date[] dates = makeDates(vertical);

        Axis verticalAxis = new SimpleAxis("VERTICAL", vertical, dates);
        Axis horizontalAxis = new SimpleAxis("HORIZONTAL", horiztonal, dates);
        DescriptiveStatistics verticalStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(verticalAxis, mathContext).call();
        DescriptiveStatistics horizontalStatistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(horizontalAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlationCalculator =
                new CorrelationMatrixCalculatorImpl.CorrelationCalculator(verticalStatistics, horizontalStatistics, mathContext);
        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlationCalculator.call();

        assert false : "A CanNotCalculateException should not have been thrown.";
    }

    @Test
    public void when_calcuateCorrelation_given_unCorrelatedButCalculable_then_returnsZero() throws Exception {
        MathContext mathContext = MathContext.DECIMAL32;
        BigDecimal[] vertical = bigDecimalArray(0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0, -1);
        BigDecimal[] horizontal = bigDecimalArray(-1, 0, 1, 0, -1, 0, 1, 0, -1, 0, 1, 0);
        Date[] dates = makeDates(vertical);

        Axis verticalAxis = new SimpleAxis("VERTICAL", vertical, dates);
        Axis horizontalAxis = new SimpleAxis("HORIZONTAL", horizontal, dates);
        DescriptiveStatistics verticalStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(verticalAxis, mathContext).call();
        DescriptiveStatistics horizontalStats = new DescriptiveStatistics.DescriptiveStatisticBuilder(horizontalAxis, mathContext).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlationCalculator =
                new CorrelationMatrixCalculatorImpl.CorrelationCalculator(verticalStats, horizontalStats, mathContext);

        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlationCalculator.call();

        assertThat("VERTICAL", equalTo("VERTICAL"));
        assertThat("HORIZONTAL", equalTo("HORIZONTAL"));
        assertEquals(result.getResult(), new BigDecimal(0));
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
