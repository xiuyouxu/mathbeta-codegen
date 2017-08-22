package com.mathbeta.models;

import java.util.function.Predicate;

/**
 * Created by xiuyou.xu on 2017/7/31.
 */
public interface ColumnFilterGenerator<T extends Column, U extends Key> {
    /**
     * 创建非主键字段过滤器谓词
     *
     * @param table
     * @return
     */
    Predicate<T> generateNonKeyColumnFilter(Table table);

    /**
     * 创建判断给定字段是否为主键谓词
     *
     * @param column
     * @return
     */
    Predicate<U> determineColumnIsKeyFilter(T column);

    /**
     * 创建实体类属性字段过滤器谓词
     *
     * @return
     */
    Predicate<T> generateEntityFieldsFilter();
}
