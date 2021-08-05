package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;


public class ZJRCVC2Indicator extends CachedIndicator<Num> {

    private final LowPriceIndicator lowPriceIndicator;


    /**
     * Constructor.
     *
     * @param series   the series
     *
     */
    public ZJRCVC2Indicator(BarSeries series) {
        super(series);
        this.lowPriceIndicator = new LowPriceIndicator(series);
    }



    @Override
    protected Num calculate(int index) {
        if (index==0){
            return numOf(0);
        }
        //MAX(LOW-VARB,0)
        Num vb = lowPriceIndicator.getValue(index-1);
        Num low = lowPriceIndicator.getValue(index);
        if (vb.isEqual(low)){
            return numOf(0);
        }
        Num c2 = low.minus(vb).max(numOf(0));
        if (index==lowPriceIndicator.getBarSeries().getEndIndex()){
//            System.out.println("c2:"+c2);
        }
        return c2;
//     VARC:=SMA(ABS(LOW-VARB),3,1)/SMA(MAX(LOW-VARB,0),3,1)*100;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() ;
    }
}
