package com.tcwang.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: JKin
 * @createTime: 2023/7/19 12:05
 * @fileDescriptions: You had better view this comment before you use it.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataFromUser {

    private String id;

    private String name;

    private String habit;

    private Integer age;

    private Date birthday;

    private Date joinTime;
}
