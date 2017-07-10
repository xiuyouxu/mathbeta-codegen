package com.mathbeta.models.types;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/10.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "db-type")
public class DbType {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "mapping")
    private List<Mapping> mappings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }
}
