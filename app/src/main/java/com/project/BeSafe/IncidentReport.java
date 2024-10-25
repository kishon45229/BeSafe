package com.project.BeSafe;

public class IncidentReport {
    public String userId;
    public String streetAddress;
    public String city;
    public String district;
    public String dob;
    public String time;
    public String incidentType;
    public String severity;
    public String injuredChildrenCount;
    public String injuredAdultsCount;
    public String childFatalitiesCount;
    public String adultFatalitiesCount;
    public String propertyDamageValue;
    public String descriptionValue;
    public String userName;
    public String userMobileNo;
    public String additionalComments;

    public IncidentReport(String userId, String streetAddress, String city, String district, String dob, String time,
                          String incidentType, String severity, String injuredChildrenCount, String injuredAdultsCount,
                          String childFatalitiesCount, String adultFatalitiesCount, String propertyDamageValue,
                          String descriptionValue, String userName, String userMobileNo, String additionalComments) {
        this.userId = userId;
        this.streetAddress = streetAddress;
        this.city = city;
        this.district = district;
        this.dob = dob;
        this.time = time;
        this.incidentType = incidentType;
        this.severity = severity;
        this.injuredChildrenCount = injuredChildrenCount;
        this.injuredAdultsCount = injuredAdultsCount;
        this.childFatalitiesCount = childFatalitiesCount;
        this.adultFatalitiesCount = adultFatalitiesCount;
        this.propertyDamageValue = propertyDamageValue;
        this.descriptionValue = descriptionValue;
        this.userName = userName;
        this.userMobileNo = userMobileNo;
        this.additionalComments = additionalComments;
    }
}
