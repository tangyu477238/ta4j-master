package com.chuancai.tfish.manager.impl;

import com.chuancai.tfish.enums.KlineEnum;
import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.GupiaoKline;
import com.chuancai.tfish.model.GupiaoKline30m;
import com.chuancai.tfish.model.GupiaoKline5m;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoKline30mRepository;
import com.chuancai.tfish.repository.GupiaoKline5mRepository;
import com.chuancai.tfish.repository.GupiaoKlineRepository;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.strategy.KzzStrategy;
import com.chuancai.tfish.util.ComUtil;
import com.chuancai.tfish.util.DateTimeUtil;
import com.chuancai.tfish.util.ExecutorProcessPool;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private GupiaoKline30mRepository gupiaoKline30mRepository; //获取30k线对象

    @Override
    public void saveGupiaoXinhao(List<GupiaoXinhao> list) {
        List<GupiaoXinhao> addList = new ArrayList();
        String maxBizDate = gupiaoXinhaoRepository.getMaxBizDate(list.get(0).getSymbol(),list.get(0).getPeriod(),"zjrc");
        for (GupiaoXinhao gupiaoXinhao : list) {
            if (!ComUtil.isEmpty(maxBizDate)
                    && maxBizDate.compareTo(gupiaoXinhao.getBizDate()) >= 0){ //已经存在的信号,不在计算和验证
                continue;
            }
            addList.add(gupiaoXinhao);
        }
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
            if (!symbol.equals("127030")){
                continue;
            }
            try {
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
            calculateTrend(symbol,period);
        }
    }


    private void calculateZjrc(String symbol, Integer period){
        Date date1 = new Date();
        List<GupiaoKline> listKline = kzzStrategy.listKine(symbol, period); //获取k数据
        if (ComUtil.isEmpty(listKline)){
            return;
        }
        GupiaoXinhao gupiaoXinhao = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(symbol,
                "zjrc", listKline.get(0).getBizDate(), period); //验证是否已处理
        if (!ComUtil.isEmpty(gupiaoXinhao)){
            return;
        }
        Collections.reverse(listKline); // 反转lists
        BarSeries series = kzzStrategy.getBarSeries(listKline);  //初始化数据
        saveGupiaoXinhao(kzzStrategy.addZjrcIndicator(series, listKline)); //计算数据
         log.info(period+"-------calculateZjrc数据处理时长---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "-------"+ symbol);
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
//        for (int i = tlist.size()-1; i >0; i--) { //包含处理后的数据
//            if (tlist.get(i).getIsMerge()==0 && i < (tlist.size()-1)){
//                tlist.get(i).setYiLow(tlist.get(i+1).getYiLow());
//                tlist.get(i).setYiHigh(tlist.get(i+1).getYiHigh());
//                tlist.get(i).setYiTrend(tlist.get(i+1).getYiTrend());
//                continue;
//            }
//        }
    }

    private void calculateTrend(String symbol, Integer period){
        Date date1 = new Date();
        List<GupiaoKline> listKline = kzzStrategy.listKine(symbol, period); //获取k数据
        if (ComUtil.isEmpty(listKline)){
            return;
        }
        Collections.reverse(listKline); // 反转lists
        //存储，趋势计算
        List<GupiaoKline> tlist = listBaohan(listKline); //处理包含关系,以及顶底

        List<GupiaoKline> listDistinct = tlist.stream().filter(t->t.getIsMerge()==1).collect(Collectors.toList()); //去掉包含关系的K线集合
        Map<String, GupiaoKline> map = calculateDingDi(listDistinct);
        toCopyKline(tlist, map);
        saveKline(tlist);
        log.info(period+"-------calculateTrend数据处理时长-----" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "-------"+ symbol);
    }

    //确认向下一笔
    private void yiBiSure(int start, int end, BigDecimal highPrice, BigDecimal lowPrice, List<GupiaoKline> listDistinct, int trend){
        for (int i = start; i<end; i++){
            listDistinct.get(i).setYiTrend(trend);
            listDistinct.get(i).setYiHigh(highPrice);
            listDistinct.get(i).setYiLow(lowPrice);
        }
    }


    /***
     * 计算顶底
     * @param listDistinct
     */
    private Map<String, GupiaoKline> calculateDingDi(List<GupiaoKline> listDistinct ){
        int dingNum = 0;
        BigDecimal highPrice = new BigDecimal(0);

        int diNum = 0 ;
        BigDecimal lowPrice = new BigDecimal(0);

        boolean isDing = true;
        for (int i = 0; i < listDistinct.size(); i++) {
            if (i<2){
                continue;
            }

            GupiaoKline previous = listDistinct.get(i-2);
            GupiaoKline before = listDistinct.get(i-1);
            GupiaoKline current = listDistinct.get(i);

            if (previous.getBizDate().startsWith("2021-09-14")){
                log.info(previous.getBizDate());
            }
            if (previous.getTrend()==1
                    &&before.getTrend()==1
                    &&current.getTrend()==0 && (i-1-diNum)>=3){ //110为顶分型，且距离上次底大于3根k线
                if (isDing && highPrice.compareTo(before.getNewHigh())>0){//上次也是顶,上次>本次最高价
                    continue;
                }
                if (!isDing){ //如果上次为底,那么底已终结
                    yiBiSure(dingNum+1, diNum, highPrice, lowPrice, listDistinct,0); //向下一笔确认
                }
                dingNum = i-1;
                highPrice = before.getNewHigh();
                isDing = true;
            }

            if (previous.getTrend()==0
                    &&before.getTrend()==0
                    &&current.getTrend()==1 && (i-1-dingNum)>=3){ //001为底分型，且距离上次顶大于3根k线
                if (!isDing && lowPrice.compareTo(before.getNewLow())<0){ //上次也是底,上次<本次最低价
                    continue;
                }

                if (isDing){ //如果上次为顶,那么顶已终结
                    yiBiSure(diNum+1, dingNum, highPrice, lowPrice, listDistinct,1); //向上一笔确认
                }
                diNum = i-1;
                lowPrice = before.getNewLow();
                isDing = false;
            }

        }
        return listDistinct.stream().collect(Collectors.toMap(GupiaoKline::getBizDate, Function.identity()));
    }



    /**
     * 第一根处理逻辑
     * @param listKline
     */
    private void firstKline(List<GupiaoKline> listKline){
        if (!ComUtil.isEmpty(listKline.get(0).getTrend())) { //已计算,不需要处理
            return;
        }
        listKline.get(0).setTrend(0); //如果第一条记录的趋势为空,则为下降处理

        listKline.get(0).setUpPrice1(listKline.get(0).getHigh());
        listKline.get(0).setDownPrice1(listKline.get(0).getLow());

        listKline.get(0).setNewHigh(listKline.get(0).getHigh());
        listKline.get(0).setNewLow(listKline.get(0).getLow());

        listKline.get(0).setBeforeDate(listKline.get(0).getBizDate()); //开始
        listKline.get(0).setAfterDate(listKline.get(0).getBizDate()); //结束

        listKline.get(0).setIsMerge(1); //有效
    }

    private List<GupiaoKline>  listBaohan(List<GupiaoKline> listKline){
        for (int i = 0; i < listKline.size(); i++) {
            if (i == 0) {
                firstKline(listKline); //首根逻辑
                continue;
            }
            GupiaoKline before = listKline.get(i-1); //前一根
            GupiaoKline current = listKline.get(i); //当前根

            if (isUpTrendBaohan(before, current)){ //判断是否上升
                current.setTrend(1);
                if (before.getTrend()==0){
                    current.setUpPrice5(before.getUpPrice4());
                    current.setDownPrice5(before.getDownPrice4());
                    current.setUpPrice4(before.getUpPrice3());
                    current.setDownPrice4(before.getDownPrice3());
                    current.setUpPrice3(before.getUpPrice2());
                    current.setDownPrice3(before.getDownPrice2());
                    current.setUpPrice2(before.getUpPrice1());
                    current.setDownPrice2(before.getDownPrice1());
                } else {
                    current.setUpPrice5(before.getUpPrice5());
                    current.setDownPrice5(before.getDownPrice5());
                    current.setUpPrice4(before.getUpPrice4());
                    current.setDownPrice4(before.getDownPrice4());
                    current.setUpPrice3(before.getUpPrice3());
                    current.setDownPrice3(before.getDownPrice3());
                    current.setUpPrice2(before.getUpPrice2());
                    current.setDownPrice2(before.getDownPrice2());
                }
                current.setUpPrice1(current.getNewHigh());
                current.setDownPrice1(before.getDownPrice1()); //低价保持不变

                continue;
            }
            current.setTrend(0);
            if (before.getTrend()==1){
                current.setUpPrice5(before.getUpPrice4());
                current.setDownPrice5(before.getDownPrice4());
                current.setUpPrice4(before.getUpPrice3());
                current.setDownPrice4(before.getDownPrice3());
                current.setUpPrice3(before.getUpPrice2());
                current.setDownPrice3(before.getDownPrice2());
                current.setUpPrice2(before.getUpPrice1());
                current.setDownPrice2(before.getDownPrice1());
            } else {
                current.setUpPrice5(before.getUpPrice5());
                current.setDownPrice5(before.getDownPrice5());
                current.setUpPrice4(before.getUpPrice4());
                current.setDownPrice4(before.getDownPrice4());
                current.setUpPrice3(before.getUpPrice3());
                current.setDownPrice3(before.getDownPrice3());
                current.setUpPrice2(before.getUpPrice2());
                current.setDownPrice2(before.getDownPrice2());
            }
            current.setUpPrice1(before.getUpPrice1());//高价保持不变
            current.setDownPrice1(current.getNewLow());


        }
        return listKline;
    }

    private List<GupiaoKline>  listTrendKline(List<GupiaoKline> listKline){
        for (int i = 0; i < listKline.size(); i++) {
            if (i == 0) {
                firstKline(listKline); //首根逻辑
                continue;
            }
            GupiaoKline before = listKline.get(i-1); //前一根
            GupiaoKline current = listKline.get(i); //当前根

            if (isUpTrend(before, current)){ //判断是否上升
                current.setTrend(1);
                if (before.getTrend()==0){
                    current.setUpPrice5(before.getUpPrice4());
                    current.setDownPrice5(before.getDownPrice4());
                    current.setUpPrice4(before.getUpPrice3());
                    current.setDownPrice4(before.getDownPrice3());
                    current.setUpPrice3(before.getUpPrice2());
                    current.setDownPrice3(before.getDownPrice2());
                    current.setUpPrice2(before.getUpPrice1());
                    current.setDownPrice2(before.getDownPrice1());
                } else {
                    current.setUpPrice5(before.getUpPrice5());
                    current.setDownPrice5(before.getDownPrice5());
                    current.setUpPrice4(before.getUpPrice4());
                    current.setDownPrice4(before.getDownPrice4());
                    current.setUpPrice3(before.getUpPrice3());
                    current.setDownPrice3(before.getDownPrice3());
                    current.setUpPrice2(before.getUpPrice2());
                    current.setDownPrice2(before.getDownPrice2());
                }
                current.setUpPrice1(current.getNewHigh());
                current.setDownPrice1(before.getDownPrice1()); //低价保持不变

                continue;
            }
            current.setTrend(0);
            if (before.getTrend()==1){
                current.setUpPrice5(before.getUpPrice4());
                current.setDownPrice5(before.getDownPrice4());
                current.setUpPrice4(before.getUpPrice3());
                current.setDownPrice4(before.getDownPrice3());
                current.setUpPrice3(before.getUpPrice2());
                current.setDownPrice3(before.getDownPrice2());
                current.setUpPrice2(before.getUpPrice1());
                current.setDownPrice2(before.getDownPrice1());
            } else {
                current.setUpPrice5(before.getUpPrice5());
                current.setDownPrice5(before.getDownPrice5());
                current.setUpPrice4(before.getUpPrice4());
                current.setDownPrice4(before.getDownPrice4());
                current.setUpPrice3(before.getUpPrice3());
                current.setDownPrice3(before.getDownPrice3());
                current.setUpPrice2(before.getUpPrice2());
                current.setDownPrice2(before.getDownPrice2());
            }
            current.setUpPrice1(before.getUpPrice1());//高价保持不变
            current.setDownPrice1(current.getNewLow());


        }
        return listKline;
    }



    private void saveKline(List<GupiaoKline> listKline){
        Date date1 = new Date();
        if (ComUtil.isEmpty(listKline)){
            return;
        }

        if (listKline.get(0).getPeriod()== KlineEnum.K_5M.getId()){
            List<String> bizDate = gupiaoKlineRepository.listKlineBizDate5m(listKline.get(0).getSymbol());
            List<GupiaoKline5m> list = listKline.stream()
                    .filter(x -> bizDate.contains(x.getBizDate()))
                    .map(t -> {
                        GupiaoKline5m gupiaoKline5m = new GupiaoKline5m();
                        BeanUtils.copyProperties(t, gupiaoKline5m);
                        return gupiaoKline5m;
                    })
                    .collect(Collectors.toList());
//            log.info("-------数据处理时长--1---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "");
            gupiaoKline5mRepository.saveAll(list); //保存新增数据
//            log.info("-------数据处理时长--2---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "");
        } else if (listKline.get(0).getPeriod()==KlineEnum.K_30M.getId()){
            List<String> bizDate = gupiaoKlineRepository.listKlineBizDate30m(listKline.get(0).getSymbol());
            List<GupiaoKline30m> list = listKline.stream()
                    .filter(x -> bizDate.contains(x.getBizDate()))
                    .map(t -> {
                GupiaoKline30m gupiaoKline30m = new GupiaoKline30m();
                BeanUtils.copyProperties(t, gupiaoKline30m);
                return gupiaoKline30m;
            }).collect(Collectors.toList());
//            log.info("-------数据处理时长--3---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "");
            gupiaoKline30mRepository.saveAll(list); //保存新增数据
//            log.info("-------数据处理时长--4---" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "");
        } else if (listKline.get(0).getPeriod()==KlineEnum.K_1D.getId()){
            List<String> bizDate = gupiaoKlineRepository.listKlineBizDate(listKline.get(0).getSymbol());
            List<GupiaoKline> list = listKline.stream()
                    .filter(x -> bizDate.contains(x.getBizDate()))
                    .collect(Collectors.toList());
            gupiaoKlineRepository.saveAll(list); //保存新增数据
        }

    }

    /****
     *  判断是否上升趋势
     * @param before
     * @param current
     * @return
     */

    private boolean isUpTrend(GupiaoKline before, GupiaoKline current){
        if ((current.getHigh().compareTo(before.getNewHigh()) > 0 //今天最高大于昨天最高
                && current.getLow().compareTo(before.getNewLow()) >= 0)){ //今天最低大于等于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            current.setBeforeDate(before.getBizDate());
            current.setAfterDate(current.getBizDate());
            current.setIsMerge(1);
            return true;
        }
        if ((current.getHigh().compareTo(before.getNewHigh()) == 0  //今天最高等于昨天最高
                && current.getLow().compareTo(before.getNewLow()) > 0)){  //今天最低大于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
            current.setAfterDate(current.getBizDate());
            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return true;
        }



        if ((current.getHigh().compareTo(before.getNewHigh()) > 0 //今天最高大于昨天最高
                && current.getLow().compareTo(before.getNewLow()) < 0)){ //今天最低小于昨天最低

            if (before.getTrend()==1) {
                current.setNewHigh(current.getHigh());
                current.setNewLow(before.getNewLow());

                current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
                current.setAfterDate(current.getBizDate());

                before.setIsMerge(0); //昨天无效
                current.setIsMerge(1); //今天保留
                return true;
            }
            /////////////////////////////////  下降判断  ////////////////////////////////////////
            current.setNewHigh(before.getNewHigh());
            current.setNewLow(current.getLow());

            current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
            current.setAfterDate(current.getBizDate());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return false;
        }

        if ((current.getHigh().compareTo(before.getNewHigh()) < 0 //今天最高小于昨天最高
                && current.getLow().compareTo(before.getNewLow()) > 0)){ //今天最低大于昨天最低
            if (before.getTrend()==1) {
                current.setNewHigh(before.getNewHigh());
                current.setNewLow(current.getLow());

                current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
                current.setAfterDate(current.getBizDate());

                before.setIsMerge(0); //昨天无效
                current.setIsMerge(1); //今天保留
                return true;
            }
            /////////////////////////////////  下降判断  ////////////////////////////////////////
            current.setNewHigh(current.getHigh());
            current.setNewLow(before.getNewLow());

            current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
            current.setAfterDate(current.getBizDate());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return false;
        }


        /////////////////////////////////  下降判断  ////////////////////////////////////////
        if ((current.getHigh().compareTo(before.getNewHigh()) <= 0 //今天最高小于等于昨天最高
                && current.getLow().compareTo(before.getNewLow()) < 0)){ //今天最低小于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            current.setBeforeDate(before.getBizDate());
            current.setAfterDate(current.getBizDate());

            current.setIsMerge(1); //今天保留
            return false;
        }
        /////////////////////////////////  下降判断  ///////////////////////////////包含/////////
        if ((current.getHigh().compareTo(before.getNewHigh()) < 0 //今天最高小于昨天最高
                && current.getLow().compareTo(before.getNewLow()) == 0)){ //今天最低等于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
            current.setAfterDate(current.getBizDate());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return false;
        }

        //昨天和今天一样的情况
        current.setNewHigh(current.getHigh());
        current.setNewLow(current.getLow());

        current.setBeforeDate(before.getBeforeDate()); //取前1天的before值
        current.setAfterDate(current.getBizDate());

        before.setIsMerge(0); //昨天无效
        current.setIsMerge(1); //今天保留
        if (before.getTrend()==1) {
            return true;
        }
        return false;
    }



    private boolean isUpTrendBaohan(GupiaoKline before, GupiaoKline current){
        if ((current.getHigh().compareTo(before.getNewHigh()) > 0 //今天最高大于昨天最高
                && current.getLow().compareTo(before.getNewLow()) >= 0)){ //今天最低大于等于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());
            current.setIsMerge(1);
            return true;
        }
        if ((current.getHigh().compareTo(before.getNewHigh()) == 0  //今天最高等于昨天最高
                && current.getLow().compareTo(before.getNewLow()) > 0)){  //今天最低大于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return true;
        }



        if ((current.getHigh().compareTo(before.getNewHigh()) > 0 //今天最高大于昨天最高
                && current.getLow().compareTo(before.getNewLow()) < 0)){ //今天最低小于昨天最低

            if (before.getTrend()==1) {
                current.setNewHigh(current.getHigh());
                current.setNewLow(before.getNewLow());

                before.setIsMerge(0); //昨天无效
                current.setIsMerge(1); //今天保留
                return true;
            }
            /////////////////////////////////  下降判断  ////////////////////////////////////////
            current.setNewHigh(before.getNewHigh());
            current.setNewLow(current.getLow());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return false;
        }

        if ((current.getHigh().compareTo(before.getNewHigh()) < 0 //今天最高小于昨天最高
                && current.getLow().compareTo(before.getNewLow()) > 0)){ //今天最低大于昨天最低
            if (before.getTrend()==1) {
                current.setNewHigh(before.getNewHigh());
                current.setNewLow(current.getLow());

                before.setIsMerge(0); //昨天无效
                current.setIsMerge(1); //今天保留
                return true;
            }
            /////////////////////////////////  下降判断  ////////////////////////////////////////
            current.setNewHigh(current.getHigh());
            current.setNewLow(before.getNewLow());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return false;
        }


        /////////////////////////////////  下降判断  ////////////////////////////////////////
        if ((current.getHigh().compareTo(before.getNewHigh()) <= 0 //今天最高小于等于昨天最高
                && current.getLow().compareTo(before.getNewLow()) < 0)){ //今天最低小于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            current.setIsMerge(1); //今天保留
            return false;
        }
        /////////////////////////////////  下降判断  ///////////////////////////////包含/////////
        if ((current.getHigh().compareTo(before.getNewHigh()) < 0 //今天最高小于昨天最高
                && current.getLow().compareTo(before.getNewLow()) == 0)){ //今天最低等于昨天最低
            current.setNewHigh(current.getHigh());
            current.setNewLow(current.getLow());

            before.setIsMerge(0); //昨天无效
            current.setIsMerge(1); //今天保留
            return false;
        }

        //昨天和今天一样的情况
        current.setNewHigh(current.getHigh());
        current.setNewLow(current.getLow());

        before.setIsMerge(0); //昨天无效
        current.setIsMerge(1); //今天保留
        if (before.getTrend()==1) {
            return true;
        }
        return false;
    }
}
