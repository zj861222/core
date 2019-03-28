package com.framework.core.search.index.factory.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.util.List;

/**
 */
@XmlRootElement(name = "IndexConfigs")
public class IndexConfigs {
    @XmlElement(name = "client")
    private List<ClientConfig> clientConfigs;

    @XmlTransient
    public List<ClientConfig> getClientConfigs() {
        return clientConfigs;
    }

    public void setClientConfigs(List<ClientConfig> clientConfigs) {
        this.clientConfigs = clientConfigs;
    }

}
