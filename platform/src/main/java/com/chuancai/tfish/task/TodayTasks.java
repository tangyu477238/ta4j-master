package com.chuancai.tfish.task;

import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.Gupiao;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoRepository;
import com.chuancai.tfish.strategy.KzzStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import javax.annotation.Resource;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 定时任务配置
 *
 * @author zifangsky
 * @date 2018/6/21
 * @since 1.0.0
 */
@Component
@Slf4j
public class TodayTasks {

    private final Format FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Value("${mq.xinhao.off}")
    private String consumerOff;


    @Resource
    private KzzStrategy kzzStrategy;

    @Resource
    private GupiaoRepository gupiaoRepository;

    @Resource
    private GupiaoXinhaoManager gupiaoXinhaoManager;



    /***
     *  5分钟级别
     * 30秒同步一次
     */
    @Scheduled(cron = "${task.today.xinhao.5fen}")
    public void todayKzzBy5Fen() {
        if ("0".equals(consumerOff)) return;
        Date current = new Date();
        log.info(MessageFormat.format("todayKzzBy5Fen，Date：{0}",FORMAT.format(current)));
        List<Gupiao> list = gupiaoRepository.getSymbolTop();
        for (Gupiao gupiao : list){
            try {
                kzzStrategy.setPeriod(5); //
                BarSeries series = kzzStrategy.getBarSeries(gupiao.getSymbol()); //获取k数据
                List<GupiaoXinhao> listXinhao = kzzStrategy.addZjrcIndicator(series); //数据
                gupiaoXinhaoManager.saveGupiaoXinhao(listXinhao);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }





    /////////////////////////////////////////////////////////////////////////////////////////////////////


    /***
     *  日级别
     * 1天同步一次
     */
    @Scheduled(cron = "${task.today.xinhao.day}")
    public void todayKzzByDay() {
        if ("0".equals(consumerOff)) return;
        Date current = new Date();
        log.info(MessageFormat.format("todayKzzByDay，Date：{0}",FORMAT.format(current)));
        List<Gupiao> list = gupiaoRepository.getSymbolTop();
        for (Gupiao gupiao : list){
            try {
                kzzStrategy.setPeriod(101); //按天
                BarSeries series = kzzStrategy.getBarSeries(gupiao.getSymbol()); //获取k数据
                List<GupiaoXinhao> listXinhao = kzzStrategy.addZjrcIndicator(series); //数据
                gupiaoXinhaoManager.saveGupiaoXinhao(listXinhao);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }



}
