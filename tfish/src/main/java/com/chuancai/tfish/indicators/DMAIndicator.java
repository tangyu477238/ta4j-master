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

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;


public class DMAIndicator extends CachedIndicator<Num> {

    private static final long serialVersionUID = -6899062131135971403L;

    private final Indicator<Num> indicator;
    private final Indicator<Num> indicator2;
//
    public DMAIndicator(Indicator<Num> indicator, Indicator<Num> indicator2) {
        super(indicator);
        this.indicator = indicator;
        this.indicator2 = indicator2;
    }


    @Override
    protected Num calculate(int index) {
        Num c = indicator.getValue(index);
        Num a = indicator2.getValue(index);
        if (index==0){
            return c;
        }
        Num a1 = c.multipliedBy(a);
        Num x1 = numOf(1).minus(a);
        Num prevValue = this.getValue(index - 1);
        Num x2 = x1.multipliedBy(prevValue);
        Num x3 = a1.plus(x2);

//        if (index==indicator.getBarSeries().getEndIndex()){
//            System.out.println("c:"+c);
//            System.out.println("a:"+a);
//            System.out.println("a1:"+a1);
//            System.out.println("x1:"+x1);
//            System.out.println("prevValue:"+prevValue);
//            System.out.println("x2:"+x2);
//            System.out.println("x3:"+x3);
//        }

        return  x3;
    }

}
