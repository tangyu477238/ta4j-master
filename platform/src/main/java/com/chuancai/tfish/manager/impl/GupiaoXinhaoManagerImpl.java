package com.chuancai.tfish.manager.impl;

import com.chuancai.tfish.enums.KlineEnum;
import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.*;
import com.chuancai.tfish.repository.*;
import com.chuancai.tfish.strategy.KzzStrategy;
import com.chuancai.tfish.util.ComUtil;
import com.chuancai.tfish.util.DateTimeUtil;
import com.chuancai.tfish.util.ExecutorProcessPool;
import com.chuancai.tfish.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GupiaoXinhaoManagerImpl implements GupiaoXinhaoManager {

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;

    @Resource
    private KzzStrategy kzzStrategy;


    @Resource
    private GupiaoKlineRepository gupiaoKlineRepository; //获取day k线对象

    @Resource
    private GupiaoKline5mRepository gupiaoKline5mRepository; //获取5k线对象

    @Resource
    private GupiaoKline15mRepository gupiaoKline15mRepository; //获取15k线对象

    @Resource
    private GupiaoKline30mRepository gupiaoKline30mRepository; //获取30k线对象

    @Resource
    private GupiaoKline60mRepository gupiaoKline60mRepository; //获取60k线对象

    @Resource
    private GupiaoKline120mRepository gupiaoKline120mRepository; //获取120k线对象

    @Override
    public void saveGupiaoXinhao(List<GupiaoXinhao> list) {
        List<GupiaoXinhao> addList = new ArrayList();
        String maxBizDate = gupiaoXinhaoRepository.getMaxBizDate(list.get(0).getSymbol(),
                list.get(0).getPeriod(), list.get(0).getTypeName());
        for (GupiaoXinhao gupiaoXinhao : list) {
            if (!ComUtil.isEmpty(maxBizDate)
                    && maxBizDate.compareTo(gupiaoXinhao.getBizDate()) >= 0){ //已经存在的信号,不在计算和验证
                continue;
            }
            addList.add(gupiaoXinhao);
        }

//        addList = list.stream()
//                .filter(t->(!ComUtil.isEmpty(maxBizDate) && maxBizDate.compareTo(t.getBizDate()) >= 0))
//                .collect(Collectors.toList());


//        log.info("--------验证数-----"+DateTimeUtil.getSecondsOfTwoDate(date1,new Date())+"");date1 = new Date();
        if (ComUtil.isEmpty(addList)){
            return;
        }
        gupiaoXinhaoRepository.saveAll(addList);
//        log.info("--------存数-----"+DateTimeUtil.getSecondsOfTwoDate(date1,new Date())+"");

    }





    @Override
    public void sysnGupiaoXinhaoAll(Integer period) {
        List<String> list = gupiaoKlineRepository.listKzz();
        for (String symbol : list){
            try {
//                if(!"127015".equals(symbol)){
//                    continue;
//                }
                Runnable run = new GupiaoXinhaoManagerImpl.CalculateZjrcRunnable(symbol, period);
                ExecutorProcessPool.getInstance().executeByCustomThread(run);
                Runnable run1 = new GupiaoXinhaoManagerImpl.CalculateTrendRunnable(symbol, period);
                ExecutorProcessPool.getInstance().executeByCustomThread(run1);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public class CalculateZjrcRunnable implements Runnable{
        private String symbol;
        private Integer period;
        public CalculateZjrcRunnable(String symbol,Integer period){
            this.symbol=symbol;
            this.period = period;
        }
        @Override
        public void run(){
            calculateZjrc(symbol, period);
        }
    }

    public class CalculateTrendRunnable implements Runnable{
        private String symbol;
        private Integer period;
        public CalculateTrendRunnable(String symbol,Integer period){
            this.symbol=symbol;
            this.period = period;
        }
        @Override
        public void run(){
            //计算数据-------trend-------
            calculateTrend(symbol,period);
        }
    }


    private void calculateZjrc(String symbol, Integer period){
//        log.info(period+"-------calculate数据处理开始---" + symbol);
        List<GupiaoKline> listKline = kzzStrategy.listKine(symbol, period); //获取k数据
        if (ComUtil.isEmpty(listKline)){
            return;
        }
        Collections.reverse(listKline); // 反转lists
        BarSeries series = kzzStrategy.getBarSeries(listKline);  //初始化数据
/////////////////////////////////////Zjrc////////////////////////
        Date date1 = new Date();
        GupiaoXinhao zjrc = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(symbol,
                "zjrc", listKline.get(listKline.size()-1).getBizDate(), period); //验证是否已处理
        if (ComUtil.isEmpty(zjrc)){
            saveGupiaoXinhao(kzzStrategy.addZjrcIndicator(series, listKline)); //计算数据 zjrc
//            log.info(period+"-------calculateZjrc数据处理时长---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "-------"+ symbol);
        }

////////////////////////////////////////Ma/////////////////////
        date1 = new Date();
        GupiaoXinhao ma = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(symbol,
                "ma", listKline.get(listKline.size()-1).getBizDate(), period); //验证是否已处理
        if (ComUtil.isEmpty(ma)){
            saveGupiaoXinhao(kzzStrategy.addMaIndicator(series, listKline)); //计算数据 ma
//            log.info(period+"-------calculateMa数据处理时长---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "-------"+ symbol);
        }
    }


    /***
     * 数据返回给Kline原始数
     * @param tlist
     * @param map
     */
    private void toCopyKline(List<GupiaoKline> tlist, Map<String, GupiaoKline> map){
        for (GupiaoKline gupiaoKline : tlist){
            if (gupiaoKline.getIsMerge()==0){
                continue;
            }
            GupiaoKline newPro = map.get(gupiaoKline.getBizDate());
            gupiaoKline.setYiLow(newPro.getYiLow());
            gupiaoKline.setYiHigh(newPro.getYiHigh());
            gupiaoKline.setYiTrend(newPro.getYiTrend());
        }
        for (int i = tlist.size()-1; i >0; i--) { //包含处理后的数据
            if (tlist.get(i).getIsMerge()==0 && i < (tlist.size()-1)
                    && tlist.get(i+1).getYiHigh() != null){
                tlist.get(i).setYiLow(tlist.get(i+1).getYiLow());
                tlist.get(i).setYiHigh(tlist.get(i+1).getYiHigh());
                tlist.get(i).setYiTrend(tlist.get(i+1).getYiTrend());

                tlist.get(i).setUpPrice5(tlist.get(i+1).getUpPrice5());
                tlist.get(i).setDownPrice5(tlist.get(i+1).getDownPrice5());
                tlist.get(i).setUpPrice4(tlist.get(i+1).getUpPrice4());
                tlist.get(i).setDownPrice4(tlist.get(i+1).getDownPrice4());
                tlist.get(i).setUpPrice3(tlist.get(i+1).getUpPrice3());
                tlist.get(i).setDownPrice3(tlist.get(i+1).getDownPrice3());
                tlist.get(i).setUpPrice2(tlist.get(i+1).getUpPrice2());
                tlist.get(i).setDownPrice2(tlist.get(i+1).getDownPrice2());
                tlist.get(i).setUpPrice1(tlist.get(i+1).getUpPrice1());
                tlist.get(i).setDownPrice1(tlist.get(i+1).getDownPrice1());

                tlist.get(i).setBeforeDate(tlist.get(i+1).getBeforeDate());
                tlist.get(i).setAfterDate(tlist.get(i+1).getAfterDate());
                tlist.get(i).setBeforeDate2(tlist.get(i+1).getBeforeDate2());
                tlist.get(i).setAfterDate2(tlist.get(i+1).getAfterDate2());
                tlist.get(i).setBeforeDate3(tlist.get(i+1).getBeforeDate3());
                tlist.get(i).setAfterDate3(tlist.get(i+1).getAfterDate3());
                tlist.get(i).setBeforeDate4(tlist.get(i+1).getBeforeDate4());
                tlist.get(i).setAfterDate4(tlist.get(i+1).getAfterDate4());
                tlist.get(i).setBeforeDate5(tlist.get(i+1).getBeforeDate5());
                tlist.get(i).setAfterDate5(tlist.get(i+1).getAfterDate5());
                continue;
            }
        }
    }

    private void calculateTrend(String symbol, Integer period){
        Date date1 = new Date();
        List<GupiaoKline> listKline = kzzStrategy.listKine(symbol, period); //获取k数据
        if (ComUtil.isEmpty(listKline)){
            return;
        }
        Collections.reverse(listKline); // 反转lists
        calculateBase(listKline); //根据初步趋势线(trend),处理包含关系,计算出(NewHigh,NewLow,IsMerge)
        List<GupiaoKline> listDistinct = listKline.stream().filter(t->t.getIsMerge()==1).collect(Collectors.toList()); //去掉包含关系的K线集合
//        calculateBaseByMerge(listDistinct); //重新计算得出趋势线(yi_trend)
        Map<String, GupiaoKline> map = calculateDingDiByMerge(listDistinct); //根据(trend)进行计算趋势 yi_trend
        toCopyKline(listKline, map); //转换和筛选可用数据
        saveKline(listKline);
//        log.info(period+"-------calculateTrend数据处理时长-----" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "-------"+ symbol);
    }



    /**
     * 确认方向上的一笔
     * @param start
     * @param end
     * @param highPrice
     * @param lowPrice
     * @param listDistinct
     * @param trend
     */
    private void yiBiSure(int start, int end,BigDecimal highPrice, BigDecimal lowPrice, List<GupiaoKline> listDistinct, int trend){
        TrendDTO trendDTO = new TrendDTO();
        BeanUtils.copyProperties(listDistinct.get(start), trendDTO); //上一轮得基础数据复制过来
        for (int i = start+1; i<=end; i++){
            listDistinct.get(i).setYiTrend(trend);
            listDistinct.get(i).setYiHigh(highPrice);
            listDistinct.get(i).setYiLow(lowPrice);

            BeanUtils.copyProperties(trendDTO, listDistinct.get(i)); //放进新得对象
            if (trend == 0) { //下降趋势
                CopyTrendInfoDown(listDistinct.get(i));
                listDistinct.get(i).setDownPrice1(lowPrice);
                listDistinct.get(i).setBeforeDate(listDistinct.get(end).getBizDate());
                continue;
            }
            CopyTrendInfoUp(listDistinct.get(i));
            listDistinct.get(i).setUpPrice1(highPrice);
            listDistinct.get(i).setAfterDate(listDistinct.get(end).getBizDate());
        }
    }


    /****
     * 上升 复制
     * @param kline
     */
    private GupiaoKline CopyTrendInfoUp(GupiaoKline kline){
        kline.setUpPrice5(kline.getUpPrice4());
        kline.setUpPrice4(kline.getUpPrice3());
        kline.setUpPrice3(kline.getUpPrice2());
        kline.setUpPrice2(kline.getUpPrice1());

        kline.setAfterDate5(kline.getAfterDate4());
        kline.setAfterDate4(kline.getAfterDate3());
        kline.setAfterDate3(kline.getAfterDate2());
        kline.setAfterDate2(kline.getAfterDate());
        return kline;
    }

    /****
     * 下降 复制
     * @param kline
     */
    private GupiaoKline CopyTrendInfoDown(GupiaoKline kline){
        kline.setDownPrice5(kline.getDownPrice4());
        kline.setDownPrice4(kline.getDownPrice3());
        kline.setDownPrice3(kline.getDownPrice2());
        kline.setDownPrice2(kline.getDownPrice1());

        kline.setBeforeDate5(kline.getBeforeDate4());
        kline.setBeforeDate4(kline.getBeforeDate3());
        kline.setBeforeDate3(kline.getBeforeDate2());
        kline.setBeforeDate2(kline.getBeforeDate());
        return kline;
    }


    /**
     * 计算饱满
     * @param kline
     * @return
     */
    private boolean getSun(GupiaoKline kline){
        BigDecimal hl = kline.getHigh().subtract(kline.getLow());
        BigDecimal ho = kline.getHigh().subtract(kline.getOpen());
        BigDecimal cl = kline.getClose().subtract(kline.getLow());
        BigDecimal co = kline.getClose().subtract(kline.getOpen());
        BigDecimal v1 = hl.multiply(new BigDecimal(2)).subtract(co.abs());
        BigDecimal tv2 = kline.getClose().compareTo(kline.getOpen()) < 0 ?
                ho.add(cl).divide(v1,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) : new BigDecimal(50);
        BigDecimal v2 = kline.getClose().compareTo(kline.getOpen()) > 0 ?
                hl.divide(v1,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) : tv2;
        if (v2.intValue()>70){
            return true;
        }
        return false;
    }

    /***
     * 计算顶底
     * @param listDistinct
     */
    private Map<String, GupiaoKline> calculateDingDiByMerge(List<GupiaoKline> listDistinct ){
        int dingNum = 0;
        BigDecimal highPrice = new BigDecimal(0);
        BigDecimal highPrice_low = new BigDecimal(0); //检查成笔用的

        int diNum = 0 ;
        BigDecimal lowPrice = new BigDecimal(0);
        BigDecimal lowPrice_high = new BigDecimal(0); //检查成笔用的

        boolean isDing = true;
        for (int i = 0; i < listDistinct.size(); i++) {
            if (i<3){
                continue;
            }
            GupiaoKline firstPrevious = listDistinct.get(i-3);
            GupiaoKline previous = listDistinct.get(i-2);
            GupiaoKline before = listDistinct.get(i-1);
            GupiaoKline current = listDistinct.get(i);

            if (((previous.getTrend()==1 && before.getTrend()==1 && current.getTrend()==0)
                    || (previous.getTrend()==0 && before.getTrend()==1 && current.getTrend()==0))
                    && (i-1-diNum)>=4 //110或010为顶分型，且距离上次底大于4根k线
                    && before.getNewHigh().compareTo(lowPrice_high) > 0){
                if (isDing && highPrice.compareTo(before.getNewHigh())>0){//上次也是顶,上次>本次最高价
                    continue;
                }
                dingNum = i-1;
                highPrice = before.getNewHigh();
                isDing = true;
                if (previous.getNewLow().compareTo(current.getNewLow())<0){ //谁低取谁
                    highPrice_low = previous.getNewLow();
                } else {
                    highPrice_low = current.getNewLow();
                }
                yiBiSure(diNum, dingNum, highPrice, lowPrice, listDistinct,1); //向上一笔确认
            }

            if (((previous.getTrend()==0 && before.getTrend()==0 && current.getTrend()==1)
                    || (previous.getTrend()==1 && before.getTrend()==0 && current.getTrend()==1))
                    && (i-1-dingNum)>=4  //001或101为底分型，且距离上次顶大于4根k线
                    && before.getNewLow().compareTo(highPrice_low) < 0){ //底分型的最低要低于上次的顶分型区间
                if (!isDing && lowPrice.compareTo(before.getNewLow())<0){ //上次也是底,上次<本次最低价
                    current.setPc("1"); //双底,当前底比上一个底要高
                    continue;
                }
                if (isDing && lowPrice.compareTo(before.getNewLow())<0){
                    current.setPb("1"); //底--顶--底,当前底比上一个底要高
                }
                diNum = i-1;
                lowPrice = before.getNewLow();
                isDing = false;
                if (previous.getNewHigh().compareTo(current.getNewHigh())>0){ //谁高取谁
                    lowPrice_high = previous.getNewHigh();
                } else {
                    lowPrice_high = current.getNewHigh();
                }
                yiBiSure(dingNum, diNum, highPrice, lowPrice, listDistinct,0); //向下一笔确认
            }

            //当出现底分型后转折
            if (firstPrevious.getTrend()==0
                    && previous.getTrend()==0
                    && before.getTrend()==1
                    && current.getTrend()==1 && (i-2-dingNum)>=4){ //001为底分型，且距离上次顶大于4根k线

                if ("1".equals(before.getPc())
                        && getSun(current)
                        && current.getClose().compareTo(before.getHigh()) > 0
                        && current.getClose().compareTo(firstPrevious.getHigh()) > 0){ //双底
                    current.setPe("1");
                    current.setPePrice(previous.getNewLow()); //最低
                }

                if ("1".equals(before.getPb())
                        && getSun(current)
                        && current.getClose().compareTo(before.getHigh()) > 0
                        && current.getClose().compareTo(firstPrevious.getHigh()) > 0){ //停顿法
                    current.setPcf("1");
                    current.setPcfPrice(previous.getNewLow()); //最低
                }
            }


        }
        return listDistinct.stream().collect(Collectors.toMap(GupiaoKline::getBizDate, Function.identity()));
    }

    /**
     * 第一根处理逻辑
     * @param listKline
     */
    private void firstKlineByMerge(List<GupiaoKline> listKline){
        listKline.get(0).setMergeHigh(listKline.get(0).getNewHigh());
        listKline.get(0).setMergeLow(listKline.get(0).getNewLow());
        listKline.get(0).setYiTrend(0);
    }

    private List<GupiaoKline> calculateBaseByMerge(List<GupiaoKline> listKline){
//        int trendFlag = 0;
        for (int i = 0; i < listKline.size(); i++) {
            if (i == 0) {
                firstKlineByMerge(listKline); //首根逻辑
                continue;
            }
//            if (i-2 >= 0){ //且包含处理后为有效时,进行更新
//                trendFlag = listKline.get(i-2).getTrend(); //往前第2根走势
//            }
            GupiaoKline before = listKline.get(i-1); //前一根
            GupiaoKline current = listKline.get(i); //当前根

            current.setYiTrend(0);//设置为 false
            current.setBalance("0");
            if (isUpByMerge(before, current)){
                current.setBalance("1");
                current.setYiTrend(1);
            }
            current.setMergeHigh(current.getNewHigh());
            current.setMergeLow(current.getNewLow());
        }
        return listKline;
    }

//    /***
//     * 计算趋势的首根处理
//     * @param listKline
//     */
//    private void firstCalculateTrend(List<GupiaoKline> listKline){
//        if (!ComUtil.isEmpty(listKline.get(0).getTrend())) { //已计算,不需要处理
//            return;
//        }
//        listKline.get(0).setTrend(0); //如果第一条记录的趋势为空,则为下降处理
//        listKline.get(0).setAveragePrice(listKline.get(0).getHigh()
//                .add(listKline.get(0).getLow()).divide(new BigDecimal(2)));//均价
//    }


    /**
     * 第一根处理逻辑
     * @param kline
     */
    private void firstKline(GupiaoKline kline){
        if (!ComUtil.isEmpty(kline.getTrend())) { //已计算,不需要处理
            return;
        }
        kline.setTrend(0); //如果第一条记录的趋势为空,则为下降处理
//        kline.setAveragePrice(kline.getHigh()
//                .add(kline.getLow()).divide(new BigDecimal(2)));//均价

        kline.setNewHigh(kline.getHigh());
        kline.setNewLow(kline.getLow());
        kline.setIsMerge(1); //有效

        kline.setBeforeDate(kline.getBizDate());
        kline.setAfterDate(kline.getBizDate());
    }

//    /****
//     * 计算趋势参数
//     * @param listKline
//     * @return
//     */
//    private List<GupiaoKline> calculateTrend(List<GupiaoKline> listKline){
//        int trendFlag = 0;
//        for (int i = 0; i < listKline.size(); i++) {
//            if (i == 0) {
//                firstCalculateTrend(listKline); //首根逻辑
//                continue;
//            }
//            if ("2021-09-27 15:00".equals(listKline.get(i).getBizDate())){
//                log.info("");
//            }
////            if (i-2 >= 0 && listKline.get(i-2).getIsMerge()==1){ //且包含处理后为有效时,进行更新
////                trendFlag = listKline.get(i-2).getTrend(); //往前第2根走势
////            }
//
//            GupiaoKline before = listKline.get(i-1); //前一根
////            trendFlag = before.getTrend(); //往前第1根走势
//            if (before.getIsMerge()==1){ //且包含处理后为有效时,进行更新
//                trendFlag = before.getTrend(); //往前第1根走势
//            }
//            GupiaoKline current = listKline.get(i); //当前根
//
//            current.setTrend(0);//设置为 false
//            if (isUp(before, current, trendFlag)){ //判断是否为上升趋势
//                current.setTrend(1);
//            }
//            current.setIsMerge(1);//设置为 false
//            if (isMerge(before, current)){ //判断是否存在包含
//                before.setIsMerge(0);//前一天设置为无效
//            }
//            current.setNewHigh(getNewHigh(before, current, trendFlag));
//            current.setNewLow(getNewLow(before, current, trendFlag));
//        }
//        return listKline;
//    }
//
//    private Integer getTrend(Integer i, List<GupiaoKline> listKline){
//        GupiaoKline before = listKline.get(i-1); //前一根
//        before.setAveragePrice(before.getNewHigh()
//                .add(before.getNewLow()).divide(new BigDecimal(2)));//均价
//        GupiaoKline current = listKline.get(i); //当前根
//        current.setAveragePrice(current.getHigh()
//                .add(current.getLow()).divide(new BigDecimal(2)));//均价
//        if (i==1){
//            if(current.getAveragePrice().compareTo(before.getAveragePrice())>0){
//                return 1;
//            }
//            return 0;
//        }
//        GupiaoKline nextBefore = listKline.get(i-2); //前2根
//        nextBefore.setAveragePrice(nextBefore.getNewHigh()
//                .add(nextBefore.getNewLow()).divide(new BigDecimal(2)));//均价
//        if(current.getAveragePrice().compareTo(nextBefore.getAveragePrice())>0){
//            return 1;
//        }
//        return 0;//设置为 false
//    }
    private List<GupiaoKline> calculateBase(List<GupiaoKline> listKline){
        int trendFlag = 0;
        for (int i = 0; i < listKline.size(); i++) {
            if (i == 0) {
                firstKline(listKline.get(0)); //首根逻辑
                continue;
            }

//            if ("2021-09-27 15:00".equals(listKline.get(i).getBizDate())){
//                log.info("");
//            }
//            if (i-2 >= 0 && listKline.get(i-2).getIsMerge()==1){ //且包含处理后为有效时,进行更新
//                trendFlag = listKline.get(i-2).getTrend(); //往前第2根走势
//            }

            GupiaoKline before = listKline.get(i-1); //前一根
            trendFlag = before.getTrend(); //往前第1根走势

            GupiaoKline current = listKline.get(i); //当前根
//            current.setAveragePrice(current.getHigh()
//                    .add(current.getLow()).divide(new BigDecimal(2)));//均价
//            current.setTrend(getTrend(i, listKline));//判断趋势

            current.setTrend(0);//设置为 false
            if (isUp(before, current, trendFlag)){ //判断是否为上升趋势
                current.setTrend(1);
            }

            current.setIsMerge(1);//设置为 false
            if (isMerge(before, current)){ //判断是否存在包含
                before.setIsMerge(0);//前一天设置为无效
            }
            current.setNewHigh(getNewHigh(before, current, trendFlag));
            current.setNewLow(getNewLow(before, current, trendFlag));
        }
        return listKline;
    }

    /**
     *  获取低价
     * @param before
     * @param current
     * @return
     */
    private BigDecimal getNewLow(GupiaoKline before, GupiaoKline current, int trendFlag){
        if (current.getLow().compareTo(before.getNewLow()) > 0) {
            if (current.getHigh().compareTo(before.getNewHigh()) > 0) {
                return current.getLow();
            }
            if (trendFlag==1) {
                return current.getLow();
            }
            return before.getNewLow();
        }

        if (current.getLow().compareTo(before.getNewLow()) == 0){
            return current.getLow();
        }

        ////////////今天最低小于昨天最低////////////////

        if (current.getHigh().compareTo(before.getNewHigh()) >= 0){  //今天最高 > 昨天最高
            if (trendFlag==0){
                return current.getLow();
            }
            return before.getNewLow();
        }
        ////////////今天最高 < 昨天最高//////////
        return current.getLow();
    }

    /**
     *  获取高价
     * @param before
     * @param current
     * @return
     */
    private BigDecimal getNewHigh(GupiaoKline before, GupiaoKline current, int trendFlag){
        if (current.getHigh().compareTo(before.getNewHigh()) > 0) { //今天最高大于昨天最高
            ////////////////今天最低大于等于昨天最低////////////////
            if (current.getLow().compareTo(before.getNewLow()) > 0) {
                return current.getHigh();
            }
             ////////////////今天最低小于等于昨天最低//////////////////
            if (trendFlag == 1) {
                return current.getHigh();
            }
            return before.getNewHigh();
        }

        if (current.getHigh().compareTo(before.getNewHigh()) == 0){   //今天最高==昨天最高
            return current.getHigh();
        }

        ////////////今天最高小于昨天最高////////////////
        if (current.getLow().compareTo(before.getNewLow()) >= 0){ //今天最低大于等于昨天最低
            if (trendFlag == 0){
                return current.getHigh();
            }
            return before.getNewHigh();
        }
        ////////////今天最低 < 昨天最低//////////
        return current.getHigh();
    }

    /**
     *  判断是否 向上
     * @param before
     * @param current
     * @return
     */
    private boolean isUpByMerge(GupiaoKline before, GupiaoKline current){
        if (current.getNewHigh().compareTo(before.getMergeHigh()) > 0) { //今天最高大于昨天最高
            if (current.getNewLow().compareTo(before.getMergeLow()) >= 0) { //今天最低大于等于昨天最低
                return true;
            }
            if (current.getNewLow().compareTo(before.getMergeLow()) < 0
                    && before.getYiTrend()==1) { //今天最低小于昨天最低
                log.info("xxxxxxxxxxxxxxx");
                log.info(before.toString());
                log.info(current.toString());
                return true;
            }
        }

        if (current.getNewHigh().compareTo(before.getMergeHigh()) == 0){   //今天最高==昨天最高
            if (current.getNewLow().compareTo(before.getMergeLow()) > 0){
                log.info("xxxxxxxxxxxxxxx");
                log.info(before.toString());
                log.info(current.toString());
                return true;
            }
            if ((current.getNewLow().compareTo(before.getMergeLow()) == 0)
                    && before.getYiTrend()==1){ //今天最低==昨天最低
                log.info("xxxxxxxxxxxxxxx");
                log.info(before.toString());
                log.info(current.toString());
                return true;
            }
        }

        if ((current.getNewHigh().compareTo(before.getMergeHigh()) < 0 //今天最高小于昨天最高
                && current.getNewLow().compareTo(before.getMergeLow()) > 0)
                && before.getYiTrend()==1){ //今天最低大于昨天最低
            log.info("xxxxxxxxxxxxxxx");
            log.info(before.toString());
            log.info(current.toString());
            return true;
        }

        return false;
    }

    /**
     *  判断是否 向上
     * @param before
     * @param current
     * @return
     */
    private boolean isUp(GupiaoKline before, GupiaoKline current, int trendFlag){
        if (current.getHigh().compareTo(before.getNewHigh()) > 0) { //今天最高大于昨天最高
            if (current.getLow().compareTo(before.getNewLow()) > 0) { //今天最低大于昨天最低
                return true;
            }
            if (current.getLow().compareTo(before.getNewLow()) <= 0
                    && trendFlag == 1) { //今天最低小于昨天最低
                return true;
            }
        }

        if (current.getHigh().compareTo(before.getNewHigh()) == 0
                && trendFlag == 1){   //今天最高==昨天最高
           return true;
        }

        if ((current.getHigh().compareTo(before.getNewHigh()) < 0 //今天最高小于昨天最高
                && current.getLow().compareTo(before.getNewLow()) >= 0)
                && trendFlag == 1){ //今天最低大于昨天最低
            return true;
        }

        return false;
    }

    /***
     * 是否存在包含关系
     * @param before
     * @param current
     * @return
     */
    private boolean isMerge(GupiaoKline before, GupiaoKline current){
        if ((current.getHigh().compareTo(before.getNewHigh()) > 0 //今天最高 > 昨天最高
                && current.getLow().compareTo(before.getNewLow()) > 0)){ //今天最低 > 昨天最低
            return false;
        }
        if ((current.getHigh().compareTo(before.getNewHigh()) < 0 //今天最高 < 昨天最高
                && current.getLow().compareTo(before.getNewLow()) < 0)){ //今天最低 < 昨天最低
            return false;
        }
        return true;
    }

    void save5mKline(List<GupiaoKline> listKline){
        List<String> bizDate = gupiaoKlineRepository.listKlineBizDate5m(listKline.get(0).getSymbol());
        List<GupiaoKline5m> list = listKline.stream()
                .filter(x -> bizDate.contains(x.getBizDate()))
                .map(t -> {
                    GupiaoKline5m gupiaoKline5m = new GupiaoKline5m();
                    BeanUtils.copyProperties(t, gupiaoKline5m);
                    return gupiaoKline5m;
                })
                .collect(Collectors.toList());
        gupiaoKline5mRepository.saveAll(list); //保存新增数据
    }

    void save15mKline(List<GupiaoKline> listKline){
        List<String> bizDate = gupiaoKlineRepository.listKlineBizDate15m(listKline.get(0).getSymbol());
        List<GupiaoKline15m> list = listKline.stream()
                .filter(x -> bizDate.contains(x.getBizDate()))
                .map(t -> {
                    GupiaoKline15m gupiaoKline15m = new GupiaoKline15m();
                    BeanUtils.copyProperties(t, gupiaoKline15m);
                    return gupiaoKline15m;
                })
                .collect(Collectors.toList());
        gupiaoKline15mRepository.saveAll(list); //保存新增数据
    }


    void save30mKline(List<GupiaoKline> listKline){
        List<String> bizDate = gupiaoKlineRepository.listKlineBizDate30m(listKline.get(0).getSymbol());
        List<GupiaoKline30m> list = listKline.stream()
                .filter(x -> bizDate.contains(x.getBizDate()))
                .map(t -> {
                    GupiaoKline30m gupiaoKline30m = new GupiaoKline30m();
                    BeanUtils.copyProperties(t, gupiaoKline30m);
                    return gupiaoKline30m;
                }).collect(Collectors.toList());
        gupiaoKline30mRepository.saveAll(list); //保存新增数据
    }
    void save60mKline(List<GupiaoKline> listKline){
        List<String> bizDate = gupiaoKlineRepository.listKlineBizDate60m(listKline.get(0).getSymbol());
        List<GupiaoKline60m> list = listKline.stream()
                .filter(x -> bizDate.contains(x.getBizDate()))
                .map(t -> {
                    GupiaoKline60m gupiaoKline60m = new GupiaoKline60m();
                    BeanUtils.copyProperties(t, gupiaoKline60m);
                    return gupiaoKline60m;
                }).collect(Collectors.toList());
        gupiaoKline60mRepository.saveAll(list); //保存新增数据
    }
    void save120mKline(List<GupiaoKline> listKline){
        List<String> bizDate = gupiaoKlineRepository.listKlineBizDate120m(listKline.get(0).getSymbol());
        List<GupiaoKline120m> list = listKline.stream()
                .filter(x -> bizDate.contains(x.getBizDate()))
                .map(t -> {
                    GupiaoKline120m gupiaoKline120m = new GupiaoKline120m();
                    BeanUtils.copyProperties(t, gupiaoKline120m);
                    return gupiaoKline120m;
                }).collect(Collectors.toList());
        gupiaoKline120mRepository.saveAll(list); //保存新增数据
    }

    private void saveKline(List<GupiaoKline> listKline){
        if (ComUtil.isEmpty(listKline)){
            return;
        }
        if (listKline.get(0).getPeriod()== KlineEnum.K_5M.getId()){
            save5mKline(listKline);
            return;
        }
        if (listKline.get(0).getPeriod()== KlineEnum.K_15M.getId()){
            save15mKline(listKline);
            return;
        }
        if (listKline.get(0).getPeriod()== KlineEnum.K_30M.getId()){
            save30mKline(listKline);
            return;
        }
        if (listKline.get(0).getPeriod()== KlineEnum.K_60M.getId()){
            save60mKline(listKline);
            return;
        }
        if (listKline.get(0).getPeriod()== KlineEnum.K_120M.getId()){
            save120mKline(listKline);
            return;
        }

        if (listKline.get(0).getPeriod()==KlineEnum.K_1D.getId()){
            List<String> bizDate = gupiaoKlineRepository.listKlineBizDate(listKline.get(0).getSymbol());
            List<GupiaoKline> list = listKline.stream()
                    .filter(x -> bizDate.contains(x.getBizDate()))
                    .collect(Collectors.toList());
            gupiaoKlineRepository.saveAll(list); //保存新增数据
        }

    }

}
