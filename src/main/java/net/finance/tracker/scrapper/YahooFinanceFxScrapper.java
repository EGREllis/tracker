package net.finance.tracker.scrapper;

import net.finance.tracker.domain.series.FXSeries;
import net.finance.tracker.domain.series.Series;
import net.finance.tracker.pattern.Listener;

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
                        Date date = SIMPLE_DATE_FORMAT.parse(matcher.group(1));
                        BigDecimal open = new BigDecimal(matcher.group(2));
                        BigDecimal high = new BigDecimal(matcher.group(3));
                        BigDecimal low = new BigDecimal(matcher.group(4));
                        BigDecimal close = new BigDecimal(matcher.group(5));
                        BigDecimal adjClose = new BigDecimal(matcher.group(6));
                        builder.addRow(date, open, close, high, low, adjClose);
                    } else {
                        System.err.println(String.format("Could not parse FX line: %1$s", line));
                    }
                } catch (Exception e) {
                    listener.listen(e);
                }
            }
        }
        return builder.build();
    }
}
