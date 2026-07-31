package com.zjsru.entity.vo;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProfileVO {
    private Integer id;
    private String realName;
    private String phone;
    private String idCard;
    private BigDecimal balance;
}