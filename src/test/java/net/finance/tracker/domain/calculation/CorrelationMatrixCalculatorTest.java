package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.SimpleAxis;
import net.finance.tracker.util.logging.NoopListener;
import net.finance.tracker.util.pattern.Listener;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class CorrelationMatrixCalculatorTest {
    @Test
    public void when_calculatingCorrelation_given_positiveCorrelation_then_returnsOne() throws Exception {
        Listener<Exception> exceptionListener = new NoopListener();
        BigDecimal[] data = new BigDecimal[] { new BigDecimal(-1), new BigDecimal(0), new BigDecimal(1)};
        Date[] dates = new Date[] {new Date(0), new Date(1), new Date(2)};
        Axis axis = new SimpleAxis("TEST", data, dates);
        DescriptiveStatistics stats = new DescriptiveStatistics.DescriptiveStatisticBuilder(axis, MathContext.DECIMAL64).call();

        CorrelationMatrixCalculatorImpl.CorrelationCalculator correlation = new CorrelationMatrixCalculatorImpl.CorrelationCalculator(stats, stats, MathContext.DECIMAL64);
        CorrelationMatrixCalculatorImpl.CorrelationResult result = correlation.call();

        assertThat(result.getSymbolX(), equalTo("TEST"));
        assertThat(result.getSymbolY(), equalTo("TEST"));
        assertThat(result.getResult(), equalTo(new BigDecimal(1)));
    }
}
