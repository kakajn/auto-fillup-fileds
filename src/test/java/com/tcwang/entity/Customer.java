package com.tcwang.entity;

import com.tcwang.anno.MappingFiledName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: JKin
 * @createTime: 2023/7/19 12:04
 * @fileDescriptions: You had better view this comment before you use it.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @MappingFiledName(mappingFiledName="id")
    private String id1;

    private String name;

    private String habit;

    private Integer age;
}
