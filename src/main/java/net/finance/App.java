package net.finance;

import net.finance.tracker.domain.Series;
import net.finance.tracker.logging.LoggingListener;
import net.finance.tracker.logging.NoopListener;
import net.finance.tracker.pattern.SetSource;
import net.finance.tracker.scrapper.YahooFinanceStockScrapper;
import net.finance.tracker.pattern.Listener;
import net.finance.tracker.source.ClasspathSymbolSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App {
    public static void main( String[] args ) throws Exception {
        long firstPeriod = 0L;
        long secondPeriod = 1610407859L;
        Listener<Exception> exceptionListener = getListenerFromProgramArguments(args);
        SetSource<String> symbolSource = new ClasspathSymbolSource(exceptionListener);

        Set<String> symbols = symbolSource.getSource();
        System.out.println(String.format("Sourced %1$d symbols", symbols.size()));
        Map<String,Series> seriesMap = loadStockSeries(symbols, firstPeriod, secondPeriod, exceptionListener);

        for (Map.Entry<String,Series> record : seriesMap.entrySet()) {
            System.out.println(record.getValue());
            System.out.flush();
        }
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

    private static Map<String,Series> loadStockSeries(Set<String> symbols, long firstPeriod, long secondPeriod, Listener<Exception> exceptionListener) {
        int nProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(nProcessors);
        Map<String,Series> seriesMap = new HashMap<>();

        long startLoad = System.currentTimeMillis();
        long nRecords = 0L;
        for (String symbol : symbols) {
            Callable<Series> loader = new YahooFinanceStockScrapper(symbol, firstPeriod, secondPeriod, exceptionListener);
            Future<Series> future = service.submit(loader);
            try {
                Series series = future.get();
                seriesMap.put(symbol, series);
                nRecords += series.getLength();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
                System.err.flush();
            }
        }
        service.shutdown();
        long stopLoad = System.currentTimeMillis();
        System.out.println(String.format("Loaded %1$d records %2$d series in %3$f/s", nRecords, symbols.size(), (stopLoad - startLoad)/1000.0));
        return seriesMap;
    }
}
