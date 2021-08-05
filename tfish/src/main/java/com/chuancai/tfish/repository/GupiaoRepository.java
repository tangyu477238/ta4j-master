package com.chuancai.tfish.repository;


import com.chuancai.tfish.model.Gupiao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GupiaoRepository extends JpaRepository<Gupiao,Integer> {

    Gupiao findBySymbol(String bondId);
}
