package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;


public class ZJRCVZJIndicator extends CachedIndicator<Num> {

    private final int barCount;

    private final LowestValueIndicator lowestValueIndicator;
    private final HighestValueIndicator highestValueIndicator;
    private final EMAIndicator VARD;
    private final LowPriceIndicator lowPriceIndicator;

    /**
     * Constructor.
     *
     * @param series   the series
     * @param barCount the time frame
     */
    public ZJRCVZJIndicator(BarSeries series, int barCount) {
        super(series);
        this.barCount = barCount;
        this.VARD = new EMAIndicator(new ZJRCVDIndicator(series,barCount),3);
        this.lowPriceIndicator = new LowPriceIndicator(series);
        this.lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator,30);
        this.highestValueIndicator = new HighestValueIndicator(VARD,30);
    }



    @Override
    protected Num calculate(int index) {
        if (index==0){
            return numOf(0);
        }
        Num vd = VARD.getValue(index);
        Num ve = lowestValueIndicator.getValue(index);
        Num vf = highestValueIndicator.getValue(index);
        //IF(L<=VARE,(VARD+VARF*2)/2,0)
        Num rc ;
        if (lowPriceIndicator.getValue(index).isLessThanOrEqual(ve)) {
            rc =  vf.multipliedBy(numOf(2)).plus(vd).dividedBy(numOf(2));
        } else {
            rc =  numOf(0);
        }
        //资金入场:EMA(IF(L<=VARE,(VARD+VARF*2)/2,0),3)/618*VAR10;
        if (index==lowPriceIndicator.getBarSeries().getEndIndex()){
//            System.out.println("rc:"+rc);
        }
        return rc;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
