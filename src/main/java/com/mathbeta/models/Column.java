package com.mathbeta.models;

/**
 * <h2>Column interface</h2>
 * <p>Table Column should contain fields:</p>
 * <p>name, the column name</p>
 * <p>description, the column description</p>
 * <p>dataType, the data type of the column</p>
 * <p>length, the max data length of the column</p>
 * <p>defaultValue, the column default value</p>
 * <p>nullable, whether the column is nullable</p>
 *
 * Created by xiuyou.xu on 2017/7/12.
 */
public interface Column {
    public String getName();

    public void setName(String name);

    void setDescription(String description);

    String getDescription();

    String getDataType();

    void setDataType(String dataType);

    int getLength();

    void setLength(int length);

    Object getDefaultValue();

    void setDefaultValue(Object defaultValue);

    void setNullable(boolean nullable);

    boolean isNullable();
}
