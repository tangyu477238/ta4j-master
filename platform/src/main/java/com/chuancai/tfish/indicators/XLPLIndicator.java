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
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;


public class XLPLIndicator extends CachedIndicator<Num> {

    private static final long serialVersionUID = -6899062131135971403L;

    private final ClosePriceIndicator closePriceIndicator;
    private final EMAIndicator e1;
    private final XLPLAIndicator xlplaIndicator;

    public XLPLIndicator(BarSeries series) {
        super(series);
        this.closePriceIndicator = new ClosePriceIndicator(series);
        this.e1 = new EMAIndicator(new EMAIndicator(closePriceIndicator,13),13);
        this.xlplaIndicator = new XLPLAIndicator(series);

    }

    @Override
    protected Num calculate(int index) {
        Num xlpl = xlplaIndicator.getValue(index);
        Num xlpl1 = xlplaIndicator.getValue(index-1);
        if (xlpl.isGreaterThanOrEqual(xlpl1)
//                && xlpl.isLessThanOrEqual(numOf(5))
                && xlpl.isGreaterThanOrEqual(numOf(0))){ //???
            return numOf(1);
        } else if (xlpl.isLessThan(xlpl1)
                && xlpl.isGreaterThanOrEqual(numOf(10))){ //????????????10
            return numOf(2); //??????
        }
        return numOf(0);
    }

}
