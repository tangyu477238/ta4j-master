package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.num.Num;


public class ZJRCVAIndicator extends CachedIndicator<Num> {

    private final int barCount;

    private final EMAIndicator VAR7;
    private final EMAIndicator VAR8;
    private final EMAIndicator VAR9;

    /**
     * Constructor.
     *
     * @param series   the series
     * @param barCount the time frame
     */
    public ZJRCVAIndicator(BarSeries series, int barCount) {
        super(series);
        this.barCount = barCount;

        this.VAR7 = new EMAIndicator(new ZJRCV7Indicator(series,barCount,0.96,0.96,0.96,0.558,0.558,0.558),barCount);
        this.VAR8 = new EMAIndicator(new ZJRCV7Indicator(series,barCount,1.25,1.23,1.2,0.55,0.55,0.65),barCount);
        this.VAR9 = new EMAIndicator(new ZJRCV7Indicator(series,barCount,1.3,1.3,1.3,0.68,0.68,0.68),barCount);
    }



    @Override
    protected Num calculate(int index) {
        // VARA:=EMA((VAR7*3+VAR8*2+VAR9)/6*1.738,21);
        Num v7 = this.VAR7.getValue(index);
        Num v8 = this.VAR8.getValue(index);
        Num v9 = this.VAR9.getValue(index);
        if (index==VAR7.getBarSeries().getEndIndex()){
//            System.out.println("v7:"+v7);
//            System.out.println("v8:"+v8);
//            System.out.println("v9:"+v9);
        }
        Num sNum= v7.multipliedBy(numOf(3)).plus(v8.multipliedBy(numOf(2))).plus(v9);
        Num va = sNum.dividedBy(numOf(6)).multipliedBy(numOf(1.738));
        return va;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
