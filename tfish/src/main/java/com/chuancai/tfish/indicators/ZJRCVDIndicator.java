package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;


public class ZJRCVDIndicator extends CachedIndicator<Num> {

    private final int barCount;

    private final ClosePriceIndicator closePriceIndicator;
    private final EMAIndicator VARA;
    private final ZJRCVCIndicator VARC;

    /**
     * Constructor.
     *
     * @param series   the series
     * @param barCount the time frame
     */
    public ZJRCVDIndicator(BarSeries series, int barCount) {
        super(series);
        this.barCount = barCount;
        this.closePriceIndicator = new ClosePriceIndicator(series);
        this.VARA = new EMAIndicator(new ZJRCVAIndicator(series,barCount),barCount);
        this.VARC = new ZJRCVCIndicator(series);
    }



    @Override
    protected Num calculate(int index) {
        if (index==0){
            return numOf(0);
        }
//        VARD:=EMA(IF(CLOSE*1.35<=VARA,VARC*10,VARC/10),3);
        Num vd00 = closePriceIndicator.getValue(index).multipliedBy(numOf(1.35));
        Num va = VARA.getValue(index);
        Num vc = VARC.getValue(index);
        if (vc.isZero()){
            return numOf(0);
        }
        boolean vd0 = vd00.isLessThanOrEqual(va);
        Num vd ;
        if (vd0) {
            vd =  vc.multipliedBy(numOf(10));
        } else {
            vd =  vc.dividedBy(numOf(10));
        }
        if (index>VARC.getBarSeries().getEndIndex()-30){
//            System.out.println("vA:"+va);
//            System.out.println("vC:"+vc);
//            System.out.println("vd:"+vd);
        }
        return vd;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
