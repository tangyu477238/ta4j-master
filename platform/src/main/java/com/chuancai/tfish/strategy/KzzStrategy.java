package com.chuancai.tfish.strategy;

import com.chuancai.tfish.enums.KlineEnum;
import com.chuancai.tfish.indicators.EmaMACDIndicator;
import com.chuancai.tfish.indicators.JAXIndicator;
import com.chuancai.tfish.indicators.XLPLAIndicator;
import com.chuancai.tfish.indicators.ZJRCIndicator;
import com.chuancai.tfish.model.GupiaoKline;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoKlineRepository;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.util.ComUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Data
@Slf4j
public class KzzStrategy {

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;

    @Resource
    private GupiaoKlineRepository gupiaoKlineRepository;


    public List<GupiaoKline> listKine(String bondId, Integer period){
        List<GupiaoKline> listKline = null;
        if (period == KlineEnum.K_5M.getId()){
            listKline = gupiaoKlineRepository.getKline5m(bondId);
        } else if (period == KlineEnum.K_15M.getId()){
            listKline = gupiaoKlineRepository.getKline15m(bondId);
        } else if (period == KlineEnum.K_30M.getId()){
            listKline = gupiaoKlineRepository.getKline30m(bondId);
        }  else if (period == KlineEnum.K_60M.getId()){
            listKline = gupiaoKlineRepository.getKline60m(bondId);
        } else if (period == KlineEnum.K_120M.getId()){
            listKline = gupiaoKlineRepository.getKline120m(bondId);
        }else if (period == KlineEnum.K_1D.getId()){
            listKline = gupiaoKlineRepository.getKline(bondId);
        }
        return listKline;
    }

    public BarSeries getBarSeries(List<GupiaoKline> listKline){
        BarSeries series = new BaseBarSeriesBuilder().withName(listKline.get(0).getSymbol()).build();
//        StringBuffer stringBuffer;
        for(GupiaoKline kline : listKline){
//            stringBuffer = new StringBuffer(kline.getTimestamp().toString());
//            ZonedDateTime date = ZonedDateTime.parse(stringBuffer.append(" PST"), DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.S z"));
//            log.info(date.toString());
//            log.info(kline.getTimestamp()+"--"+kline.getOpen()+"--"+kline.getHigh()+"--"+kline.getLow()+"--"+ kline.getClose()+"--"+kline.getVolume());
            series.addBar(ZonedDateTime.ofInstant(kline.getTimestamp().toInstant(), ZoneId.systemDefault()),
                    kline.getOpen(), kline.getHigh(), kline.getLow(), kline.getClose(), kline.getVolume(), kline.getAmount());
        }
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

//            GupiaoXinhao gupiaoXinhao = new GupiaoXinhao();
//            gupiaoXinhao.setBizDate(series.getBar(i).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
//            gupiaoXinhao.setSymbol(bondId);
//            gupiaoXinhao.setType(0);
//            gupiaoXinhao.setTypeName("macd");
//
//            GupiaoXinhao gupiaoXinhao_1  = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDate(bondId,
//                    gupiaoXinhao.getTypeName(),gupiaoXinhao.getBizDate());
//            if (gupiaoXinhao_1!=null){
//                gupiaoXinhao.setId(gupiaoXinhao_1.getId());
//            }
//            gupiaoXinhao.setSj1(new BigDecimal(dif.getValue(i).doubleValue()));
//            gupiaoXinhao.setSj2(new BigDecimal(dea.getValue(i).doubleValue()));
//            gupiaoXinhao.setSj3(new BigDecimal(emaMACDIndicator.getValue(i).doubleValue()));
//            gupiaoXinhaoRepository.save(gupiaoXinhao);

//            }
        }

    }

