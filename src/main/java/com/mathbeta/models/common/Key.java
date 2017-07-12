package com.mathbeta.models.common;

import java.util.List;

/**
 * <h2>Key interface</h2>
 * <p>Table Key should contain fields:</p>
 * <p>name, the key name</p>
 * <p>description, the key description</p>
 * <p>columns, the columns of the key</p>
 *
 * Created by xiuyou.xu on 2017/7/12.
 */
public interface Key<T extends Column> {
    void setName(String name);

    String getName();

    void setDescription(String description);

    String getDescription();

    void setColumns(List<T> columns);

    List<T> getColumns();
}
