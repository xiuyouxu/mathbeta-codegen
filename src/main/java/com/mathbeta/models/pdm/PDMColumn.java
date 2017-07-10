package com.mathbeta.models.pdm;

import javax.xml.bind.annotation.*;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "o:Column")
public class PDMColumn {
    @XmlAttribute(name = "Id")
    private String id;
    @XmlAttribute(name = "Ref")
    private String ref;
    @XmlElement(name = "a:Name")
    private String name;
    @XmlElement(name = "a:Code")
    private String code;
    @XmlElement(name = "a:DataType")
    private String dataType;
    @XmlElement(name = "a:Length")
    private int length;
    @XmlElement(name = "a:Mandatory")
    private int mandatory;

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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMandatory() {
        return mandatory;
    }

    public void setMandatory(int mandatory) {
        this.mandatory = mandatory;
    }
}
