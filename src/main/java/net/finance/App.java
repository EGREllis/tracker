package net.finance;

import net.finance.tracker.domain.Series;
import net.finance.tracker.scrapper.YahooFinanceStockScrapper;

import java.util.concurrent.Callable;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        String[] symbols = new String[] {"MSFT", "USD", "TSLA", "LSE.L"};
        //long firstPeriod = 1578785459L;
        long firstPeriod = 1500000000L;
        long secondPeriod = 1610407859L;

        for (String symbol : symbols) {
            Callable<Series> loader = new YahooFinanceStockScrapper(symbol, firstPeriod, secondPeriod);
            Series series = loader.call();
            System.out.println(series);
        }
    }
}
