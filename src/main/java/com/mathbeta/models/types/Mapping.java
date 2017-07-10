package com.mathbeta.models.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by xiuyou.xu on 2017/7/10.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mapping")
public class Mapping {
    @XmlAttribute(name = "jdbc")
    private String jdbc;
    @XmlAttribute(name = "java")
    private String java;

    public String getJdbc() {
        return jdbc;
    }

    public void setJdbc(String jdbc) {
        this.jdbc = jdbc;
    }

    public String getJava() {
        return java;
    }

    public void setJava(String java) {
        this.java = java;
    }
}
