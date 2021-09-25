package com.chuancai.tfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "gupiao_kline_5m")
public class GupiaoKline5m implements Serializable {
    private static final long serialVersionUID = -5395611529404702931L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String symbol;
    private Integer period;

    @JsonProperty("biz_date")
    private String bizDate;
    private Date timestamp;

    private BigDecimal volume;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal amount;

    private double chg;
    private double percent;
    private double turnoverrate;


    @JsonProperty("volume_post")
    private String volumePost;
    @JsonProperty("amount_post")
    private String amountPost;
    private String pe;
    @Column(precision = 20, scale = 3)
    private BigDecimal pePrice;
    private String pb;
    private String pc;
    private String ps;
    private String pcf;
    @Column(precision = 20, scale = 3)
    private BigDecimal pcfPrice;
    @JsonProperty("market_capital")
    private String marketCapital;
    private String balance;
    @JsonProperty("hold_volume_cn")
    private String holdVolumeCn;
    @JsonProperty("hold_ratio_cn")
    private String holdRatioCn;
    @JsonProperty("net_volume_cn")
    private String netVolumeCn;
    @JsonProperty("hold_volume_hk")
    private String holdVolumeHk;
    @JsonProperty("hold_ratio_hk")
    private String holdRatioHk;
    @JsonProperty("net_volume_hk")
    private String netVolumeHk;

    @Column(length = 1)
    private Integer trend;//趋势(1上升0下降)
    //处理包含后的低价
    @Column(precision = 20, scale = 3)
    private BigDecimal newLow;
    //处理包含后的高价
    @Column(precision = 20, scale = 3)
    private BigDecimal newHigh;

    @Column(length = 1)
    private Integer isMerge;//是否合并有效（1有效，0无效）

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

    //合并后重新计算的
    @Column(precision = 20, scale = 3)
    private BigDecimal mergeLow;
    //合并后重新计算的
    @Column(precision = 20, scale = 3)
    private BigDecimal mergeHigh;


    @Column(length = 1)
    private Integer yiTrend;//趋势(1上升0下降)
    @Column(precision = 20, scale = 3)
    private BigDecimal yiLow;
    @Column(precision = 20, scale = 3)
    private BigDecimal yiHigh;

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
