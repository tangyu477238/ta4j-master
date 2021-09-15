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
package com.chuancai.tfish;

import com.chuancai.tfish.indicators.BuyEmaMACDIndicator;
import com.chuancai.tfish.indicators.XLPLIndicator;
import com.chuancai.tfish.indicators.impl.ZJRCIndicatorImpl;
import com.chuancai.tfish.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.trading.rules.IsEqualRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

/**
 * Quickstart for ta4j.
 *
 * Global example.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class EmaTest {





    @Test
    public void getData() {

        String bondId = "123031";
//        BarSeries series  = getBarSeries(bondId); //获取数据
//        addIndicator(series, bondId); //数据存储
//        buildStrategy(series, bondId); //执行策略



//        List<Gupiao> list = gupiaoRepository.findAll();
//        Collections.shuffle(list);
//        list.forEach(gupiao -> {
//            try {
//
//                String bondId = gupiao.getSymbol();
//                BarSeries series  = getBarSeries(bondId); //获取数据
//                addIndicator(series, bondId); //数据存储
//                buildStrategy(series, bondId); //执行策略
//
//            }catch (Exception e){}
//        });






    }




    public void buildStrategy(BarSeries series,String bondId) {

        // Building the trading strategy
        Strategy strategy = buildStrategy(series);

        // Running the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
//        Number of trades for the strategy
        System.out.println("交易次数: " + tradingRecord.getTradeCount());
        // Analysis Total profit for the strategy
        System.out.println(
                "该策略总利润: " + new TotalProfitCriterion().calculate(series, tradingRecord));

        StringUtil.getTds(series, bondId, tradingRecord); //生成xls

//         Analysis

        // Getting the profitable trades ratio //获利交易比率Profitable trades ratio
        AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
        System.out.println("获利交易比率: " + profitTradesRatio.calculate(series, tradingRecord));
        // Getting the reward-risk ratio 风险比（回报率）Reward-risk ratio
        AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
        System.out.println("风险比（回报率）: " + rewardRiskRatio.calculate(series, tradingRecord));

        // Total profit of our strategy 总利润Our profit vs buy-and-hold profit
        // vs total profit of a buy-and-hold strategy
        AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
        System.out.println("总利润: " + vsBuyAndHold.calculate(series, tradingRecord));

//         Your turn!
    }



    private void save(BarSeries series, String bondId){


//
//
//        GupiaoXinhao xlplxh = new GupiaoXinhao();
//        xlplxh.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
//        xlplxh.setSymbol(bondId);
//        xlplxh.setType(1);
//        xlplxh.setTypeName("xlpl");
//        xlplxh.setSj1(new BigDecimal(xlpl.getValue(i).doubleValue()));
//        gupiaoXinhaoRepository.save(xlplxh);
//
//        GupiaoXinhao jaxxx = new GupiaoXinhao();
//        jaxxx.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
//        jaxxx.setSymbol(bondId);
//        jaxxx.setType(2);
//        jaxxx.setTypeName("jax");
//        jaxxx.setSj1(new BigDecimal(jaxIndicator.getValue(i).doubleValue()));
//        gupiaoXinhaoRepository.save(jaxxx);

    }

    /**
     * @param series the bar series
     * @return the moving momentum strategy
     */
    public  Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        // Entry rule
        Rule entryRule = getEntryRule(series);
        // Exit rule
        Rule exitRule = getExitRule(series);

        return new BaseStrategy(entryRule, exitRule);
    }


    /***
     *  介入
     * @param series
     * @return
     */
    private Rule getEntryRule(BarSeries series){
        OpenPriceIndicator  openPrice= new OpenPriceIndicator(series);

        //        // Entry rule
//        Rule entryRule = new IsEqualRule(jaxIndicator, 1)
//                .and(getMa(closePrice))
//                .and(new IsEqualRule(xlpl, 1)); // Signal 2
//        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
//                .and(new CrossedDownIndicatorRule(stochasticOscillK, 20)) // Signal 1
//                ; // Signal 2
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        BuyEmaMACDIndicator emaMacd = new BuyEmaMACDIndicator(series);
        XLPLIndicator xlpl = new XLPLIndicator(series);

        SMAIndicator ma5 = new SMAIndicator(closePrice, 5); //收盘价>5均线
        SMAIndicator ma10 = new SMAIndicator(closePrice, 10); //收盘价>10均线
        SMAIndicator ma20 = new SMAIndicator(closePrice, 20);
        SMAIndicator ma60 = new SMAIndicator(closePrice, 60);
        SMAIndicator ma120 = new SMAIndicator(closePrice, 120);
        SMAIndicator ma250 = new SMAIndicator(closePrice, 250);

        ZJRCIndicatorImpl zjrc = new ZJRCIndicatorImpl(series);

        return new OverIndicatorRule(zjrc, 0);

//        return new OverIndicatorRule(closePrice, ma5)
//                .and(new OverIndicatorRule(ma5, ma10))
//                .and(new OverIndicatorRule(ma10, ma20))
//                .and(new OverIndicatorRule(ma20, ma60))
////                .and(new OverIndicatorRule(ma60, ma120))
//                .and(new OverIndicatorRule(ma60, ma250))
//                .and(new IsEqualRule(xlpl, 1))
//                .and(new OverIndicatorRule(closePrice, openPrice));

//        return new OverIndicatorRule(ma20, ma60)
////                .and(new IsEqualRule(xlpl, 1))
//                .and(new IsEqualRule(emaMacd, 11))
//                .and(new OverIndicatorRule(closePrice, openPrice));
    }

    /**
     * 退出
     * @param series
     * @return
     */
    private Rule getExitRule(BarSeries series){

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);


//        Rule exitRule = new StopGainRule(closePrice,15).or(new StopLossRule(closePrice,5));
//        Rule exitRule =  new IsEqualRule(jaxIndicator, 0)
//                .or(new IsEqualRule(xlpl, 0).and(new UnderIndicatorRule(closePrice, ma10))) ;



        SMAIndicator ma10 = new SMAIndicator(closePrice, 10); //收盘价>10均线
        SMAIndicator ma20 = new SMAIndicator(closePrice, 20);
        SMAIndicator ma60 = new SMAIndicator(closePrice, 60);
        SMAIndicator ma120 = new SMAIndicator(closePrice, 120);
        SMAIndicator ma250 = new SMAIndicator(closePrice, 250);
//        return new OverIndicatorRule(closePrice, ma10)
//                .and(new OverIndicatorRule(ma10, ma20))
//                .and(new OverIndicatorRule(ma20, ma60))
//                .and(new OverIndicatorRule(ma60, ma120))
//                .and(new OverIndicatorRule(ma120, ma250));

//        return new UnderIndicatorRule(ma120, ma250).or(new StopGainRule(closePrice,15));

//        return  new StopGainRule(closePrice,15).or(new UnderIndicatorRule(closePrice, ma20));

        XLPLIndicator xlpl = new XLPLIndicator(series);
        return  new UnderIndicatorRule(closePrice, ma20)
                .or(new IsEqualRule(xlpl, 2));
    }




}
