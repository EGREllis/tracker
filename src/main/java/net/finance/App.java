package net.finance;

import net.finance.tracker.domain.series.FinanceData;
import net.finance.tracker.logging.LoggingListener;
import net.finance.tracker.logging.NoopListener;
import net.finance.tracker.scrapper.FinanceDataLoader;
import net.finance.tracker.pattern.Listener;

public class App {
    public static void main( String[] args ) throws Exception {
        Listener<Exception> exceptionListener = getListenerFromProgramArguments(args);

        FinanceData data = new FinanceDataLoader(exceptionListener).call();
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
