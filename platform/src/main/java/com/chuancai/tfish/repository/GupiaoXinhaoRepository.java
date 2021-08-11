package com.chuancai.tfish.repository;


import com.chuancai.tfish.model.GupiaoXinhao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GupiaoXinhaoRepository extends JpaRepository<GupiaoXinhao,Integer> {

    GupiaoXinhao findBySymbolAndTypeNameAndBizDate(String bondId, String typeName, String bizDate);
}
