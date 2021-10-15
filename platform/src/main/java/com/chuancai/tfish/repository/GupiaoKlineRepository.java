package com.chuancai.tfish.repository;

import com.chuancai.tfish.model.GupiaoKline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface GupiaoKlineRepository extends JpaRepository<GupiaoKline,Integer> {


    @Query(value = "select symbol from gupiao where symbol like '11%' or  symbol like '12%' ", nativeQuery = true)
    List<String> listKzz();

    List<GupiaoKline> findBySymbolOrderByBizDate(String bondId);

    GupiaoKline findBySymbolAndPeriodAndTimestamp(String bondId, String period, Date date);

    GupiaoKline findBySymbolAndPeriodAndBizDate(String bondId, String period, String bizDate);

    @Query(value = "select * from gupiao_kline_5m where symbol = ?1  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<GupiaoKline> getKline5m(String bondId);

    @Query(value = "select * from gupiao_kline_15m where symbol = ?1  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<GupiaoKline> getKline15m(String bondId);

    @Query(value = "select * from gupiao_kline_30m where symbol = ?1  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<GupiaoKline> getKline30m(String bondId);

    @Query(value = "select * from gupiao_kline_60m where symbol = ?1  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<GupiaoKline> getKline60m(String bondId);

    @Query(value = "select * from gupiao_kline_120m where symbol = ?1  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<GupiaoKline> getKline120m(String bondId);

    @Query(value = "select * from gupiao_kline where symbol = ?1 order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<GupiaoKline> getKline(String bondId);



    @Query(value = "select biz_date as bizDate from gupiao_kline_5m where symbol = ?1 and yi_high is null  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<String> listKlineBizDate5m(String bondId);

    @Query(value = "select biz_date as bizDate from gupiao_kline_15m where symbol = ?1 and yi_high is null  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<String> listKlineBizDate15m(String bondId);

    @Query(value = "select biz_date as bizDate from gupiao_kline_30m where symbol = ?1 and yi_high is null  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<String> listKlineBizDate30m(String bondId);

    @Query(value = "select biz_date as bizDate from gupiao_kline_60m where symbol = ?1 and yi_high is null  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<String> listKlineBizDate60m(String bondId);
    @Query(value = "select biz_date as bizDate from gupiao_kline_120m where symbol = ?1 and yi_high is null  order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<String> listKlineBizDate120m(String bondId);

    @Query(value = "select biz_date as bizDate from gupiao_kline where symbol = ?1 and yi_high is null order by biz_date desc  LIMIT 0, 500 ", nativeQuery = true)
    List<String> listKlineBizDate(String bondId);


}
