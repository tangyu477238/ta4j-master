package com.chuancai.tfish.util;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.PrecisionNum;
import ta4jexamples.indicators.IndicatorsToCsv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringUtil {
    public static void cvs(StringBuilder sb,String filename){
        /*
         * Writing CSV file
         */
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("target", filename)));
            writer.write(sb.toString());
        } catch (IOException ioe) {
            Logger.getLogger(IndicatorsToCsv.class.getName()).log(Level.SEVERE, "Unable to write CSV file", ioe);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static String getTds(BarSeries series, String bondId, TradingRecord tradingRecord){
        StringBuilder tds= new StringBuilder(
                "bondId,bizDate,type,index,price,amount\n");
        List<Trade> trades = tradingRecord.getTrades();
        trades.forEach(trade->{
            tds.append(bondId).append(',')
                    .append(series.getBar(trade.getEntry().getIndex()).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append(',')
                    .append(trade.getEntry().getType()).append(',')
                    .append(trade.getEntry().getIndex()).append(',')
                    .append(trade.getEntry().getPricePerAsset().multipliedBy(PrecisionNum.valueOf("-1"))).append(',')
                    .append(trade.getEntry().getAmount()).append('\n');

            tds.append(bondId).append(',')
                    .append(series.getBar(trade.getExit().getIndex()).getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append(',')
                    .append(trade.getExit().getType()).append(',')
                    .append(trade.getExit().getIndex()).append(',')
                    .append(trade.getExit().getPricePerAsset()).append(',')
                    .append(trade.getExit().getAmount()).append('\n');
        });
        StringUtil.cvs(tds,bondId+"orders.csv");
        return tds.toString();
    }
}
