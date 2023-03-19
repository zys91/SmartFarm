/**
 * Copyright 2022 bejson.com
 */
package com.seu.smartfarm.modules.bean;
import java.util.List;

/**
 * Auto-generated: 2022-08-16 22:4:24
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Elements {

    private String type;
    private List<String> filters;
    private int versionCode;
    private String versionName;
    private String outputFile;
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
    public List<String> getFilters() {
        return filters;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    public String getVersionName() {
        return versionName;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
    public String getOutputFile() {
        return outputFile;
    }

}