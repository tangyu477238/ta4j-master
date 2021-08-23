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
import java.util.List;
@Slf4j
@Service("gupiaoXinhaoManager")
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
        for (int i = 0; i < list.size(); i++) {
            GupiaoXinhao gupiaoXinhao = list.get(i);
            GupiaoXinhao gupiaoXinhao_1  = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(gupiaoXinhao.getSymbol(),
                    gupiaoXinhao.getTypeName(), gupiaoXinhao.getBizDate(), gupiaoXinhao.getPeriod());
            if (ComUtil.isEmpty(gupiaoXinhao_1)){
                addList.add(gupiaoXinhao);
                continue;
            }
            if (gupiaoXinhao.getBizDate().startsWith(DateTimeUtil.getBeforeDay(0))) { //如果是当天，请覆盖
                gupiaoXinhao.setId(gupiaoXinhao_1.getId());
                addList.add(gupiaoXinhao);
                continue;
            }
        }
        if (ComUtil.isEmpty(addList)){
            return;
        }
        gupiaoXinhaoRepository.saveAll(addList);
    }


    @Override
    public void sysnGupiaoXinhaoAll(Integer period) {
        List<Gupiao> list = gupiaoRepository.getSymbolTop();
        for (Gupiao gupiao : list){
            try {
                Runnable run = new GupiaoXinhaoManagerImpl.GupiaoXinhaoAllRunnable(gupiao, period);
                ExecutorProcessPool.getInstance().executeByCustomThread(run);
            } catch (Exception e){
                log.info(e.toString());
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
            BarSeries series = kzzStrategy.getBarSeries(gupiao.getSymbol(), period); //获取k数据
            List<GupiaoXinhao> listXinhao = kzzStrategy.addZjrcIndicator(series, period); //数据
            saveGupiaoXinhao(listXinhao);
        }
    }
}
