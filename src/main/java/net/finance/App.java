package net.finance;

import net.finance.tracker.domain.Series;
import net.finance.tracker.logging.LoggingListener;
import net.finance.tracker.logging.NoopListener;
import net.finance.tracker.pattern.SetSource;
import net.finance.tracker.scrapper.YahooFinanceFxScrapper;
import net.finance.tracker.scrapper.YahooFinanceStockScrapper;
import net.finance.tracker.pattern.Listener;
import net.finance.tracker.source.ClasspathFxSymbolSource;
import net.finance.tracker.source.ClasspathStockSymbolSource;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App {
    public static void main( String[] args ) throws Exception {
        long firstStockPeriod = 0L;
        long secondStockPeriod = 1610407859L;
        long firstFxPeriod = 1578845052L;
        long secondFxPeriod = 1610467452L;
        Listener<Exception> exceptionListener = getListenerFromProgramArguments(args);
        SetSource<String> stockSymbolSource = new ClasspathStockSymbolSource(exceptionListener);
        SetSource<String> fxSymbolSource = new ClasspathFxSymbolSource(exceptionListener);

        int nProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(nProcessors);

        Set<String> stockSymbols = stockSymbolSource.getSource();
        System.out.println(String.format("Sourced %1$d stock symbols", stockSymbols.size()));
        Map<String,Series> stockMap = loadStockSeries(stockSymbols, firstStockPeriod, secondStockPeriod, exceptionListener, service);

        Set<String> fxSymbols = fxSymbolSource.getSource();
        System.out.println(String.format("Sourced %1$d fx symbols", fxSymbols.size()));
        Map<String,Series> fxMap = loadFxMap(fxSymbols, firstFxPeriod, secondFxPeriod, exceptionListener, service);

        service.shutdown();

        for (Map.Entry<String,Series> stock : stockMap.entrySet()) {
            System.out.println(stock.getValue());
        }

        for (Map.Entry<String,Series> fx : fxMap.entrySet()) {
            System.out.println(fx.getValue());
        }
        System.out.flush();
    }

    private static Listener<Exception> getListenerFromProgramArguments(String[] args) {
        Listener<Exception> listener;
        if (isVerboseLoggingEnabled(args)) {
            listener = new LoggingListener();
        } else {
            listener = new NoopListener();
        }
        return listener;
    }

    private static boolean isVerboseLoggingEnabled(String[] args) {
        boolean result = false;
        for (String arg : args) {
            if ("-v".equals(arg)) {
                result = true;
                break;
            }
        }
        System.out.println(String.format("Verbose mode enabled? %1$s", result));
        return result;
    }

    private static Map<String,Series> loadStockSeries(Set<String> symbols, long firstPeriod, long secondPeriod, Listener<Exception> exceptionListener, ExecutorService service) {
        Map<String,Series> seriesMap = new TreeMap<>();

        List<Future<Series>> futures = new ArrayList<>();
        long startLoad = System.currentTimeMillis();
        long nRecords = 0L;
        for (String symbol : symbols) {
            Callable<Series> loader = new YahooFinanceStockScrapper(symbol, firstPeriod, secondPeriod, exceptionListener);
            Future<Series> future = service.submit(loader);
            futures.add(future);
        }
        for (Future<Series> future : futures) {
            try {
                Series series = future.get();
                seriesMap.put(series.getSymbol(), series);
                nRecords += series.getLength();
            } catch (Exception e) {
                exceptionListener.listen(e);
            }
        }
        long stopLoad = System.currentTimeMillis();
        System.out.println(String.format("Loaded %1$d records %2$d series in %3$f/s", nRecords, symbols.size(), (stopLoad - startLoad)/1000.0));
        return seriesMap;
    }

    private static Map<String,Series> loadFxMap(Set<String> fxSymbols, long firstPeriod, long secondPeriod, Listener<Exception> listener, ExecutorService service) {
        Map<String, Series> fxMap = new TreeMap<>();

        List<Future<Series>> futures = new ArrayList<>();
        long startLoad = System.currentTimeMillis();
        long nRecords = 0L;
        for (String symbol :  fxSymbols) {
            Callable<Series> loader = new YahooFinanceFxScrapper(symbol, firstPeriod, secondPeriod, listener);
            Future<Series> future = service.submit(loader);
            futures.add(future);
        }
        for (Future<Series> future : futures) {
            try {
                Series series = future.get();
                fxMap.put(series.getSymbol(), series);
                nRecords += series.getLength();
            } catch (Exception e) {
                listener.listen(e);
            }
        }
        long stopLoad = System.currentTimeMillis();
        System.out.println(String.format("Loaded %1$d records %2$d series in %3$f/s", nRecords, fxSymbols.size(), (stopLoad - startLoad)/1000.0));
        return fxMap;
    }
}