//    private String formatterDate(ZonedDateTime bizDate, Integer period){
//        String pdate = bizDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T"," ");
//        if (period==5){
//            return pdate.substring(0,16);
//        } else if (period==30){
//            return pdate.substring(0,16);
//        } else if (period==101){
//            return pdate.substring(0,10);
//        }
//        return null;
//    }

    private BigDecimal getValue(ZJRCIndicator zjrc, Integer index){
        if (index<0){
            return new BigDecimal(0);
        }
        return new BigDecimal(zjrc.getValue(index).doubleValue());
    }

    private String getBizDate(List<GupiaoKline> listKline, Integer index){
        if (index<0){
            return null;
        }
        return listKline.get(index).getBizDate();
    }

    private BigDecimal getLowPrice(List<GupiaoKline> listKline, Integer index){
        if (index<0){
            return new BigDecimal(0);
        }
        return listKline.get(index).getLow();
    }

    public List<GupiaoXinhao> addZjrcIndicator(BarSeries series, List<GupiaoKline> listKline){
        List<GupiaoXinhao> list = new ArrayList<>();
        if (ComUtil.isEmpty(series)){
            return list;
        }
        GupiaoXinhao gupiaoXinhao;
        ZJRCIndicator zjrc = new ZJRCIndicator(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            gupiaoXinhao = new GupiaoXinhao();

            gupiaoXinhao.setSymbol(series.getName());
            gupiaoXinhao.setType(1);
            gupiaoXinhao.setTypeName("zjrc");
            gupiaoXinhao.setPeriod(listKline.get(i).getPeriod());

            gupiaoXinhao.setSj1(getValue(zjrc, i));
            gupiaoXinhao.setSj2(getValue(zjrc, i-1));
            gupiaoXinhao.setSj3(getValue(zjrc, i-2));
            gupiaoXinhao.setSj4(getValue(zjrc, i-3));
            gupiaoXinhao.setSj5(getValue(zjrc, i-4));
            gupiaoXinhao.setSj6(getValue(zjrc, i-5));
            gupiaoXinhao.setSj7(getValue(zjrc, i-6));
            gupiaoXinhao.setSj8(getValue(zjrc, i-7));
            gupiaoXinhao.setSj9(getValue(zjrc, i-8));
            gupiaoXinhao.setSj10(getValue(zjrc, i-9));


            gupiaoXinhao.setBizDate(getBizDate(listKline, i));
            gupiaoXinhao.setBizDate2(getBizDate(listKline, i-1));
            gupiaoXinhao.setBizDate3(getBizDate(listKline, i-2));
            gupiaoXinhao.setBizDate4(getBizDate(listKline, i-3));
            gupiaoXinhao.setBizDate5(getBizDate(listKline, i-4));
            gupiaoXinhao.setBizDate6(getBizDate(listKline, i-5));
            gupiaoXinhao.setBizDate7(getBizDate(listKline, i-6));
            gupiaoXinhao.setBizDate8(getBizDate(listKline, i-7));
            gupiaoXinhao.setBizDate9(getBizDate(listKline, i-8));
            gupiaoXinhao.setBizDate10(getBizDate(listKline, i-9));

            gupiaoXinhao.setPrice1(getLowPrice(listKline, i));
            gupiaoXinhao.setPrice2(getLowPrice(listKline, i-1));
            gupiaoXinhao.setPrice3(getLowPrice(listKline, i-2));
            gupiaoXinhao.setPrice4(getLowPrice(listKline, i-3));
            gupiaoXinhao.setPrice5(getLowPrice(listKline, i-4));
            gupiaoXinhao.setPrice6(getLowPrice(listKline, i-5));
            gupiaoXinhao.setPrice7(getLowPrice(listKline, i-6));
            gupiaoXinhao.setPrice8(getLowPrice(listKline, i-7));
            gupiaoXinhao.setPrice9(getLowPrice(listKline, i-8));
            gupiaoXinhao.setPrice10(getLowPrice(listKline, i-9));
            list.add(gupiaoXinhao);
        }

        return list;
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


    public void addIndicator(BarSeries series){

//        addMacdIndicator(series, bondId); //macd
//        addZjrcIndicator(series); //zjrc //资金入场
//        addJaxIndicator(series, bondId); //jax //济安线
//        addXlplIndicator(series, bondId); //xlpl //西拉派罗
//        addMaIndicator(series, bondId); //ma //均线


    }
}
