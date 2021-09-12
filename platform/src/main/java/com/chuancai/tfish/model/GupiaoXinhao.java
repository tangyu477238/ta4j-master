package com.chuancai.tfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "gupiao_xinhao")
public class GupiaoXinhao implements Serializable {
    private static final long serialVersionUID = -5395611529404702931L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonProperty("symbol")
    @Column(length = 50)
    private String symbol;

    @JsonProperty("period")
    @Column(length = 11)
    private Integer period;

    @JsonProperty("name")
    @Column(length = 50)
    private String name;

    @JsonProperty("type")
    @Column(length = 10)
    private int type;

    @JsonProperty("type_name")
    @Column(length = 10)
    private String typeName;


    @JsonProperty("biz_date")
    @Column(length = 50)
    private String bizDate;
    @JsonProperty("biz_date2")
    @Column(length = 50)
    private String bizDate2;
    @JsonProperty("biz_date3")
    @Column(length = 50)
    private String bizDate3;
    @JsonProperty("biz_date4")
    @Column(length = 50)
    private String bizDate4;
    @JsonProperty("biz_date5")
    @Column(length = 50)
    private String bizDate5;

    @JsonProperty("biz_date6")
    @Column(length = 50)
    private String bizDate6;
    @JsonProperty("biz_date7")
    @Column(length = 50)
    private String bizDate7;
    @JsonProperty("biz_date8")
    @Column(length = 50)
    private String bizDate8;
    @JsonProperty("biz_date9")
    @Column(length = 50)
    private String bizDate9;
    @JsonProperty("biz_date10")
    @Column(length = 50)
    private String bizDate10;


    @JsonProperty("sj1")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj1;
    @JsonProperty("sj2")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj2;
    @JsonProperty("sj3")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj3;
    @JsonProperty("sj4")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj4;
    @JsonProperty("sj5")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj5;



    @JsonProperty("sj6")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj6;
    @JsonProperty("sj7")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj7;
    @JsonProperty("sj8")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj8;
    @JsonProperty("sj9")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj9;
    @JsonProperty("sj10")
    @Column(precision = 20, scale = 2)
    private BigDecimal sj10;



    @JsonProperty("price1")
    @Column(precision = 20, scale = 3)
    private BigDecimal price1;
    @JsonProperty("price2")
    @Column(precision = 20, scale = 3)
    private BigDecimal price2;
    @JsonProperty("price3")
    @Column(precision = 20, scale = 3)
    private BigDecimal price3;
    @JsonProperty("price4")
    @Column(precision = 20, scale = 3)
    private BigDecimal price4;
    @JsonProperty("price5")
    @Column(precision = 20, scale = 3)
    private BigDecimal price5;



    @JsonProperty("price6")
    @Column(precision = 20, scale = 3)
    private BigDecimal price6;
    @JsonProperty("price7")
    @Column(precision = 20, scale = 3)
    private BigDecimal price7;
    @JsonProperty("price8")
    @Column(precision = 20, scale = 3)
    private BigDecimal price8;
    @JsonProperty("price9")
    @Column(precision = 20, scale = 3)
    private BigDecimal price9;
    @JsonProperty("price10")
    @Column(precision = 20, scale = 3)
    private BigDecimal price10;







}
