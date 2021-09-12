package com.chuancai.tfish.model;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TrendDTO {
    private Integer trend;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;

}
