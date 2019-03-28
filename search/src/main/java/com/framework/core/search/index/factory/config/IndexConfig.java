package com.framework.core.search.index.factory.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


import java.util.HashMap;

/**
 */
public class IndexConfig {
    @XmlElement
    private String aliasName;

    @XmlJavaTypeAdapter(HashMapAdapter.class)
    private HashMap<String,String> properties;
    
    @XmlElement
    private String builderClass;
    
    @XmlElement
    private String mappingFile;

    @XmlElement
    private String defaultSearchFields;

    @XmlTransient
    public String getAliasName() {
        return aliasName;
    }

    @XmlTransient
    public HashMap<String,String> getProperties() {
        return properties;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setProperties(HashMap<String,String> properties) {
        this.properties = properties;
    }

    @XmlTransient
    public String getBuilderClass() {
        return builderClass;
    }

    public void setBuilderClass(String builderClass) {
        this.builderClass = builderClass;
    }

    @XmlTransient
    public String getDefaultSearchFields() {
        return defaultSearchFields;
    }

    public void setDefaultSearchFields(String defaultSearchFields) {
        this.defaultSearchFields = defaultSearchFields;
    }

    @XmlTransient
	public String getMappingFile() {
		return mappingFile;
	}

	public void setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
	}
}
