package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.SimpleAxis;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class AxisCleanerTest {
    private AxisCleaner axisCleaner;

    @Before
    public void setup() {
        axisCleaner = new AxisCleanerImpl();
    }

    @Test
    public void when_cleaningAxis_given_missingDates_then_returnsMatchingData() {
        Axis aAxis = new SimpleAxis("a",
                new BigDecimal[] {new BigDecimal(1), new BigDecimal(2), new BigDecimal(4)},
                new Date[] {new Date(0), new Date(2), new Date(4)});
        Axis bAxis = new SimpleAxis("b",
                new BigDecimal[] {new BigDecimal(2), new BigDecimal(4), new BigDecimal(6)},
                new Date[] {new Date(1), new Date(2), new Date(3)});
        CleanedAxes cleanedAxes = axisCleaner.cleanAxes(aAxis, bAxis);

        Axis aCleaned = cleanedAxes.getAClean();
        Axis bCleaned = cleanedAxes.getBClean();

        assertThat(aCleaned.getLength(), equalTo(1));
        assertThat(aCleaned.getSymbol(), equalTo("a"));
        assertThat(aCleaned.getValue(0), equalTo(new BigDecimal(2)));
        assertThat(aCleaned.getDate(0), equalTo(new Date(2)));

        assertThat(bCleaned.getLength(), equalTo(1));
        assertThat(bCleaned.getSymbol(), equalTo("b"));
        assertThat(bCleaned.getValue(0), equalTo(new BigDecimal(4)));
        assertThat(bCleaned.getDate(0), equalTo(new Date(2)));
    }
}
