package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.num.Num;


public class ZJRCVCIndicator extends CachedIndicator<Num> {

    private final MMAIndicator VC11;
    private final MMAIndicator VC22;

    /**
     * Constructor.
     *
     * @param series   the series
     *
     */
    public ZJRCVCIndicator(BarSeries series) {
        super(series);
        this.VC11 = new MMAIndicator(new ZJRCVC1Indicator(series),3) ;///////////////////////////对不上去
        this.VC22 = new MMAIndicator(new ZJRCVC2Indicator(series),3) ;

    }



    @Override
    protected Num calculate(int index) {
        if (index==0){
            return numOf(0);
        }
//     VARC:=SMA(ABS(LOW-VARB),3,1)/SMA(MAX(LOW-VARB,0),3,1)*100;
        Num vc11 = this.VC11.getValue(index);
        Num vc22 = this.VC22.getValue(index);
        if (vc11.isZero()||vc22.isZero()){
            return numOf(0);
        }
        if (index==VC22.getBarSeries().getEndIndex()){
//            System.out.println("vc11:"+vc11);
//            System.out.println("vc22:"+vc22);
        }
        Num c = vc11.dividedBy(vc22).multipliedBy(numOf(100));
        return c;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() ;
    }
}
