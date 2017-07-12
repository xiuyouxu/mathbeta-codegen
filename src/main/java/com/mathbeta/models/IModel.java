package com.mathbeta.models;

import com.mathbeta.models.common.Table;

import java.util.List;

/**
 * model interface. specific model should implement this interface
 * 模型接口，具体的模型应该实现该接口
 *
 * Created by xiuyou.xu on 2017/7/12.
 */
public interface IModel<T extends Table> {
    List<T> getTables();
}
