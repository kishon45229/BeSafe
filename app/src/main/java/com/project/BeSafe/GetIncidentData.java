package com.project.BeSafe;

import java.util.Map;

public class GetIncidentData {
    private String userId;
    private String streetAddress;
    private String city;
    private String district;
    private String dob;
    private String time;
    private String incidentType;
    private String severity;
    private String injuredChildrenCount;
    private String injuredAdultsCount;
    private String childFatalitiesCount;
    private String adultFatalitiesCount;
    private String propertyDamageValue;
    private String descriptionValue;
    private String userName;
    private String userMobileNo;
    private String emergencyResponse;
    private String additionalComment;
    private Map<String, String> media; // For storing media links

    public GetIncidentData() {
        // Default constructor required for calls to DataSnapshot.getValue(GetIncidentData.class)
    }

    // Getters and setters for each field, including media
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStreetAddress() {
        return streetAddress;
    }
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }
    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDob() {
        return dob;
    }
    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getIncidentType() {
        return incidentType;
    }
    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getInjuredChildrenCount() {
        return injuredChildrenCount;
    }
    public void setInjuredChildrenCount(String injuredChildrenCount) {
        this.injuredChildrenCount = injuredChildrenCount;
    }

    public String getInjuredAdultsCount() {
        return injuredAdultsCount;
    }
    public void setInjuredAdultsCount(String injuredAdultsCount) {
        this.injuredAdultsCount = injuredAdultsCount;
    }

    public String getChildFatalitiesCount() {
        return childFatalitiesCount;
    }
    public void setChildFatalitiesCount(String childFatalitiesCount) {
        this.childFatalitiesCount = childFatalitiesCount;
    }

    public String getAdultFatalitiesCount() {
        return adultFatalitiesCount;
    }
    public void setAdultFatalitiesCount(String adultFatalitiesCount) {
        this.adultFatalitiesCount = adultFatalitiesCount;
    }

    public String getPropertyDamageValue() {
        return propertyDamageValue;
    }
    public void setPropertyDamageValue(String propertyDamageValue) {
        this.propertyDamageValue = propertyDamageValue;
    }

    public String getDescriptionValue() {
        return descriptionValue;
    }
    public void setDescriptionValue(String descriptionValue) {
        this.descriptionValue = descriptionValue;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMobileNo() {
        return userMobileNo;
    }
    public void setUserMobileNo(String userMobileNo) {
        this.userMobileNo = userMobileNo;
    }

    public String getAdditionalComment() {
        return additionalComment;
    }
    public void setAdditionalComment(String additionalComment) {
        this.additionalComment = additionalComment;
    }

    public Map<String, String> getMedia() {
        return media;
    }
    public void setMedia(Map<String, String> media) {
        this.media = media;
    }
}
