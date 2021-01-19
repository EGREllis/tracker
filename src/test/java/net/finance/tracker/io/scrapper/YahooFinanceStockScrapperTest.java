package net.finance.tracker.io.scrapper;

import net.finance.tracker.domain.series.Series;
import net.finance.tracker.domain.series.StockSeries;
import net.finance.tracker.util.logging.LoggingListener;
import net.finance.tracker.util.pattern.Listener;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class YahooFinanceStockScrapperTest {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private final String testSymbol1 = "BARC.L";
    private final String testSymbol2 = "0H6I.IL";

    @Test
    public void when_sampledData_given_fixedSymbol2AndDates_then_returnExpectedValues() throws Exception {
        Listener<Exception> listener = new LoggingListener();
        YahooFinanceStockScrapper scrapper = new YahooFinanceFileStockScrapper(
                testSymbol2, 0L, 1610407859L, listener);
        Series series = scrapper.call();

        assertThat(series.getNDataQualityIssues(), equalTo(89));
        assertThat(series.getLength(), equalTo(2464-89));

        assertThat(series.getDate(0), equalTo(DATE_FORMAT.parse("20110411")));
        assertThat(series.getOpen(0), equalTo(new BigDecimal("1.080010")));
        assertThat(series.getHigh(0), equalTo(new BigDecimal("1.086000")));
        assertThat(series.getLow(0), equalTo(new BigDecimal("1.073000")));
        assertThat(series.getClose(0), equalTo(new BigDecimal("1.080010")));
        assertThat(series.getAdjClose(0), equalTo(new BigDecimal("1.015799")));
        assertThat(((StockSeries)series).getVolume(0), equalTo(151120429L));

        int lastIndex = series.getLength()-1;
        assertThat(series.getDate(lastIndex), equalTo(DATE_FORMAT.parse("20210111")));
        assertThat(series.getOpen(lastIndex), equalTo(new BigDecimal("0.391900")));
        assertThat(series.getHigh(lastIndex), equalTo(new BigDecimal("0.394500")));
        assertThat(series.getLow(lastIndex), equalTo(new BigDecimal("0.387000")));
        assertThat(series.getClose(lastIndex), equalTo(new BigDecimal("0.389800")));
        assertThat(series.getAdjClose(lastIndex), equalTo(new BigDecimal("0.389800")));
        assertThat(((StockSeries)series).getVolume(lastIndex), equalTo(8585731L));
    }

    @Test
    public void when_sampledData_given_fixedSymbol1AndDates_then_returnsExpectedValues() throws Exception {
        Listener<Exception> listener = new LoggingListener();
        YahooFinanceStockScrapper scrapper = new YahooFinanceFileStockScrapper(
                testSymbol1, 0L, 1610407859L, listener);
        Series series = scrapper.call();

        assertThat(series.getNDataQualityIssues(), equalTo(19));
        assertThat(series.getLength(), equalTo(8345));

        assertThat(series.getDate(0), equalTo(DATE_FORMAT.parse("19880701")));
        assertThat(series.getOpen(0), equalTo(new BigDecimal("66.972000")));
        assertThat(series.getHigh(0), equalTo(new BigDecimal("66.972000")));
        assertThat(series.getLow(0), equalTo(new BigDecimal("66.972000")));
        assertThat(series.getClose(0), equalTo(new BigDecimal("66.972000")));
        assertThat(((StockSeries)series).getVolume(0), equalTo(0L));

        int lastIndex = series.getLength()-1;
        assertThat(series.getDate(lastIndex), equalTo(DATE_FORMAT.parse("20210111")));
        assertThat(series.getOpen(lastIndex), equalTo(new BigDecimal("152.399994")));
        assertThat(series.getHigh(lastIndex), equalTo(new BigDecimal("154.479996")));
        assertThat(series.getLow(lastIndex), equalTo(new BigDecimal("150.039993")));
        assertThat(series.getClose(lastIndex), equalTo(new BigDecimal("151.199997")));
        assertThat(series.getAdjClose(lastIndex), equalTo(new BigDecimal("151.199997")));
        assertThat(((StockSeries)series).getVolume(lastIndex), equalTo(97258483L));
    }

    @Test
    public void when_yahooFinance_given_fixedSymbol1AndDates_then_returnsExpectedValues() throws Exception {
        Listener<Exception> listener = new LoggingListener();
        YahooFinanceStockScrapper scrapper = new YahooFinanceStockScrapper(
                testSymbol1, 0L, 1610407859L, listener);
        Series series = scrapper.call();

        assertThat(series.getNDataQualityIssues(), equalTo(19));
        assertThat(series.getLength(), equalTo(8345));

        assertThat(series.getDate(0), equalTo(DATE_FORMAT.parse("19880701")));
        assertThat(series.getOpen(0), equalTo(new BigDecimal("66.972000")));
        assertThat(series.getHigh(0), equalTo(new BigDecimal("66.972000")));
        assertThat(series.getLow(0), equalTo(new BigDecimal("66.972000")));
        assertThat(series.getClose(0), equalTo(new BigDecimal("66.972000")));
        assertThat(((StockSeries)series).getVolume(0), equalTo(0L));

        int lastIndex = series.getLength()-1;
        assertThat(series.getDate(lastIndex), equalTo(DATE_FORMAT.parse("20210111")));
        assertThat(series.getOpen(lastIndex), equalTo(new BigDecimal("152.399994")));
        assertThat(series.getHigh(lastIndex), equalTo(new BigDecimal("154.479996")));
        assertThat(series.getLow(lastIndex), equalTo(new BigDecimal("150.039993")));
        assertThat(series.getClose(lastIndex), equalTo(new BigDecimal("151.199997")));
        assertThat(series.getAdjClose(lastIndex), equalTo(new BigDecimal("151.199997")));
        assertThat(((StockSeries)series).getVolume(lastIndex), equalTo(97258483L));
    }
}
