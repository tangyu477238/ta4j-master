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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;


public class JAXIndicator extends CachedIndicator<Num> {

    private static final long serialVersionUID = -6899062131135971403L;

//
    private final JAXA1Indicator jaxa1Indicator;
    private final JAXAaIndicator jaxAaIndicator;
    private final DMAIndicator dmaIndicator;
    private final SMAIndicator sma1;

    public JAXIndicator(BarSeries series) {
        super(series);
        this.jaxa1Indicator = new JAXA1Indicator(series);
        this.jaxAaIndicator = new JAXAaIndicator(series);
        this.dmaIndicator = new DMAIndicator(jaxa1Indicator, jaxAaIndicator); //济安值
        this.sma1 = new SMAIndicator(new JAXMa1Indicator(series),3);

    }

    @Override
    protected Num calculate(int index) {
        Num jax = dmaIndicator.getValue(index); //济安值
        Num ma1 = sma1.getValue(index);
        Num maaa = ma1.minus(jax).dividedBy(jax).dividedBy(numOf(3));
        Num tmp = ma1.minus(maaa.multipliedBy(ma1)); //价格实时值
        return jax.minus(tmp); //小于0则为红，大于0则为绿
    }

}
