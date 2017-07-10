package com.mathbeta.models;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
public interface ICodeGen {
    /**
     * 创建实体对象类
     */
    void genEntity();

    /**
     * 创建Restful接口类
     */
    void genRestful();

    /**
     * 创建dubbo接口类、dubbo provider类及dubbo consumer、provider配置文件内容
     */
    void genDubbo();

    /**
     * 创建Mybatis Mapper接口类定义及mapper配置文件内容
     */
    void genMapper();
}
