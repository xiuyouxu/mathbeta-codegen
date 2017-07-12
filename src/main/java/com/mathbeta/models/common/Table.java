package com.mathbeta.models.common;

import java.util.List;

/**
 * <h2>Table interface</h2>
 * <p>Specific Table should have fields:</p>
 * <p>name, i.e. the table name in db</p>
 * <p>description, i.e. the table description</p>
 * <p>isEntity, whether the table stands for an object entity in real world. For example a relation table only contains foreign keys of other tables should not be an object entity.</p>
 * <p>columns, the columns of the table</p>
 * <p>keys, the table keys</p>
 * <p>primaryKeys, the table primary keys</p>
 * <p>
 * Created by xiuyou.xu on 2017/7/12.
 */
public interface Table<T extends Column, U extends Key> {
    String getName();

    void setName(String name);

    void setDescription(String description);

    String getDescription();

    void setIsEntity(boolean isEntity);

    boolean isEntity();

    void setColumns(List<T> columns);

    List<T> getColumns();

    void setKeys(List<U> keys);

    List<U> getKeys();

    void setPrimaryKeys(List<U> primaryKeys);

    List<U> getPrimaryKeys();
}
