package net.finance.tracker.io.scrapper;

import net.finance.tracker.util.pattern.Listener;

import java.io.InputStream;

public class YahooFinanceFileStockScrapper extends YahooFinanceStockScrapper {
    public YahooFinanceFileStockScrapper(String symbol, Long firstPeriod, Long secondPeriod, Listener<Exception> exceptionListener) {
        super(symbol, firstPeriod, secondPeriod, exceptionListener);
    }

    InputStream getInputStream(String symbol, long firstPeriod, long secondPeriod) throws Exception {
        return ClassLoader.getSystemResourceAsStream(String.format("%1$s.csv", symbol));
    }
}
