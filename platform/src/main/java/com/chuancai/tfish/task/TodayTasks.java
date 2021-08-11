package com.chuancai.tfish.task;

import com.chuancai.tfish.model.Gupiao;
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


    /***
     * 30秒同步一次
     */
    @Scheduled(cron = "${task.today.xinhao.fen}")
    public void todayKzzByFen() {
        if ("0".equals(consumerOff)) return;
        Date current = new Date();
        log.debug(MessageFormat.format("todayKzzByFen，Date：{0}",FORMAT.format(current)));
        List<Gupiao> list = gupiaoRepository.getSymbolTop();
        for (Gupiao gupiao : list){
            try {
                BarSeries series = kzzStrategy.getBarSeries(gupiao.getSymbol()); //获取k数据
                kzzStrategy.addIndicator(series, gupiao.getSymbol()); //数据存储
            }catch (Exception e){}
        }

    }

}
