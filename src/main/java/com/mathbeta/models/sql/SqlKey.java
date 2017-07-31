package com.mathbeta.models.sql;

import com.mathbeta.models.Key;

import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/18.
 */
public class SqlKey implements Key<SqlColumn> {
    private String name;
    private List<SqlColumn> columns;
    private String description;

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

    public void setColumns(List<SqlColumn> columns) {
        this.columns = columns;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
