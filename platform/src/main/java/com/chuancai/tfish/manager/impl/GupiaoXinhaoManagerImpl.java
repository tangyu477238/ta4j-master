package com.chuancai.tfish.manager.impl;

import com.chuancai.tfish.enums.KlineEnum;
import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.*;
import com.chuancai.tfish.repository.*;
import com.chuancai.tfish.strategy.KzzStrategy;
import com.chuancai.tfish.util.ComUtil;
import com.chuancai.tfish.util.DateTimeUtil;
import com.chuancai.tfish.util.ExecutorProcessPool;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GupiaoXinhaoManagerImpl implements GupiaoXinhaoManager {

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;

    @Resource
    private KzzStrategy kzzStrategy;

    @Resource
    private GupiaoRepository gupiaoRepository;

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




    @Synchronized
    @Override
    public void sysnGupiaoXinhaoAll(Integer period) {
        List<Gupiao> list = gupiaoRepository.listKzz();
        for (Gupiao gupiao : list){
            try {
              Runnable run = new GupiaoXinhaoManagerImpl.GupiaoXinhaoAllRunnable(gupiao, period);
              ExecutorProcessPool.getInstance().executeByFixedThread(run);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public class GupiaoXinhaoAllRunnable implements Runnable{
        private Gupiao gupiao;
        private Integer period;
        public GupiaoXinhaoAllRunnable(Gupiao gupiao,Integer period){
            this.gupiao=gupiao;
            this.period = period;
        }
        @Override
        public void run(){
            Date date1 = new Date();
            List<GupiaoKline> listKline = kzzStrategy.listKine(gupiao.getSymbol(), period); //获取k数据
            if (ComUtil.isEmpty(listKline)){
                return;
            }
            log.info(period+"-------数据处理时长-----" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "-------"+gupiao.getSymbol());
            GupiaoXinhao gupiaoXinhao = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(gupiao.getSymbol(),
                    "zjrc", listKline.get(0).getBizDate(), period); //验证是否已处理
            if (!ComUtil.isEmpty(gupiaoXinhao)){
                return;
            }

            Collections.reverse(listKline); // 反转lists
            BarSeries series = kzzStrategy.getBarSeries(listKline);  //初始化数据
            saveGupiaoXinhao(kzzStrategy.addZjrcIndicator(series, listKline)); //计算数据
            if (period==30) {
                log.info("-------数据处理时长-----" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "");
            }
            //存储，趋势计算
            saveKline(listTrendKline(listKline));

        }
    }

    @Synchronized
    private List<GupiaoKline>  listTrendKline(List<GupiaoKline> listKline){
        for (int i = 0; i < listKline.size(); i++) {
            if (i==0){
                if (ComUtil.isEmpty(listKline.get(i).getTrend())) { //未计算,进行初始化
                    listKline.get(i).setTrend(0); //如果第一条记录的趋势为空,则为下降处理

                    listKline.get(i).setUpPrice1(listKline.get(i).getHigh());
                    listKline.get(i).setDownPrice1(listKline.get(i).getLow());

                    listKline.get(i).setNewHigh(listKline.get(i).getHigh());
                    listKline.get(i).setNewLow(listKline.get(i).getLow());
                }
                continue;
            }
            GupiaoKline previousGupiaoKline = listKline.get(i-1);
            GupiaoKline gupiaoKline = listKline.get(i);
            if (isUpTrend(previousGupiaoKline, gupiaoKline)){
                gupiaoKline.setTrend(1);
                if (previousGupiaoKline.getTrend()==0){
                    gupiaoKline.setUpPrice5(previousGupiaoKline.getUpPrice4());
                    gupiaoKline.setDownPrice5(previousGupiaoKline.getDownPrice4());
                    gupiaoKline.setUpPrice4(previousGupiaoKline.getUpPrice3());
                    gupiaoKline.setDownPrice4(previousGupiaoKline.getDownPrice3());
                    gupiaoKline.setUpPrice3(previousGupiaoKline.getUpPrice2());
                    gupiaoKline.setDownPrice3(previousGupiaoKline.getDownPrice2());
                    gupiaoKline.setUpPrice2(previousGupiaoKline.getUpPrice1());
                    gupiaoKline.setDownPrice2(previousGupiaoKline.getDownPrice1());
                } else {
                    gupiaoKline.setUpPrice5(previousGupiaoKline.getUpPrice5());
                    gupiaoKline.setDownPrice5(previousGupiaoKline.getDownPrice5());
                    gupiaoKline.setUpPrice4(previousGupiaoKline.getUpPrice4());
                    gupiaoKline.setDownPrice4(previousGupiaoKline.getDownPrice4());
                    gupiaoKline.setUpPrice3(previousGupiaoKline.getUpPrice3());
                    gupiaoKline.setDownPrice3(previousGupiaoKline.getDownPrice3());
                    gupiaoKline.setUpPrice2(previousGupiaoKline.getUpPrice2());
                    gupiaoKline.setDownPrice2(previousGupiaoKline.getDownPrice2());
                }
                gupiaoKline.setUpPrice1(gupiaoKline.getNewHigh());
                gupiaoKline.setDownPrice1(previousGupiaoKline.getDownPrice1()); //低价保持不变

                continue;
            }
            gupiaoKline.setTrend(0);
            if (previousGupiaoKline.getTrend()==1){
                gupiaoKline.setUpPrice5(previousGupiaoKline.getUpPrice4());
                gupiaoKline.setDownPrice5(previousGupiaoKline.getDownPrice4());
                gupiaoKline.setUpPrice4(previousGupiaoKline.getUpPrice3());
                gupiaoKline.setDownPrice4(previousGupiaoKline.getDownPrice3());
                gupiaoKline.setUpPrice3(previousGupiaoKline.getUpPrice2());
                gupiaoKline.setDownPrice3(previousGupiaoKline.getDownPrice2());
                gupiaoKline.setUpPrice2(previousGupiaoKline.getUpPrice1());
                gupiaoKline.setDownPrice2(previousGupiaoKline.getDownPrice1());
            } else {
                gupiaoKline.setUpPrice5(previousGupiaoKline.getUpPrice5());
                gupiaoKline.setDownPrice5(previousGupiaoKline.getDownPrice5());
                gupiaoKline.setUpPrice4(previousGupiaoKline.getUpPrice4());
                gupiaoKline.setDownPrice4(previousGupiaoKline.getDownPrice4());
                gupiaoKline.setUpPrice3(previousGupiaoKline.getUpPrice3());
                gupiaoKline.setDownPrice3(previousGupiaoKline.getDownPrice3());
                gupiaoKline.setUpPrice2(previousGupiaoKline.getUpPrice2());
                gupiaoKline.setDownPrice2(previousGupiaoKline.getDownPrice2());
            }
            gupiaoKline.setUpPrice1(previousGupiaoKline.getUpPrice1());//高价保持不变
            gupiaoKline.setDownPrice1(gupiaoKline.getNewLow());


        }
        return listKline;
    }


    @Synchronized
    private void saveKline(List<GupiaoKline> listKline){
        if (ComUtil.isEmpty(listKline)){
            return;
        }
        if (listKline.get(0).getPeriod()== KlineEnum.K_5M.getId()){
            List<GupiaoKline5m> list = listKline.stream().map(t -> {
                GupiaoKline5m gupiaoKline5m = new GupiaoKline5m();
                BeanUtils.copyProperties(t, gupiaoKline5m);
                return gupiaoKline5m;
            }).collect(Collectors.toList());
            gupiaoKline5mRepository.saveAll(list); //保存新增数据
        } else if (listKline.get(0).getPeriod()==KlineEnum.K_30M.getId()){
            List<GupiaoKline30m> list = listKline.stream().map(t -> {
                GupiaoKline30m gupiaoKline30m = new GupiaoKline30m();
                BeanUtils.copyProperties(t, gupiaoKline30m);
                return gupiaoKline30m;
            }).collect(Collectors.toList());
            gupiaoKline30mRepository.saveAll(list); //保存新增数据
        } else if (listKline.get(0).getPeriod()==KlineEnum.K_1D.getId()){
            gupiaoKlineRepository.saveAll(listKline); //保存新增数据
        }
    }

    /****
     *  判断是否上升趋势
     * @param previousGupiaoKline
     * @param gupiaoKline
     * @return
     */
    @Synchronized
    private boolean isUpTrend(GupiaoKline previousGupiaoKline, GupiaoKline gupiaoKline){
//        log.info(gupiaoKline.getHigh()+"------------"+previousGupiaoKline.getNewHigh());
//        log.info(gupiaoKline.getLow()+"------------"+previousGupiaoKline.getNewLow());
        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) > 0 //今天最高大于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) >= 0)){ //今天最低大于等于昨天最低
            gupiaoKline.setNewHigh(gupiaoKline.getHigh());
            gupiaoKline.setNewLow(gupiaoKline.getLow());
            return true;
        }
        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) == 0  //今天最高等于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) > 0)){  //今天最低大于昨天最低
            gupiaoKline.setNewHigh(gupiaoKline.getHigh());
            gupiaoKline.setNewLow(gupiaoKline.getLow());
            return true;
        }

        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) > 0 //今天最高大于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) < 0)){ //今天最低小于昨天最低

            if (previousGupiaoKline.getTrend()==1) {
                gupiaoKline.setNewHigh(gupiaoKline.getHigh());
                gupiaoKline.setNewLow(previousGupiaoKline.getNewLow());
                return true;
            }
            /////////////////////////////////  下降判断  ////////////////////////////////////////
            gupiaoKline.setNewHigh(previousGupiaoKline.getNewHigh());
            gupiaoKline.setNewLow(gupiaoKline.getLow());
            return false;
        }

        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) < 0 //今天最高小于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) > 0)){ //今天最低大于昨天最低
            if (previousGupiaoKline.getTrend()==1) {
                gupiaoKline.setNewHigh(previousGupiaoKline.getNewHigh());
                gupiaoKline.setNewLow(gupiaoKline.getLow());
                return true;
            }
            /////////////////////////////////  下降判断  ////////////////////////////////////////
            gupiaoKline.setNewHigh(gupiaoKline.getHigh());
            gupiaoKline.setNewLow(previousGupiaoKline.getNewLow());
            return false;
        }


        /////////////////////////////////  下降判断  ////////////////////////////////////////
        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) <= 0 //今天最高小于等于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) < 0)){ //今天最低小于昨天最低
            gupiaoKline.setNewHigh(gupiaoKline.getHigh());
            gupiaoKline.setNewLow(gupiaoKline.getLow());
            return false;
        }
        /////////////////////////////////  下降判断  ////////////////////////////////////////
        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) < 0 //今天最高小于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) == 0)){ //今天最低等于昨天最低
            gupiaoKline.setNewHigh(gupiaoKline.getHigh());
            gupiaoKline.setNewLow(gupiaoKline.getLow());
            return false;
        }

        log.info(gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh())+"---------"+gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()));
        if ((gupiaoKline.getHigh().compareTo(previousGupiaoKline.getNewHigh()) == 0 //今天最高等于昨天最高
                && gupiaoKline.getLow().compareTo(previousGupiaoKline.getNewLow()) == 0)) { //今天最低等于昨天最低
            gupiaoKline.setNewHigh(gupiaoKline.getHigh());
            gupiaoKline.setNewLow(gupiaoKline.getLow());
            if (previousGupiaoKline.getTrend()==1) {
                return true;
            }
            return false;
        }

        if (gupiaoKline.getNewHigh()==null){
            log.info("-----------------------");
        }
        return false;
    }

}
