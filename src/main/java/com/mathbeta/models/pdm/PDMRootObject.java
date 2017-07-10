package com.mathbeta.models.pdm;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/10.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "o:RootObject")
public class PDMRootObject{
    @XmlAttribute(name = "Id")
    private String id;
    @XmlElementWrapper(name = "c:Children")
    @XmlElement(name = "o:Model")
    private List<PDMChildModel> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PDMChildModel> getChildren() {
        return children;
    }

    public void setChildren(List<PDMChildModel> children) {
        this.children = children;
    }
}
