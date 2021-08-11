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
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;


public class BuyEmaMACDIndicator extends CachedIndicator<Num> {

    private static final long serialVersionUID = -6899062131135971403L;

    private final ClosePriceIndicator closePriceIndicator;

    private final EmaMACDIndicator emaMACDIndicator;

    private final MACDIndicator dif;

    private final SMAIndicator smaIndicator5;
    private final SMAIndicator smaIndicator10;
    private final SMAIndicator smaIndicator20;
    private final SMAIndicator smaIndicator60;
    private final SMAIndicator smaIndicator120;
    private final SMAIndicator smaIndicator250;

    /**
     * Constructor.
     *
     */
    public BuyEmaMACDIndicator(BarSeries series) {
        super(series);
        this.closePriceIndicator = new ClosePriceIndicator(series);

        this.dif = new MACDIndicator(closePriceIndicator,12,135);
        this.emaMACDIndicator = new EmaMACDIndicator(series);
        this.smaIndicator5 = new SMAIndicator(closePriceIndicator,5);
        this.smaIndicator10 = new SMAIndicator(closePriceIndicator,10);
        this.smaIndicator20 = new SMAIndicator(closePriceIndicator,20);
        this.smaIndicator60 = new SMAIndicator(closePriceIndicator,60);

        this.smaIndicator120 = new SMAIndicator(closePriceIndicator,120);
        this.smaIndicator250 = new SMAIndicator(closePriceIndicator,250);
    }

    @Override
    protected Num calculate(int index) {
        if (index<5){
            return numOf(0);
        }
        Num end = emaMACDIndicator.getValue(index);
        Num end1 = emaMACDIndicator.getValue(index-1);
        Num end2 = emaMACDIndicator.getValue(index-2);
        Num end3 = emaMACDIndicator.getValue(index-3);
        Num end4 = emaMACDIndicator.getValue(index-4);
//        if (index>emaMACDIndicator.getBarSeries().getEndIndex()-3) {
//            System.out.println(this.dif.getValue(index)+"--"+end+"--"+end1+"--"+end2+"--"+end3+"--"+end4+"---");
//            System.out.println(this.smaIndicator5.getValue(index)+"--"+this.smaIndicator10.getValue(index)+"--"+
//                    this.smaIndicator20.getValue(index)+"--"+this.smaIndicator60.getValue(index)+"--"+
//                    this.smaIndicator120.getValue(index)+"--"+this.smaIndicator250.getValue(index)+"---");
//        }
//        if (getMa60(index) && getUnder(end,end1,end2,end3,end4)  && getDifUpLine(index)){
//            return numOf(111);
//        } else
        if (getUnder(end,end1,end2,end3,end4) && getDifUpLine(index)){
            return numOf(11);
        } else if (getUnder(end,end1,end2,end3,end4)){
            return numOf(1);
        }
        return numOf(0);
    }

    //ma
    private boolean getMa60(int index){
        return  this.closePriceIndicator.getValue(index).isGreaterThanOrEqual(smaIndicator60.getValue(index));
    }
    //macd
    private boolean getMacdUpLine(Num end,Num end1,Num end2,Num end3,Num end4){
//        end4.multipliedBy(end3).isGreaterThanOrEqual(end3.multipliedBy(end2)) &&
        return end.minus(end1).isGreaterThanOrEqual(end1.minus(end2))
                && getUnder(end,end1,end2,end3,end4);
    }
    //dif
    private boolean getDifUpLine(int index){
        return this.dif.getValue(index).isGreaterThanOrEqual(numOf(-0.3));
    }
    //顶
    private boolean getUpside(Num end,Num end1,Num end2,Num end3,Num end4){
        if (end4.isLessThanOrEqual(end3) && end3.isLessThanOrEqual(end2)
                && end2.isGreaterThanOrEqual(end1) && end1.isLessThanOrEqual(end)){
            return true;
        }
        return false;
    }
    //底
    private boolean getUnder(Num end,Num end1,Num end2,Num end3,Num end4){
        if (end.isLessThanOrEqual(numOf(0))
//                && end.isGreaterThanOrEqual(numOf(-0.1))
                && end4.isGreaterThanOrEqual(end3)
                && end3.isGreaterThanOrEqual(end2)
                && end2.isLessThanOrEqual(end1)
                && end1.isLessThanOrEqual(end)){
            return true;
        }
        return false;
    }
}
