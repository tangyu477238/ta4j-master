package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;


public class ZJRCVC1Indicator extends CachedIndicator<Num> {

    private final LowPriceIndicator lowPriceIndicator;


    /**
     * Constructor.
     *
     * @param series   the series
     *
     */
    public ZJRCVC1Indicator(BarSeries series) {
        super(series);
        this.lowPriceIndicator = new LowPriceIndicator(series);
    }



    @Override
    protected Num calculate(int index) {
        if (index==0){
            return numOf(0);
        }
        //ABS(LOW-VARB)
        final Num vb = lowPriceIndicator.getValue(index-1);
        final Num low = lowPriceIndicator.getValue(index);
        if (vb.isEqual(low)){
            return numOf(0);
        }
        final Num x1 = low.minus(vb);
        final Num c1 = x1.abs();
        if (index==lowPriceIndicator.getBarSeries().getEndIndex()){
//            System.out.println("c1:"+c1);
        }
        return c1;
//     VARC:=SMA(ABS(LOW-VARB),3,1)/SMA(MAX(LOW-VARB,0),3,1)*100;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() ;
    }
}
