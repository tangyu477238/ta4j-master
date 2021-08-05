package com.chuancai.tfish.repository;


import com.chuancai.tfish.model.XueqiuGupiaoGainian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GupiaoGainianRepository extends JpaRepository<XueqiuGupiaoGainian,Integer> {

    XueqiuGupiaoGainian findBySymbolAndGainian(String bondId, String gainian);
}
