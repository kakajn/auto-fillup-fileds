package com.tcwang.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: kakajn
 * @createTime: 2023/7/17 11:56
 * @fileDescriptions:
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MappingFiledName {
    String mappingFiledName() default "";
}
