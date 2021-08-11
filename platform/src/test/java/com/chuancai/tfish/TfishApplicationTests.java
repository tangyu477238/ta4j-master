package com.chuancai.tfish;

import com.chuancai.tfish.indicators.ZJRCIndicator;
import com.chuancai.tfish.model.GupiaoKline;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.repository.GupiaoKlineRepository;
import com.chuancai.tfish.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.ta4j.core.*;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Slf4j
class TfishApplicationTests {


//
//
//    @Test
//    void contextLoads() {
//        //List<JslKzz> jslKzzes = jslKzzRepository.findAll();
//        StringBuilder sb = new StringBuilder(
//                "bondId,TradeCount,Total,Profitable,Reward-risk,profit\n");
//
//        StringBuilder tds= new StringBuilder(
//                "bondId,bizDate,type,index,price,amount\n");
//
//
//        getData("000002",sb,tds);
//        StringUtil.cvs(sb,"indicators.csv");
//
//    }
//
//    @Resource
//    private GupiaoXinhaoRepository gupiaoXinhaoRepository;
//    @Resource
//    private GupiaoKlineRepository gupiaoKlineRepository;
//
//
//    private BarSeries getBarSeries(String bondId){
//        List<GupiaoKline> gupiaoKline = gupiaoKlineRepository.getSymbolTop(bondId,10000);
//        if (gupiaoKline.isEmpty()) return null;
//
//        // 反转lists
//        Collections.reverse(gupiaoKline);
//        BarSeries series = new BaseBarSeriesBuilder().withName(bondId).build();
//        gupiaoKline.forEach(kline ->{
////            System.out.println(kline.getTimestamp()+"--"+kline.getOpen()+"--"+kline.getHigh()+"--"+kline.getLow()+"--"+ kline.getClose()+"--"+kline.getVolume());
//            ZonedDateTime date = ZonedDateTime.parse(kline.getTimestamp() + " PST",
//                    DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.S z"));
//            series.addBar(new BaseBar(Duration.ofDays(1), date,
//                    new BigDecimal(kline.getOpen()), new BigDecimal(kline.getHigh()),
//                    new BigDecimal(kline.getLow()), new BigDecimal(kline.getClose()), new BigDecimal(kline.getVolume())));
//        });
//        return series;
//    }
//
//    String getTds(BarSeries series,String bondId, TradingRecord tradingRecord){
//        StringBuilder tds= new StringBuilder(
//                "bondId,bizDate,type,index,price,amount\n");
//        List<Trade> trades = tradingRecord.getTrades();
//        trades.forEach(trade->{
//            tds.append(bondId).append(',')
//                    .append(series.getBar(trade.getEntry().getIndex()).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append(',')
//                    .append(trade.getEntry().getType()).append(',')
//                    .append(trade.getEntry().getIndex()).append(',')
//                    .append(trade.getEntry().getPricePerAsset().multipliedBy(PrecisionNum.valueOf("-1"))).append(',')
//                    .append(trade.getEntry().getAmount()).append('\n');
//            tds.append(bondId).append(',')
//                    .append(series.getBar(trade.getExit().getIndex()).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append(',')
//                    .append(trade.getExit().getType()).append(',')
//                    .append(trade.getExit().getIndex()).append(',')
//                    .append(trade.getExit().getPricePerAsset()).append(',')
//                    .append(trade.getExit().getAmount()).append('\n');
//        });
//        StringUtil.cvs(tds,"orders.csv");
//        return tds.toString();
//    }
//
//    public  void getData(String bondId,StringBuilder sb,StringBuilder tds) {
//
//        BarSeries series  = getBarSeries(bondId);
//
//
//
//        /*
//         * Building header
//         */
////        StringBuilder sb = new StringBuilder(
////                "timestamp,close,zjrc\n");
////        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//
////        ZJRCVDIndicator d = new ZJRCVDIndicator(series,21);
////        EMAIndicator vd = new EMAIndicator(vd,3);
//
////        System.out.println(VAR1.getValue(series.getBarCount()-1).toString());
////        System.out.println(VAR2.getValue(series.getBarCount()-1).toString());
////        System.out.println(VAR3.getValue(series.getBarCount()-1).toString());
////        System.out.println("--------------------");
////        System.out.println(VAR4.getValue(series.getBarCount()-1).toString());
////        System.out.println(VAR5.getValue(series.getBarCount()-1).toString());
////        System.out.println(VAR6.getValue(series.getBarCount()-1).toString());
//
////        EMAIndicator  vd = new EMAIndicator(new ZJRCVDIndicator(series,21),3);
////        HighestValueIndicator  vf = new HighestValueIndicator(vd,30);
//
////
////        ZJRCIndicator zjrc = new ZJRCIndicator(series);
////
////        /*
////         * Adding indicators values
////         */
////        final int nbBars = series.getBarCount();
////        for (int i = 0; i < nbBars; i++) {
////            if (i>=nbBars-10 && i<nbBars) {
//////                System.out.println(zjrc.getValue(i).toString());
////                sb.append(series.getBar(i).getEndTime()).append(',').append(closePrice.getValue(i)).append(',')
//////                    .append(vd.getValue(i)).append(',')
////                        .append(zjrc.getValue(i)).append('\n');
////            }
////        }
////
////        /*
////         * Writing CSV file
////         */
////        BufferedWriter writer = null;
////        try {
////            writer = new BufferedWriter(new FileWriter(new File("target", "indicators.csv")));
////            writer.write(sb.toString());
////        } catch (IOException ioe) {
////            Logger.getLogger(IndicatorsToCsv.class.getName()).log(Level.SEVERE, "Unable to write CSV file", ioe);
////        } finally {
////            try {
////                if (writer != null) {
////                    writer.close();
////                }
////            } catch (IOException ioe) {
////                ioe.printStackTrace();
////            }
////        }
////        if(true)return;
//
//
//
//
//
//
//
//        // Building the trading strategy
//        Strategy strategy = buildZjrcStrategy(series);
//
//        // Running the strategy
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        TradingRecord tradingRecord = seriesManager.run(strategy);
////        Number of trades for the strategy
//        System.out.println(bondId+"交易次数: " + tradingRecord.getTradeCount());
//        // Analysis Total profit for the strategy
//        System.out.println(
//                "该策略总利润: " + new TotalProfitCriterion().calculate(series, tradingRecord));
//
//
//        getTds(series,bondId,tradingRecord);
//
////         Analysis
//
//        // Getting the cash flow of the resulting trades  //获取交易产生的现金流//获取交易产生的现金流
//        CashFlow cashFlow = new CashFlow(series, tradingRecord);
//
//        // Getting the profitable trades ratio  //获得有利可图的交易比率//获得有利可图的交易比率
//        AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
////        System.out.println("盈利交易比Profitable trades ratio: " + profitTradesRatio.calculate(series, tradingRecord));
//        // Getting the reward-risk ratio//获得奖励风险比//获得奖励风险比
//        AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
////        System.out.println("报酬-风险比Reward-risk ratio: " + rewardRiskRatio.calculate(series, tradingRecord));
//        // Total profit of our strategy//我们策略的总利润//我们策略的总利润
//        // vs total profit of a buy-and-hold strategy // vs并购策略的总利润// vs并购策略的总利润
//        AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
////        System.out.println("我们的利润vs买入和持有利润Our profit vs buy-and-hold profit: " + vsBuyAndHold.calculate(series, tradingRecord));
//
//
//        sb.append(bondId).append(',')
//                .append(tradingRecord.getTradeCount()).append(',')
//                .append(new TotalProfitCriterion().calculate(series, tradingRecord)).append(',')
//                .append(profitTradesRatio.calculate(series, tradingRecord)).append(',')
//                .append(rewardRiskRatio.calculate(series, tradingRecord)).append(',')
//                .append(vsBuyAndHold.calculate(series, tradingRecord)).append('\n');
//
//
//
//    }
//
//
//    /**
//     * @param series the bar series
//     * @return the moving momentum strategy
//     */
//    public static Strategy buildStrategy(BarSeries series) {
//        if (series == null) {
//            throw new IllegalArgumentException("Series cannot be null");
//        }
//
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//
//        // The bias is bullish when the shorter-moving average moves above the longer
//        // moving average.
//        // The bias is bearish when the shorter-moving average moves below the longer
//        // moving average.
//        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
//        EMAIndicator longEma = new EMAIndicator(closePrice, 26);
//
//        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, 14);
//
//        MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
//        EMAIndicator emaMacd = new EMAIndicator(macd, 18);
//
//        // Entry rule
//        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
//                .and(new CrossedDownIndicatorRule(stochasticOscillK, 20)) // Signal 1
//                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2
//
//        // Exit rule
//        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
//                .and(new CrossedUpIndicatorRule(stochasticOscillK, 20)) // Signal 1
//                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2
//
//        return new BaseStrategy(entryRule, exitRule);
//    }
//
//
//    /**
//     * @param series the bar series
//     * @return the moving momentum strategy
//     */
//    public static Strategy buildStrategy1(BarSeries series) {
//        if (series == null) {
//            throw new IllegalArgumentException("Series cannot be null");
//        }
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        WMAIndicator ema10 = new WMAIndicator(closePrice, 10);
//        WMAIndicator ema20 = new WMAIndicator(closePrice, 20);
//        // Entry rule
//        Rule entryRule = new OverIndicatorRule(closePrice, ema20) ;
//        // Exit rule
//        Rule exitRule = new UnderIndicatorRule(closePrice, ema20); // Trend
////        .or(new StopLossRule(closePrice, series.numOf(2))) //2%止损
//        return new BaseStrategy(entryRule, exitRule);
//    }
//
//
//    /**
//     * @param series the bar series
//     * @return the moving momentum strategy
//     */
//    public static Strategy buildZjrcStrategy(BarSeries series) {
//        if (series == null) {
//            throw new IllegalArgumentException("Series cannot be null");
//        }
//
//        ZJRCIndicator zjrc = new ZJRCIndicator(series);
//
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        EMAIndicator ema20 = new EMAIndicator(closePrice, 20);
//        EMAIndicator ema60 = new EMAIndicator(closePrice, 60);
//        // Entry rule
//        Rule entryRule = new OverIndicatorRule(ema20, ema60) ;
//        // Exit rule
//        Rule exitRule = new StopGainRule(closePrice, series.numOf(10))  //10%盈利卖出
//        .or(new StopLossRule(closePrice, series.numOf(1))); //2%止损
//        return new BaseStrategy(entryRule, exitRule);
//    }

}
