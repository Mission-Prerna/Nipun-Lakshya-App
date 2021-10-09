package com.samagra.commons;

/**
 * A POJO for representing the a InstitutionInfo.
 */
public class InstitutionInfo {

    public String getDistrict() {
        return District;
    }

    public String getBlock() {
        return Block;
    }

    public String getCluster() {
        return Cluster;
    }

    public String getSchoolName() {
        return SchoolName;
    }

    public String getUdise() {
        return Udise;
    }

    private String District;
    private String Block;
    private String Cluster;
    private String SchoolName;
    private String Udise;

    public String getClusterCode() {
        return ClusterCode;
    }

    public void setClusterCode(String clusterCode) {
        ClusterCode = clusterCode;
    }

    private String ClusterCode;


    public String getStringForSearch() {
        return this.District + " "
                + this.Block + " "
                + this.Cluster + " "
                + this.Udise + " "
                + this.SchoolName+ " "
                +this.ClusterCode;
    }

    public InstitutionInfo(String district, String block, String cluster, String schoolName,
                           String udise, String clusterCode) {
        this.Block = block;
        this.SchoolName = schoolName;
        this.ClusterCode = clusterCode;
        this.District = district;
        this.Cluster =cluster;
        this.Udise = udise;
    }

}
