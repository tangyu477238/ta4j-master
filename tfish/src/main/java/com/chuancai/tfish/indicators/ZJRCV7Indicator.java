package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;


public class ZJRCV7Indicator extends CachedIndicator<Num> {

    private final int barCount;

    private final Num factor1;
    private final Num factor2;
    private final Num factor3;
    private final Num factor4;
    private final Num factor5;
    private final Num factor6;

    private final EMAIndicator VAR1;
    private final EMAIndicator VAR2;
    private final EMAIndicator VAR3;
    private final EMAIndicator VAR4;
    private final EMAIndicator VAR5;
    private final EMAIndicator VAR6;

    /**
     * Constructor.
     *
     * @param series   the series
     * @param barCount the time frame
     */
    public ZJRCV7Indicator(BarSeries series, int barCount,double factor1,double factor2,double factor3,double factor4,double factor5,double factor6) {
        super(series);
        this.barCount = barCount;

        this.factor1 = numOf(factor1);
        this.factor2 = numOf(factor2);
        this.factor3 = numOf(factor3);
        this.factor4 = numOf(factor4);
        this.factor5 = numOf(factor5);
        this.factor6 = numOf(factor6);

        this.VAR1 = new EMAIndicator(new HighestValueIndicator(new HighPriceIndicator(series), 500),barCount);
        this.VAR2 = new EMAIndicator(new HighestValueIndicator(new HighPriceIndicator(series), 250),barCount);
        this.VAR3 = new EMAIndicator(new HighestValueIndicator(new HighPriceIndicator(series), 90),barCount);

        this.VAR4 = new EMAIndicator(new LowestValueIndicator(new LowPriceIndicator(series), 500),barCount);
        this.VAR5 = new EMAIndicator(new LowestValueIndicator(new LowPriceIndicator(series), 250),barCount);
        this.VAR6 = new EMAIndicator(new LowestValueIndicator(new LowPriceIndicator(series), 90),barCount);
    }



    @Override
    protected Num calculate(int index) {

        final Num v1 = VAR1.getValue(index);
        final Num v2 = VAR2.getValue(index);
        final Num v3 = VAR3.getValue(index);
        final Num v4 = VAR4.getValue(index);
        final Num v5 = VAR5.getValue(index);
        final Num v6 = VAR6.getValue(index);
//        VAR7:EMA((VAR4*0.96+VAR5*0.96+VAR6*0.96+VAR1*0.558+VAR2*0.558+VAR3*0.558)/6,21);
        final Num v7 = (v4.multipliedBy(factor1)
                .plus(v5.multipliedBy(factor2))
                .plus(v6.multipliedBy(factor3))
                .plus(v1.multipliedBy(factor4))
                .plus(v2.multipliedBy(factor5))
                .plus(v3.multipliedBy(factor6))).dividedBy(numOf(6));

        if (index==VAR1.getBarSeries().getEndIndex()){
//            System.out.println("v1:"+v1.toString());
//            System.out.println("v2:"+v2.toString());
//            System.out.println("v3:"+v3.toString());
//            System.out.println("v4:"+v4.toString());
//            System.out.println("v5:"+v5.toString());
//            System.out.println("v6:"+v6.toString());
        }

        return v7;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
