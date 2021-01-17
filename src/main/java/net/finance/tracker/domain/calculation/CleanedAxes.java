package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;

public class CleanedAxes {
    private final Axis aClean;
    private final Axis bClean;

    public CleanedAxes(Axis aClean, Axis bClean) {
        this.aClean = aClean;
        this.bClean = bClean;
    }

    public Axis getAClean() {
        return aClean;
    }

    public Axis getBClean() {
        return bClean;
    }
}
