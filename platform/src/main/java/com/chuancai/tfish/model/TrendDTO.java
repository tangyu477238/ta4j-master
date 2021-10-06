package com.chuancai.tfish.model;

import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
@Data
public class TrendDTO {
    @Column(length = 20)
    private String beforeDate;//前底
    @Column(length = 20)
    private String afterDate;//前顶
    @Column(length = 20)
    private String beforeDate2;
    @Column(length = 20)
    private String afterDate2;
    @Column(length = 20)
    private String beforeDate3;
    @Column(length = 20)
    private String afterDate3;
    @Column(length = 20)
    private String beforeDate4;
    @Column(length = 20)
    private String afterDate4;
    @Column(length = 20)
    private String beforeDate5;
    @Column(length = 20)
    private String afterDate5;


    @Column(precision = 20, scale = 3)
    private BigDecimal upPrice1;
    @Column(precision = 20, scale = 3)
    private BigDecimal upPrice2;
    @Column(precision = 20, scale = 3)
    private BigDecimal upPrice3;
    @Column(precision = 20, scale = 3)
    private BigDecimal upPrice4;
    @Column(precision = 20, scale = 3)
    private BigDecimal upPrice5;

    @Column(precision = 20, scale = 3)
    private BigDecimal downPrice1;
    @Column(precision = 20, scale = 3)
    private BigDecimal downPrice2;
    @Column(precision = 20, scale = 3)
    private BigDecimal downPrice3;
    @Column(precision = 20, scale = 3)
    private BigDecimal downPrice4;
    @Column(precision = 20, scale = 3)
    private BigDecimal downPrice5;

}
