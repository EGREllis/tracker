package net.finance.tracker.io.scrapper;

import net.finance.tracker.domain.series.FXSeries;
import net.finance.tracker.domain.series.Series;
import net.finance.tracker.util.pattern.Listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooFinanceFxScrapper implements Callable<Series> {
    // https://query1.finance.yahoo.com/v7/finance/download/GBPEUR=X?period1=1578845052&period2=1610467452&interval=1d&events=history&includeAdjustedClose=true
    private static final String YAHOO_URL_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%1$s=X?period1=%2$d&period2=%3$d&interval=1d&events=history&includeAdjustedClose=true";
    private static final Pattern DATA_LINE_PATTERN = Pattern.compile("^([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+)");
    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final Listener<Exception> listener;
    private final String symbol;
    private final long firstPeriod;
    private final long secondPeriod;

    public YahooFinanceFxScrapper(String symbol, long firstPeriod, long secondPeriod, Listener<Exception> listener) {
        this.symbol = symbol;
        this.firstPeriod = firstPeriod;
        this.secondPeriod = secondPeriod;
        this.listener = listener;
    }

    @Override
    public Series call() throws Exception {
        URL url = new URL(String.format(YAHOO_URL_TEMPLATE, symbol, firstPeriod, secondPeriod));
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();

        FXSeries.FXSeriesBuilder builder = new FXSeries.FXSeriesBuilder(symbol);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ( (line = reader.readLine()) != null ) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    Matcher matcher = DATA_LINE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        Date date;
                        BigDecimal open;
                        BigDecimal high;
                        BigDecimal low;
                        BigDecimal close;
                        BigDecimal adjClose;
                        try {
                            date = SIMPLE_DATE_FORMAT.parse(matcher.group(1));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Could not parse date %1$s for symbol %2$s in line:\n%3$s", matcher.group(1), symbol, line));
                        }
                        try {
                            open = new BigDecimal(matcher.group(2));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Could not parse open %1$s for symbol %2$s in line:\n%3$s", matcher.group(2), symbol, line));
                        }
                        try {
                            high = new BigDecimal(matcher.group(3));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Could not parse high %1$s for symbol %2$s in line:\n%3$s", matcher.group(3), symbol, line));
                        }
                        try {
                            low = new BigDecimal(matcher.group(4));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Could not parse low %1$s for symbol %2$s in line:\n%3$s", matcher.group(4), symbol, line));
                        }
                        try {
                            close = new BigDecimal(matcher.group(5));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Could not parse close %1$s for symbol %2$s in line:\n%3$s", matcher.group(5), symbol, line));
                        }
                        try {
                            adjClose = new BigDecimal(matcher.group(6));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Could not parse adjustedClose %1$s for symbol %2$s in line:\n%3$s", matcher.group(6), symbol, line));
                        }
                        builder.addRow(date, open, close, high, low, adjClose);
                    } else {
                        System.err.println(String.format("Could not parse FX line: %1$s", line));
                    }
                } catch (Exception e) {
                    builder.addDataQualityIssue(e);
                }
            }
        }
        return builder.build();
    }
}
