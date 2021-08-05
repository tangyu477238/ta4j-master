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

import com.chuancai.tfish.indicators.*;
import com.chuancai.tfish.model.Gupiao;
import com.chuancai.tfish.model.GupiaoKline;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoKlineRepository;
import com.chuancai.tfish.repository.GupiaoRepository;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quickstart for ta4j.
 *
 * Global example.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class EmaTest {

    @Resource
    private GupiaoKlineRepository gupiaoKlineRepository;

    @Resource
    private GupiaoRepository gupiaoRepository;

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;




    @Test
    public void getData() {

        String bondId = "603168";
        BarSeries series  = getBarSeries(bondId); //获取数据
        addIndicator(series, bondId); //数据存储
        buildStrategy(series, bondId); //执行策略



        List<Gupiao> list = gupiaoRepository.findAll();
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



    private void addMacdIndicator(BarSeries series,String bondId){
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        MACDIndicator  dif = new MACDIndicator(closePrice,12,135);
        EMAIndicator dea = new EMAIndicator(dif, 9);
        EmaMACDIndicator emaMACDIndicator = new EmaMACDIndicator(series, dif, dea);
        int nbBars = series.getBarCount();
        for (int i = 0; i < nbBars; i++) {
//            if (i>=nbBars-3 && i<nbBars) {

                GupiaoXinhao gupiaoXinhao = new GupiaoXinhao();
                gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
                gupiaoXinhao.setSymbol(bondId);
                gupiaoXinhao.setType(0);
                gupiaoXinhao.setTypeName("macd");
                gupiaoXinhao.setSj1(new BigDecimal(dif.getValue(i).doubleValue()));
                gupiaoXinhao.setSj2(new BigDecimal(dea.getValue(i).doubleValue()));
                gupiaoXinhao.setSj3(new BigDecimal(emaMACDIndicator.getValue(i).doubleValue()));
                gupiaoXinhaoRepository.save(gupiaoXinhao);

//            }
        }

    }

    private void addZjrcIndicator(BarSeries series,String bondId){
        ZJRCIndicator zjrc = new ZJRCIndicator(series);
        int nbBars = series.getBarCount();
        for (int i = 0; i < nbBars; i++) {
            GupiaoXinhao gupiaoXinhao = new GupiaoXinhao();
            gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            gupiaoXinhao.setSymbol(bondId);
            gupiaoXinhao.setType(1);
            gupiaoXinhao.setTypeName("zjrc");
            gupiaoXinhao.setSj1(new BigDecimal(zjrc.getValue(i).doubleValue()));
            gupiaoXinhaoRepository.save(gupiaoXinhao);
        }

    }

    private void addJAXIndicator(BarSeries series,String bondId){
        JAXIndicator jax = new JAXIndicator(series);
        int nbBars = series.getBarCount();
        for (int i = 0; i < nbBars; i++) {
            GupiaoXinhao gupiaoXinhao = new GupiaoXinhao();
            gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            gupiaoXinhao.setSymbol(bondId);
            gupiaoXinhao.setType(2);
            gupiaoXinhao.setTypeName("jax");
            gupiaoXinhao.setSj1(new BigDecimal(jax.getValue(i).doubleValue()));
            gupiaoXinhaoRepository.save(gupiaoXinhao);
        }

    }

    private void addMaIndicator(BarSeries series,String bondId){
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator ma5 = new SMAIndicator(closePrice, 5); //收盘价>5均线
        SMAIndicator ma10 = new SMAIndicator(closePrice, 10); //收盘价>10均线
        SMAIndicator ma20 = new SMAIndicator(closePrice, 20);
        SMAIndicator ma60 = new SMAIndicator(closePrice, 60);
        SMAIndicator ma120 = new SMAIndicator(closePrice, 120);
        SMAIndicator ma240 = new SMAIndicator(closePrice, 240);

        int nbBars = series.getBarCount();
        for (int i = 0; i < nbBars; i++) {
            GupiaoXinhao gupiaoXinhao = new GupiaoXinhao();
            gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            gupiaoXinhao.setSymbol(bondId);
            gupiaoXinhao.setType(3);
            gupiaoXinhao.setTypeName("ma");
            gupiaoXinhao.setSj1(new BigDecimal(ma5.getValue(i).doubleValue()));
            gupiaoXinhao.setSj2(new BigDecimal(ma10.getValue(i).doubleValue()));
            gupiaoXinhao.setSj3(new BigDecimal(ma20.getValue(i).doubleValue()));
            gupiaoXinhao.setSj4(new BigDecimal(ma60.getValue(i).doubleValue()));
            gupiaoXinhao.setSj5(new BigDecimal(ma120.getValue(i).doubleValue()));
            gupiaoXinhao.setSj5(new BigDecimal(ma240.getValue(i).doubleValue()));
            gupiaoXinhaoRepository.save(gupiaoXinhao);
        }

    }


    private void addIndicator(BarSeries series,String bondId){

        addMacdIndicator(series, bondId); //macd
        addZjrcIndicator(series, bondId); //zjrc //资金入场
        addJAXIndicator(series, bondId); //jax //济安线
        addMaIndicator(series, bondId); //ma //均线


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
        return new OverIndicatorRule(closePrice, ma5)
                .and(new OverIndicatorRule(ma5, ma10))
                .and(new OverIndicatorRule(ma10, ma20))
                .and(new OverIndicatorRule(ma20, ma60))
//                .and(new OverIndicatorRule(ma60, ma120))
                .and(new OverIndicatorRule(ma60, ma250))
                .and(new IsEqualRule(xlpl, 1))
                .and(new OverIndicatorRule(closePrice, openPrice));

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



    private BarSeries getBarSeries(String bondId){
        List<GupiaoKline> gupiaoKline = gupiaoKlineRepository.getSymbolTop(bondId,1500);
        if (gupiaoKline.isEmpty()) return null;

        // 反转lists
        Collections.reverse(gupiaoKline);
        BarSeries series = new BaseBarSeriesBuilder().withName(bondId).build();
        gupiaoKline.forEach(kline ->{
//            System.out.println(kline.getTimestamp()+"--"+kline.getOpen()+"--"+kline.getHigh()+"--"+kline.getLow()+"--"+ kline.getClose()+"--"+kline.getVolume());
            ZonedDateTime date = ZonedDateTime.parse(kline.getTimestamp() + " PST",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.S z"));
            series.addBar(new BaseBar(Duration.ofDays(1), date,
                    new BigDecimal(kline.getOpen()), new BigDecimal(kline.getHigh()),
                    new BigDecimal(kline.getLow()), new BigDecimal(kline.getClose()), new BigDecimal(kline.getVolume())));
        });
        return series;
    }
}
