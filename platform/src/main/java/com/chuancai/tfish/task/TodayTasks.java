package com.chuancai.tfish.task;

import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private GupiaoXinhaoManager gupiaoXinhaoManager;





    /////////////////////////////////////////////////////////////////////////////////////////////////////


    /***
     *  5分钟级别
     * 30秒同步一次
     */
    @Scheduled(cron = "${task.today.xinhao.5m}")
    public void todayKzzBy5m() {
        if ("0".equals(consumerOff)) return;
        Date current = new Date();
        log.info(MessageFormat.format("todayKzzBy5m，Date：{0}",FORMAT.format(current)));
        gupiaoXinhaoManager.sysnGupiaoXinhaoAll(5);

    }


    /***
     *  30分钟级别
     * 2分 同步一次
     */
    @Scheduled(cron = "${task.today.xinhao.30m}")
    public void todayKzzBy30m() {
        if ("0".equals(consumerOff)) return;
        Date current = new Date();
        log.info(MessageFormat.format("todayKzzBy30m，Date：{0}",FORMAT.format(current)));
        gupiaoXinhaoManager.sysnGupiaoXinhaoAll(30);

    }



    /***
     *  日级别
     * 1天同步一次
     */
    @Scheduled(cron = "${task.today.xinhao.day}")
    public void todayKzzByDay() {
        if ("0".equals(consumerOff)) return;
        Date current = new Date();
        log.info(MessageFormat.format("todayKzzByDay，Date：{0}",FORMAT.format(current)));
        gupiaoXinhaoManager.sysnGupiaoXinhaoAll(101);

    }



}
