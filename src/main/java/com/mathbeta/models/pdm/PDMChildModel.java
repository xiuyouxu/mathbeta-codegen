package com.mathbeta.models.pdm;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/10.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "o:Model")
public class PDMChildModel {
    @XmlAttribute(name = "Id")
    private String id;
    @XmlElementWrapper(name = "c:Tables")
    @XmlElement(name = "o:Table")
    private List<PDMTable> tables;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PDMTable> getTables() {
        return tables;
    }

    public void setTables(List<PDMTable> tables) {
        this.tables = tables;
    }
}
