package com.mathbeta.models.sql;

import com.mathbeta.models.common.Table;

import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/18.
 */
public class SqlTable implements Table<SqlColumn, SqlKey> {
    private String description;
    private String name;
    private List<SqlColumn> columns;
    private List<SqlKey> keys;
    private List<SqlKey> primaryKeys;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<SqlColumn> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<SqlColumn> columns) {
        this.columns = columns;
    }

    @Override
    public List<SqlKey> getKeys() {
        return keys;
    }

    @Override
    public void setKeys(List<SqlKey> keys) {
        this.keys = keys;
    }

    @Override
    public List<SqlKey> getPrimaryKeys() {
        return primaryKeys;
    }

    @Override
    public void setPrimaryKeys(List<SqlKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    @Override
    public void setIsEntity(boolean isEntity) {

    }

    @Override
    public boolean isEntity() {
        return false;
    }
}
