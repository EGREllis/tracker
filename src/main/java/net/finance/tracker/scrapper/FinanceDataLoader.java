package net.finance.tracker.scrapper;

import net.finance.tracker.domain.FinanceData;
import net.finance.tracker.domain.Series;
import net.finance.tracker.pattern.Listener;
import net.finance.tracker.pattern.SetSource;
import net.finance.tracker.source.ClasspathFxSymbolSource;
import net.finance.tracker.source.ClasspathStockSymbolSource;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FinanceDataLoader implements Callable<FinanceData> {
    private final Listener<Exception> exceptionListener;

    public FinanceDataLoader(Listener<Exception> exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    @Override
    public FinanceData call() throws Exception {
        long firstStockPeriod = 0L;
        long secondStockPeriod = 1610407859L;
        long firstFxPeriod = 1578845052L;
        long secondFxPeriod = 1610467452L;
        SetSource<String> stockSymbolSource = new ClasspathStockSymbolSource(exceptionListener);
        SetSource<String> fxSymbolSource = new ClasspathFxSymbolSource(exceptionListener);

        Set<String> stockSymbols = stockSymbolSource.getSource();
        Set<String> fxSymbols = fxSymbolSource.getSource();

        int nThreads = Math.max(stockSymbols.size(), fxSymbols.size());
        ExecutorService service = Executors.newFixedThreadPool(nThreads);

        System.out.println(String.format("Sourced %1$d stock symbols", stockSymbols.size()));
        Map<String, Series> stockMap = loadStockSeries(stockSymbols, firstStockPeriod, secondStockPeriod, exceptionListener, service);

        System.out.println(String.format("Sourced %1$d fx symbols", fxSymbols.size()));
        Map<String,Series> fxMap = loadFxMap(fxSymbols, firstFxPeriod, secondFxPeriod, exceptionListener, service);

        service.shutdown();
        return new FinanceData(stockMap, fxMap);
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
