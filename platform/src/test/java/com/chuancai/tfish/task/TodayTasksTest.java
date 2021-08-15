/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.chuancai.tfish.task;

import com.chuancai.tfish.indicators.BuyEmaMACDIndicator;
import com.chuancai.tfish.indicators.XLPLIndicator;
import com.chuancai.tfish.indicators.impl.ZJRCIndicatorImpl;
import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.Gupiao;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoRepository;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.strategy.KzzStrategy;
import com.chuancai.tfish.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.trading.rules.IsEqualRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import javax.annotation.Resource;
import java.util.List;

/**
 * Quickstart for ta4j.
 *
 * Global example.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TodayTasksTest {


    @Resource
    private KzzStrategy kzzStrategy;

    @Resource
    private GupiaoRepository gupiaoRepository;

    @Resource
    private GupiaoXinhaoManager gupiaoXinhaoManager;



    @Test
    public void getData() {

        String bondId = "123102";
        kzzStrategy.setPeriod(5); //按天
        BarSeries series = kzzStrategy.getBarSeries(bondId); //获取k数据
        List<GupiaoXinhao> listXinhao = kzzStrategy.addZjrcIndicator(series); //数据
        gupiaoXinhaoManager.saveGupiaoXinhao(listXinhao);






    }




}
