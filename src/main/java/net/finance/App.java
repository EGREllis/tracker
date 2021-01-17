package net.finance;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.OpenAxisAdapter;
import net.finance.tracker.domain.calculation.CorrelationMatrix;
import net.finance.tracker.domain.calculation.CorrelationMatrixCalculator;
import net.finance.tracker.domain.calculation.CorrelationMatrixCalculatorImpl;
import net.finance.tracker.domain.series.FinanceData;
import net.finance.tracker.domain.series.Series;
import net.finance.tracker.util.logging.LoggingListener;
import net.finance.tracker.util.logging.NoopListener;
import net.finance.tracker.io.scrapper.FinanceDataLoader;
import net.finance.tracker.util.pattern.Listener;

import java.io.FileWriter;
import java.io.Writer;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    public static void main( String[] args ) throws Exception {
        Listener<Exception> exceptionListener = getListenerFromProgramArguments(args);

        FinanceData data = new FinanceDataLoader(exceptionListener).call();
        System.out.println(data.getSummary());

        CorrelationMatrixCalculator calculator = new CorrelationMatrixCalculatorImpl(exceptionListener, MathContext.DECIMAL64);
        int nThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(nThreads);
        try {
            CorrelationMatrix stocks = correlate(data.getStocks(), calculator, service);
            CorrelationMatrix rates = correlate(data.getRates(), calculator, service);
        } finally {
            service.shutdown();
        }
    }

    private static void writeFile(String fileName, String contents) {
        try {
            Writer writer = new FileWriter(fileName);
            writer.write(contents);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static CorrelationMatrix correlate(Map<String, Series> seriesMap, CorrelationMatrixCalculator calculator, ExecutorService service) {
        List<Axis> axes = new ArrayList<>(seriesMap.size() * 2);
        for (Map.Entry<String, Series> entry : seriesMap.entrySet()) {
            axes.add(new OpenAxisAdapter(entry.getValue()));
        }
        return calculator.calculate(axes, service);
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
