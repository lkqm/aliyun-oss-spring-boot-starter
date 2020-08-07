package com.github.lkqm.spring.aliyun.oss.template;

public interface Consumer<T> {

    void accept(T t);
}