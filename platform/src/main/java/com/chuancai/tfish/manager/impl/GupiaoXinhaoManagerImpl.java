package com.chuancai.tfish.manager.impl;

import com.chuancai.tfish.manager.GupiaoXinhaoManager;
import com.chuancai.tfish.model.GupiaoXinhao;
import com.chuancai.tfish.repository.GupiaoXinhaoRepository;
import com.chuancai.tfish.util.DateTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service("gupiaoXinhaoManager")
public class GupiaoXinhaoManagerImpl implements GupiaoXinhaoManager {

    @Resource
    private GupiaoXinhaoRepository gupiaoXinhaoRepository;

    @Override
    public void saveGupiaoXinhao(List<GupiaoXinhao> list) {

        for (int i = 0; i < list.size(); i++) {
            GupiaoXinhao gupiaoXinhao = list.get(i);
            GupiaoXinhao gupiaoXinhao_1  = gupiaoXinhaoRepository.findBySymbolAndTypeNameAndBizDateAndPeriod(gupiaoXinhao.getSymbol(),
                    gupiaoXinhao.getTypeName(), gupiaoXinhao.getBizDate(), gupiaoXinhao.getPeriod());
            if (gupiaoXinhao_1 == null){
                gupiaoXinhaoRepository.save(gupiaoXinhao);
                continue;
            }
            if (gupiaoXinhao.getBizDate().startsWith(DateTimeUtil.getBeforeDay(0))) { //如果是当天，请覆盖
                gupiaoXinhao.setId(gupiaoXinhao_1.getId());
                gupiaoXinhaoRepository.save(gupiaoXinhao);
                continue;
            }
        }
    }


}
