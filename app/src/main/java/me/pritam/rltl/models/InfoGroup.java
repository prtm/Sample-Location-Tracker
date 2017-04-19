package me.pritam.rltl.models;

/**
 * Created by ghost on 19/4/17.
 */

public class InfoGroup {
    public String getGrpName() {
        return grpName;
    }

    public void setGrpName(String grpName) {
        this.grpName = grpName;
    }

    public String getGrpTime() {
        return grpTime;
    }

    public void setGrpTime(String grpTime) {
        this.grpTime = grpTime;
    }

    public String getGrpImageUrl() {
        return grpImageUrl;
    }

    public void setGrpImageUrl(String grpImageUrl) {
        this.grpImageUrl = grpImageUrl;
    }

    private String grpName;
    private String grpTime;
    private String grpImageUrl;

    public String getGrpFullName() {
        return grpFullName;
    }

    public void setGrpFullName(String grpFullName) {
        this.grpFullName = grpFullName;
    }

    private String grpFullName;
}
