package net.finance.tracker.scrapper;

import net.finance.tracker.domain.Series;
import net.finance.tracker.domain.StockSeries;
import net.finance.tracker.pattern.Listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooFinanceStockScrapper implements Callable<Series> {
    private static final String DATA_LINE_ERROR_TEMPLATE = "Error in data line: %1$s";
    //https://query1.finance.yahoo.com/v7/finance/download/LSE.L?period1=1578813378&period2=1610435778&interval=1d&events=history&includeAdjustedClose=true
    private static final String YAHOO_URL_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%1$s?period1=%2$s&period2=%3$s&interval=1d&events=history&includeAdjustedClose=true";
    private static final Pattern DATA_LINE = Pattern.compile("^([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*)$");
    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String symbol;
    private final Long firstPeriod;
    private final Long secondPeriod;
    private final Listener<Exception> listener;

    public YahooFinanceStockScrapper(String symbol, Long firstPeriod, Long secondPeriod, Listener<Exception> exceptionListener) {
        this.symbol = symbol;
        this.firstPeriod = firstPeriod;
        this.secondPeriod = secondPeriod;
        this.listener = exceptionListener;
    }

    @Override
    public Series call() throws Exception {
        StockSeries.StockSeriesBuilder builder = new StockSeries.StockSeriesBuilder(symbol);
        URL url = new URL(String.format(YAHOO_URL_TEMPLATE, symbol, firstPeriod, secondPeriod));
        URLConnection connection = url.openConnection();
        connection.connect();

        String line;
        int lineN = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            boolean header = true;

            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                Matcher matcher = DATA_LINE.matcher(line);
                if (!matcher.matches()) {
                    throw new ParseException(String.format(DATA_LINE_ERROR_TEMPLATE, line), lineN);
                } else {
                    try {
                        Date date = SIMPLE_DATE_FORMAT.parse(matcher.group(1));
                        BigDecimal open = new BigDecimal(matcher.group(2));
                        BigDecimal high = new BigDecimal(matcher.group(3));
                        BigDecimal low = new BigDecimal(matcher.group(4));
                        BigDecimal close = new BigDecimal(matcher.group(5));
                        BigDecimal adjClose = new BigDecimal(matcher.group(6));
                        Long volume = Long.parseLong(matcher.group(7));
                        builder.addLine(date, open, high, low, close, adjClose, volume);
                    } catch (Exception e) {
                        listener.listen(e);
                    }
                }
                lineN++;
            }
        } catch (Exception e) {
            listener.listen(e);
        }
        return builder.build();
    }
}
