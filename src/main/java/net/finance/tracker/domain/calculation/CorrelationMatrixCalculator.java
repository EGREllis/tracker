package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface CorrelationMatrixCalculator {
    CorrelationMatrix calculate(List<Axis> axes, ExecutorService service);
}
