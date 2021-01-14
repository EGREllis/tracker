package net.finance.tracker.domain.series;

import net.finance.tracker.domain.series.DividendSeries;

import java.math.BigDecimal;
import java.util.Date;

public class NasdaqDividendSeries implements DividendSeries {
    private final Date[] effectiveDates;
    private final BigDecimal[] dividendPayment;
    private final Date[] declarationDate;
    private final Date[] recordDate;

    public NasdaqDividendSeries(Date[] effectiveDates, BigDecimal[] dividendPayment, Date[] declarationDate, Date[] recordDate) {
        this.effectiveDates = effectiveDates;
        this.dividendPayment =  dividendPayment;
        this.declarationDate = declarationDate;
        this.recordDate = recordDate;
    }

    public Date getEffectiveDate(int i) {
        return effectiveDates[i];
    }

    public BigDecimal getDividendPayment(int i) {
        return dividendPayment[i];
    }

    public Date getDeclarationDate(int i) {
        return declarationDate[i];
    }

    public Date getRecordDate(int i) {
        return recordDate[i];
    }
}
