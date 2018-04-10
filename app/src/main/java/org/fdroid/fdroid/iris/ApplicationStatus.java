package org.fdroid.fdroid.iris;

/**
 * Created by Khaled on 4/9/2018.
 * Assumptions
 * Descriptions
 */

public class ApplicationStatus {

    private String clientId;
    private String applicationId;
    private String version;
    private String webDownloadDate;
    private String deviceDownloadDate;
    private String installationDate;
    private String status;

    public ApplicationStatus() {
    }

    public ApplicationStatus(String clientId, String applicationId, String version, String webDownloadDate, String deviceDownloadDate, String installationDate, String status) {
        this.clientId = clientId;
        this.applicationId = applicationId;
        this.version = version;
        this.webDownloadDate = webDownloadDate;
        this.deviceDownloadDate = deviceDownloadDate;
        this.installationDate = installationDate;
        this.status = status;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getVersion() {
        return version;
    }

    public String getWebDownloadDate() {
        return webDownloadDate;
    }

    public String getDeviceDownloadDate() {
        return deviceDownloadDate;
    }

    public String getInstallationDate() {
        return installationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWebDownloadDate(String webDownloadDate) {
        this.webDownloadDate = webDownloadDate;
    }

    public void setDeviceDownloadDate(String deviceDownloadDate) {
        this.deviceDownloadDate = deviceDownloadDate;
    }

    public void setInstallationDate(String installationDate) {
        this.installationDate = installationDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
