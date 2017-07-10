package com.mathbeta.models.pdm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Model")
public class PDMModel {
    @XmlElement(name = "o:RootObject")
    private PDMRootObject rootObject;
//    @XmlElementWrapper(name = "c:Tables")
//    @XmlElement(name = "o:Table")
//    private List<PDMTable> tables;

//    public List<PDMTable> getTables() {
//        return tables;
//    }
//
//    public void setTables(List<PDMTable> tables) {
//        this.tables = tables;
//    }

    public PDMRootObject getRootObject() {
        return rootObject;
    }

    public void setRootObject(PDMRootObject rootObject) {
        this.rootObject = rootObject;
    }
}
