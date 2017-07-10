package com.mathbeta.models.pdm;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "o:Key")
public class PDMKey {
    @XmlAttribute(name = "Id")
    private String id;
    @XmlAttribute(name = "Ref")
    private String ref;
    @XmlElement(name = "a:Name")
    private String name;
    @XmlElement(name = "a:Code")
    private String code;
    @XmlElementWrapper(name = "c:Key.Columns")
    @XmlElement(name = "o:Column")
    private List<PDMColumn> keyColumns;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<PDMColumn> getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(List<PDMColumn> keyColumns) {
        this.keyColumns = keyColumns;
    }
}
