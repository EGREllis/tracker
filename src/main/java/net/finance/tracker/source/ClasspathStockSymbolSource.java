package net.finance.tracker.source;

import net.finance.tracker.pattern.Listener;
import net.finance.tracker.pattern.SetSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

public class ClasspathStockSymbolSource implements SetSource<String> {
    private final Listener<Exception> listener;

    public ClasspathStockSymbolSource(Listener<Exception> listener) {
        this.listener = listener;
    }

    @Override
    public Set<String> getSource() {
        Set<String> symbols = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("data/stock_symbols.dat")))) {
            String line;
            while ( (line = reader.readLine()) != null) {
                symbols.add(line);
            }
        } catch (Exception e) {
            listener.listen(e);
        }
        return symbols;
    }
}
