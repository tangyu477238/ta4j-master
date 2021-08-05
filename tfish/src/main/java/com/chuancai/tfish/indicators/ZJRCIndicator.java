package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;




public class ZJRCIndicator extends CachedIndicator<Num> {

//    private final int barCount;
    private final ZJRCV10Indicator VAR10;
    private final EMAIndicator zjrc;

    /**
     * Constructor.
     *
     * @param series   the series
     *
     */
    public ZJRCIndicator(BarSeries series) {
        super(series);
        this.VAR10 = new ZJRCV10Indicator(series);
        this.zjrc = new EMAIndicator(new ZJRCVZJIndicator(series,21),3);
    }



    @Override
    protected Num calculate(int index) {
        if (index==0){
            return numOf(0);
        }
        //资金入场:EMA(IF(L<=VARE,(VARD+VARF*2)/2,0),3)/618*VAR10;
        Num v10 = this.VAR10.getValue(index);
        Num zjrc = this.zjrc.getValue(index).dividedBy(numOf(618)).multipliedBy(v10);
        if (index==VAR10.getBarSeries().getEndIndex()){
//            System.out.println("zjrc:"+zjrc);
        }
        return zjrc;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() ;
    }
}
