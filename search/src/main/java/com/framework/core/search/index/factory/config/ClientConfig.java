package com.framework.core.search.index.factory.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


import java.util.HashMap;
import java.util.List;

/**
 * 解析配置文件
 */
public class ClientConfig {
    @XmlAttribute
    private String name;

    @XmlElement
    private String factory;

    @XmlElement(name = "index")
    private List<IndexConfig> indexConfigs;
    
    @XmlJavaTypeAdapter(HashMapAdapter.class)
    private HashMap<String,String> properties;

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setProperties(HashMap<String,String> properties) {
        this.properties = properties;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public HashMap<String,String> getProperties() {
        return properties;
    }

    @XmlTransient
    public String getFactory() {
        return factory;
    }

    @XmlTransient
    public String getName() {
        return name;
    }

    @XmlTransient
    public List<IndexConfig> getIndexConfigs() {
        return indexConfigs;
    }

    public void setIndexConfigs(List<IndexConfig> indexConfigs) {
        this.indexConfigs = indexConfigs;
    }
}
