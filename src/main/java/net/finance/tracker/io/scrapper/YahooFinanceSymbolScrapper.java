package net.finance.tracker.io.scrapper;

import net.finance.tracker.util.pattern.Listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.concurrent.Callable;

/*
This class is not functional - Yahoo finance uses react/javascript
 */
public class YahooFinanceSymbolScrapper implements Callable<Set<String>> {
    private static final String TEMPLATE_URL = "https://finance.yahoo.com/losers?offset=0&count=100";
    private final Listener<Exception> exceptionListener;

    public YahooFinanceSymbolScrapper(Listener<Exception> exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    @Override
    public Set<String> call() throws Exception {
        URL url = new URL(TEMPLATE_URL);
        URLConnection connection = url.openConnection();
        connection.connect();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ( (line = reader.readLine()) != null ) {
                System.out.println(line);
            }
        } catch (Exception e) {
            exceptionListener.listen(e);
        }

        return null;
    }
}
