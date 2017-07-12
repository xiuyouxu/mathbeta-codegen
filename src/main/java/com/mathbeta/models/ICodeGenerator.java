package com.mathbeta.models;

/**
 * code generator interface, specific generator should generate code from specific model
 * 代码生成器接口，具体的生成器应该根据具体的模型生成代码
 *
 * Created by xiuyou.xu on 2017/7/12.
 */
public interface ICodeGenerator {
    void generateCode(IModel model);
}
