package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;

public interface AxisCleaner {
    CleanedAxes cleanAxes(Axis a, Axis b);
}
