package net.finance.tracker.domain.calculation;

import net.finance.tracker.domain.axis.Axis;
import net.finance.tracker.domain.axis.SimpleAxis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AxisCleanerImpl implements AxisCleaner {
    @Override
    public CleanedAxes cleanAxes(Axis a, Axis b) {
        List<Object[]> records = new ArrayList<>();
        int aIndex = a.getLength()-1;
        int bIndex = b.getLength()-1;

        while (aIndex >= 0 && bIndex >= 0) {
            long aTime = a.getDate(aIndex).getTime();
            long bTime = b.getDate(bIndex).getTime();

            while (aIndex > 0 && bIndex > 0 && aTime != bTime) {
                if (aTime < bTime) {
                    bTime = b.getDate(--bIndex).getTime();
                } else if (bTime < aTime) {
                    aTime = a.getDate(--aIndex).getTime();
                }
            }

            if (aIndex >= 0 && bIndex >= 0 && aTime == bTime) {
                records.add(new Object[]{a.getValue(aIndex), b.getValue(bIndex), a.getDate(aIndex)});
                aIndex--;
                bIndex--;
            } else if (aIndex <= 0 || bIndex <= 0) {
                break;
            }
        }

        BigDecimal[] aClean = new BigDecimal[records.size()];
        BigDecimal[] bClean = new BigDecimal[records.size()];
        Date[] dates = new Date[records.size()];
        for (int i = 0; i < records.size(); i++) {
            Object[] row = records.get(i);
            aClean[i] = (BigDecimal)row[0];
            bClean[i] = (BigDecimal)row[1];
            dates[i] = (Date)row[2];
        }
        return new CleanedAxes(new SimpleAxis(a.getSymbol(), aClean, dates), new SimpleAxis(b.getSymbol(), bClean, dates));
    }
}
