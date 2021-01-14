package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.SimpleAxis;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class DescriptiveStatisticsTest {
    @Test
    public void when_calculatingStatistics_given_easyData_calculatesCorrectAnswer() throws Exception {
        BigDecimal[] data = new BigDecimal[] {new BigDecimal(-1), new BigDecimal(0), new BigDecimal(1)};
        Date[] dates = new Date[] {new Date(0), new Date(1), new Date(2)};
        Axis axis = new SimpleAxis("TEST", data, dates);
        DescriptiveStatistics statistics = new DescriptiveStatistics.DescriptiveStatisticBuilder(axis, MathContext.DECIMAL64).call();

        assertThat(statistics.getAxis(), equalTo(axis));
        assertThat(statistics.getValue(0), equalTo(new BigDecimal(-1)));
        assertThat(statistics.getDate(0), equalTo(new Date(0)));
        assertThat(statistics.getSymbol(), equalTo("TEST"));
        assertThat(statistics.getLength(), equalTo(3));
        assertThat(statistics.getMean(), equalTo(new BigDecimal(0)));
        assertThat(statistics.getStandardDeviation(), equalTo(new BigDecimal(1)));
    }
}
