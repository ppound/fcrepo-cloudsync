package com.github.cwilper.fcrepo.cloudsync.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="configuration")
public class Configuration {

    private Integer keepSysLogDays;
    private Integer keepTaskLogDays;
    private String cloudSyncVersion;
    private String cloudSyncBuildDate;

    public String getCloudSyncVersion() {
        return cloudSyncVersion;
    }

    public void setCloudSyncVersion(String cloudSyncVersion) {
        this.cloudSyncVersion = cloudSyncVersion;
    }

    public String getCloudSyncBuildDate() {
        return cloudSyncBuildDate;
    }

    public void setCloudSyncBuildDate(String cloudSyncBuildDate) {
        this.cloudSyncBuildDate = cloudSyncBuildDate;
    }

    public Integer getKeepSysLogDays() {
        return keepSysLogDays;
    }

    public void setKeepSysLogDays(Integer keepSysLogDays) {
        this.keepSysLogDays = keepSysLogDays;
    }

    public Integer getKeepTaskLogDays() {
        return keepTaskLogDays;
    }

    public void setKeepTaskLogDays(Integer keepTaskLogDays) {
        this.keepTaskLogDays = keepTaskLogDays;
    }
}
