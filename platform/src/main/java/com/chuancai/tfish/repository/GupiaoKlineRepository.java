package com.chuancai.tfish.repository;

import com.chuancai.tfish.model.GupiaoKline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface GupiaoKlineRepository extends JpaRepository<GupiaoKline,Integer> {

    List<GupiaoKline> findBySymbolOrderByBizDate(String bondId);

    GupiaoKline findBySymbolAndPeriodAndTimestamp(String bondId, String period, Date date);

    GupiaoKline findBySymbolAndPeriodAndBizDate(String bondId, String period, String bizDate);

    @Query(value = "select * from gupiao_kline_5m where symbol = ?1 and biz_date>='2020-01-01' order by biz_date desc  LIMIT 0, ?2 ", nativeQuery = true)
    List<GupiaoKline> getSymbolTop(String bondId, int topNum);

}
