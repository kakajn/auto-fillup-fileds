package com.tcwang.interfaces;

import java.io.Serializable;

/**
 * @author: JKin
 * @createTime: 2023/7/31 10:20
 * @fileDescriptions: You had better view this comment before you use it.
 *  这个接口是为了得到把get方法通过lambda表达式得到他的字段名称的接口.(This interface is a functional interface for transfer a get method
 *  to it is relative filed name)
 */
@FunctionalInterface
public interface TGet<T> extends Serializable {

    void getProperties(T source);
}
