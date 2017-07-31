package com.mathbeta.models.pdm;

import com.mathbeta.models.Key;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "o:Key")
public class PDMKey implements Key<PDMColumn> {
    @XmlAttribute(name = "Id")
    private String id;
    @XmlAttribute(name = "Ref")
    private String ref;
    @XmlElement(name = "a:Name")
    private String description;
    @XmlElement(name = "a:Code")
    private String name;
    @XmlElementWrapper(name = "c:Key.Columns")
    @XmlElement(name = "o:Column")
    private List<PDMColumn> columns;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDescription() {
        return description;
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
}
