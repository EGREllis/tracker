package net.finance.tracker.scrapper;

import net.finance.tracker.domain.series.NasdaqDividendSeries;
import net.finance.tracker.pattern.Listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

public class NasdaqDividendScrapper implements Callable<NasdaqDividendSeries> {
    private static final String URL_TEMPLATE = "https://www.nasdaq.com/market-activity/stocks/%1$s/dividend-history";
    private final Listener<Exception> exceptionListener;
    private final String symbol;

    public NasdaqDividendScrapper(String symbol, Listener<Exception> exceptionListener) {
        this.exceptionListener = exceptionListener;
        this.symbol = symbol;
    }

    @Override
    public NasdaqDividendSeries call() throws Exception {
        URL url = new URL(String.format(URL_TEMPLATE, symbol));
        URLConnection connection = url.openConnection();
        connection.connect();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ( (line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            exceptionListener.listen(e);
        }
        return null;
    }
}
