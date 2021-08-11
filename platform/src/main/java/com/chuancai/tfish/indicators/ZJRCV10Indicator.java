package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;


public class ZJRCV10Indicator extends CachedIndicator<Num> {

    private final SMAIndicator VAR10;

    /**
     * Constructor.
     *
     * @param series   the series
     *
     */
    public ZJRCV10Indicator(BarSeries series) {
        super(series);
        this.VAR10 = new SMAIndicator(new ClosePriceIndicator(series),58);

    }



    @Override
    protected Num calculate(int index) {
//       VAR10:=IF(MA(CLOSE,58),1,0);
        Num v10 = this.VAR10.getValue(index);
        if (index==VAR10.getBarSeries().getEndIndex()){
//            System.out.println("v10:"+v10.toString());
        }
        if (!v10.isZero()) {
            return numOf(1);
        } else {
            return numOf(0);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
