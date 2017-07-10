package com.mathbeta.models.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/10.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "type-mappings")
public class TypeMapping {
    @XmlElement(name = "db-type")
    private List<DbType> types;

    public List<DbType> getTypes() {
        return types;
    }

    public void setTypes(List<DbType> types) {
        this.types = types;
    }
}
