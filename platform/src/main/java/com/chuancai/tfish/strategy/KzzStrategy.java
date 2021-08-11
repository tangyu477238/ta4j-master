package com.chuancai.tfish.strategy;

import com.chuancai.tfish.indicators.EmaMACDIndicator;
import com.chuancai.tfish.indicators.JAXIndicator;
import com.chuancai.tfish.indicators.XLPLAIndicator;
import com.chuancai.tfish.indicators.ZJRCIndicator;
import com.chuancai.tfish.model.GupiaoKline5m;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoKline5mRepository;
import com.chuancai.tfish.repository.GupiaoRepository;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class KzzStrategy {

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;

    @Resource
    private GupiaoKline5mRepository gupiaoKline5mRepository;

    public BarSeries getBarSeries(String bondId){
        List<GupiaoKline5m> gupiaoKline = gupiaoKline5mRepository.getSymbolTop(bondId,300);
        if (gupiaoKline.isEmpty()) return null;

        // 反转lists
        Collections.reverse(gupiaoKline);
        BarSeries series = new BaseBarSeriesBuilder().withName(bondId).build();
        gupiaoKline.forEach(kline ->{
//            System.out.println(kline.getTimestamp()+"--"+kline.getOpen()+"--"+kline.getHigh()+"--"+kline.getLow()+"--"+ kline.getClose()+"--"+kline.getVolume());
            ZonedDateTime date = ZonedDateTime.parse(kline.getTimestamp() + " PST",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.S z"));
            series.addBar(date, new BigDecimal(kline.getOpen()), new BigDecimal(kline.getHigh()),
                    new BigDecimal(kline.getLow()), new BigDecimal(kline.getClose()), new BigDecimal(kline.getVolume()));
        });
        return series;
    }



    private void addMacdIndicator(BarSeries series,String bondId){
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        MACDIndicator dif = new MACDIndicator(closePrice,12,135);
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

            GupiaoXinhao gupiaoXinhao_1  = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDate(bondId,
                    gupiaoXinhao.getTypeName(),gupiaoXinhao.getBizDate());
            if (gupiaoXinhao_1!=null){
                gupiaoXinhao.setId(gupiaoXinhao_1.getId());
            }
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
            gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            gupiaoXinhao.setBizDate(gupiaoXinhao.getBizDate().replace("T"," ").substring(0,16));
            gupiaoXinhao.setSymbol(bondId);
            gupiaoXinhao.setType(1);
            gupiaoXinhao.setTypeName("zjrc");

            GupiaoXinhao gupiaoXinhao_1  = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDate(bondId,
                    gupiaoXinhao.getTypeName(),gupiaoXinhao.getBizDate());
            if (gupiaoXinhao_1 == null){
                gupiaoXinhao.setSj1(new BigDecimal(zjrc.getValue(i).doubleValue()));
                gupiaoXinhaoRepository.save(gupiaoXinhao);
                continue;
            }

            if (gupiaoXinhao.getBizDate().startsWith(DateTimeUtil.getBeforeDay(0))) { //如果是当天，请覆盖
                gupiaoXinhao.setId(gupiaoXinhao_1.getId());
                gupiaoXinhao.setSj1(new BigDecimal(zjrc.getValue(i).doubleValue()));
                gupiaoXinhaoRepository.save(gupiaoXinhao);
                continue;
            }



        }

    }

    private void addJaxIndicator(BarSeries series,String bondId){
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

    private void addXlplIndicator(BarSeries series,String bondId){
        XLPLAIndicator xlpl = new XLPLAIndicator(series);
        int nbBars = series.getBarCount();
        for (int i = 0; i < nbBars; i++) {
            GupiaoXinhao gupiaoXinhao = new GupiaoXinhao();
            gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            gupiaoXinhao.setSymbol(bondId);
            gupiaoXinhao.setType(2);
            gupiaoXinhao.setTypeName("xlpl");
            gupiaoXinhao.setSj1(new BigDecimal(xlpl.getValue(i).doubleValue()));
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


    public void addIndicator(BarSeries series,String bondId){

//        addMacdIndicator(series, bondId); //macd
        addZjrcIndicator(series, bondId); //zjrc //资金入场
//        addJaxIndicator(series, bondId); //jax //济安线
//        addXlplIndicator(series, bondId); //xlpl //西拉派罗
//        addMaIndicator(series, bondId); //ma //均线


    }
}
