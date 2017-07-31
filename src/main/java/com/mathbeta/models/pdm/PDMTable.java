package com.mathbeta.models.pdm;

import com.mathbeta.models.Table;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "o:Table")
public class PDMTable implements Table<PDMColumn, PDMKey> {
    @XmlElement(name = "a:Name")
    private String description;
    @XmlElement(name = "a:Code")
    private String name;
    @XmlElementWrapper(name = "c:Columns")
    @XmlElement(name = "o:Column")
    private List<PDMColumn> columns;
    @XmlElementWrapper(name = "c:Keys")
    @XmlElement(name = "o:Key")
    private List<PDMKey> keys;
    @XmlElementWrapper(name = "c:PrimaryKey")
    @XmlElement(name = "o:Key")
    private List<PDMKey> primaryKeys;

    public String getDescription() {
        return description;
    }

    @Override
    public void setIsEntity(boolean isEntity) {

    }

    @Override
    public boolean isEntity() {
        return false;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PDMColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<PDMColumn> columns) {
        this.columns = columns;
    }

    public List<PDMKey> getKeys() {
        return keys;
    }

    public void setKeys(List<PDMKey> keys) {
        this.keys = keys;
    }

    public List<PDMKey> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<PDMKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }
}
