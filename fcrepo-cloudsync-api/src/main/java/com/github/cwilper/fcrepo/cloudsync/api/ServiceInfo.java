package com.github.cwilper.fcrepo.cloudsync.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="service")
public class ServiceInfo {

    private boolean isInitialized;
    private String version;
    private String buildDate;
    private String initialAdminUsername;
    private String initialAdminPassword;
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public String getInitialAdminUsername() {
        return initialAdminUsername;
    }

    public void setInitialAdminUsername(String initialAdminUsername) {
        this.initialAdminUsername = initialAdminUsername;
    }

    public String getInitialAdminPassword() {
        return initialAdminPassword;
    }

    public void setInitialAdminPassword(String initialAdminPassword) {
        this.initialAdminPassword = initialAdminPassword;
    }

}
