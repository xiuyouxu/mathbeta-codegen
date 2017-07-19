package com.mathbeta.models.sql;

import com.mathbeta.models.IModel;

import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/18.
 */
public class SqlModel implements IModel<SqlTable> {
    private List<SqlTable> tables;

    @Override
    public List<SqlTable> getTables() {
        return tables;
    }

    public void setTables(List<SqlTable> tables) {
        this.tables = tables;
    }
}
