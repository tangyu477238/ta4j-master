package com.chuancai.tfish.manager.impl;

import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.Gupiao;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoRepository;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.strategy.KzzStrategy;
import com.chuancai.tfish.util.ComUtil;
import com.chuancai.tfish.util.DateTimeUtil;
import com.chuancai.tfish.util.ExecutorProcessPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Slf4j
@Service
public class GupiaoXinhaoManagerImpl implements GupiaoXinhaoManager {

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;

    @Resource
    private KzzStrategy kzzStrategy;

    @Resource
    private GupiaoRepository gupiaoRepository;



    @Override
    public void saveGupiaoXinhao(List<GupiaoXinhao> list) {
        List<GupiaoXinhao> addList = new ArrayList();

        String maxBizDate = gupiaoXinhaoRepository.getMaxBizDate(list.get(0).getSymbol(),list.get(0).getPeriod(),"zjrc");
        for (GupiaoXinhao gupiaoXinhao : list) {
            if (!ComUtil.isEmpty(maxBizDate)
                    && gupiaoXinhao.getBizDate().compareTo(maxBizDate) <= 0){ //已经存在的信号,不在计算和验证
                continue;
            }
            addList.add(gupiaoXinhao);
        }

//          Date date1 = new Date();
//        for (int i = 0; i < list.size(); i++) {
//            GupiaoXinhao gupiaoXinhao = list.get(i);
//            if (!ComUtil.isEmpty(maxBizDate)
//                    && gupiaoXinhao.getBizDate().compareTo(maxBizDate) <= 0){ //已经存在的信号,不在计算和验证
//                continue;
//            }
//            GupiaoXinhao gupiaoXinhao_1  = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(gupiaoXinhao.getSymbol(),
//                    gupiaoXinhao.getTypeName(), gupiaoXinhao.getBizDate(), gupiaoXinhao.getPeriod());
//            if (ComUtil.isEmpty(gupiaoXinhao_1)){
//                addList.add(gupiaoXinhao);
//                continue;
//            }
//            if (gupiaoXinhao.getBizDate().startsWith(DateTimeUtil.getBeforeDay(0))) { //如果是当天，请覆盖
//                gupiaoXinhao.setId(gupiaoXinhao_1.getId());
//                addList.add(gupiaoXinhao);
//                continue;
//            }
//        }
//        log.info("--------验证数-----"+DateTimeUtil.getSecondsOfTwoDate(date1,new Date())+"");date1 = new Date();
        if (ComUtil.isEmpty(addList)){
            return;
        }
        gupiaoXinhaoRepository.saveAll(addList);
//        log.info("--------存数-----"+DateTimeUtil.getSecondsOfTwoDate(date1,new Date())+"");

    }


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
            BarSeries series = kzzStrategy.getBarSeries(gupiao.getSymbol(), period); //获取k数据
            if (ComUtil.isEmpty(series)){
                return;
            }
            List<GupiaoXinhao> listXinhao = kzzStrategy.addZjrcIndicator(series, period); //数据
            saveGupiaoXinhao(listXinhao);
            if (period==30) {
                log.info("-------数据处理时长-----" + DateTimeUtil.getSecondsOfTwoDate(date1, new Date()) + "");
            }
        }
    }
}
