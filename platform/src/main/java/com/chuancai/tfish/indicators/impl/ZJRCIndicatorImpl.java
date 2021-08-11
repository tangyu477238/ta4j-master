package com.chuancai.tfish.indicators.impl;

import com.chuancai.tfish.indicators.ZJRCIndicator;
import com.chuancai.tfish.indicators.ZJRCV10Indicator;
import com.chuancai.tfish.indicators.ZJRCVZJIndicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;


public class ZJRCIndicatorImpl extends CachedIndicator<Num> {

    private final ZJRCIndicator zjrc;
    private final LowPriceIndicator lowPriceIndicator;

    /**
     * Constructor.
     *
     * @param series   the series
     *
     */
    public ZJRCIndicatorImpl(BarSeries series) {
        super(series);
        this.zjrc = new ZJRCIndicator(series);
        this.lowPriceIndicator = new LowPriceIndicator(series);
    }



    @Override
    protected Num calculate(int index) {
        if (index < 3){
            return numOf(0);
        }
        if (index==zjrc.getBarSeries().getEndIndex()){
            log.info(zjrc.getValue(index).toString());
        }
        Num num_3 = zjrc.getValue(index-3);
        Num num_2 = zjrc.getValue(index-2);
        Num num_1 = zjrc.getValue(index-1);
        Num num = zjrc.getValue(index);

        if (num_3.isLessThan(num_2)
                && num_2.isGreaterThan(num_1)
                && num_1.isGreaterThan(num)){
            return lowPriceIndicator.getValue(-2);
        }
        return numOf(0);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() ;
    }
}
