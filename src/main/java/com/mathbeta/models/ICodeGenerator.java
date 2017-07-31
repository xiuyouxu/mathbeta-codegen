package com.mathbeta.models;

/**
 * code generator interface, specific generator should generate code from specific model
 * 代码生成器接口，具体的生成器应该根据具体的模型生成代码
 * <p>
 * Created by xiuyou.xu on 2017/7/12.
 */
public interface ICodeGenerator {
    /**
     * @param model           模型实例
     * @param parentPath      生成的代码所在路径
     * @param tableNamePrefix 模型中的表名前缀
     */
    void generateCode(IModel model, String parentPath, String tableNamePrefix);
}
