package net.finance;

import net.finance.tracker.domain.Series;
import net.finance.tracker.logging.LoggingListener;
import net.finance.tracker.logging.NoopListener;
import net.finance.tracker.scrapper.YahooFinanceStockScrapper;
import net.finance.tracker.util.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 */
public class App {
    public static void main( String[] args ) throws Exception {
        String[] symbols = new String[] {"MSFT", "USD", "TSLA", "LSE.L", "F", "GE", "AAPL", "BAC", "PFE", "AMD", "XOM", "NOK", "WFC", "T", "FB"};
        long firstPeriod = 0L;
        long secondPeriod = 1610407859L;
        Listener<Exception> exceptionListener = getListenerFromProgramArguments(args);

        int nProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(nProcessors);
        Map<String,Series> seriesMap = new HashMap<>();

        long startLoad = System.currentTimeMillis();
        for (String symbol : symbols) {
            Callable<Series> loader = new YahooFinanceStockScrapper(symbol, firstPeriod, secondPeriod, exceptionListener);
            Future<Series> future = service.submit(loader);
            try {
                seriesMap.put(symbol, future.get());
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
                System.err.flush();
            }
        }
        service.shutdown();
        long stopLoad = System.currentTimeMillis();
        System.out.println(String.format("Loaded %1$d symbols in %2$f/s", symbols.length, (stopLoad - startLoad)/1000.0));

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
}
