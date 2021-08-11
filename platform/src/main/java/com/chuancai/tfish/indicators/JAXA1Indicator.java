/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.chuancai.tfish.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;


public class JAXA1Indicator extends CachedIndicator<Num> {

    private static final long serialVersionUID = -6899062131135971403L;

    private final ClosePriceIndicator closePriceIndicator;
    private final LowPriceIndicator lowPriceIndicator;
    private final HighPriceIndicator highPriceIndicator;

    public JAXA1Indicator(BarSeries series) {
        super(series);
        this.closePriceIndicator = new ClosePriceIndicator(series);
        this.lowPriceIndicator = new LowPriceIndicator(series);
        this.highPriceIndicator = new HighPriceIndicator(series);
    }

    @Override
    protected Num calculate(int index) {

        Num a1 = closePriceIndicator.getValue(index).multipliedBy(numOf(2))
                .plus(highPriceIndicator.getValue(index)).plus(lowPriceIndicator.getValue(index));
//        if (index>=closePriceIndicator.getBarSeries().getEndIndex()){
//            log.info("a1:"+closePriceIndicator.getValue(index).multipliedBy(numOf(2))+":"+highPriceIndicator.getValue(index)+":"+lowPriceIndicator.getValue(index));
//        }
        return a1.dividedBy(numOf(4));
    }

}
