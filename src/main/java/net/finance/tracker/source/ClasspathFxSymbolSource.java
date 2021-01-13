package net.finance.tracker.source;

import net.finance.tracker.pattern.Listener;
import net.finance.tracker.pattern.SetSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

public class ClasspathFxSymbolSource implements SetSource<String> {
    private Listener<Exception> listener;

    public ClasspathFxSymbolSource(Listener<Exception> listener) {
        this.listener = listener;
    }

    @Override
    public Set<String> getSource() {
        Set<String> fxSymbols = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("data/fx_symbols.dat")))) {
            String line;
            while ( (line = reader.readLine()) != null) {
                fxSymbols.add(line);
            }
        } catch (Exception e) {
            listener.listen(e);
        }
        return fxSymbols;
    }
}
