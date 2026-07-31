package com.zjsru.entity.vo;

import lombok.Data;

@Data
public class NoticeVO {
    private Integer id;
    private String title;
    private String category;
    private String type;
    private String date;
    private String author;
    private String content;
}