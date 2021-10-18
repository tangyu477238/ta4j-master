package com.chuancai.tfish.repository;


import com.chuancai.tfish.model.GupiaoXinhao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GupiaoXinhaoRepository extends JpaRepository<GupiaoXinhao,Integer> {

    GupiaoXinhao findBySymbolAndTypeNameAndBizDateAndPeriod(String bondId, String typeName, String bizDate, Integer period);

    @Query(value = "select max(biz_date) as biz_date  from  gupiao_xinhao  where symbol=?1 and period=?2 and type_name=?3 " , nativeQuery = true)
    String getMaxBizDate(String bondId,Integer period, String typeName);
}
