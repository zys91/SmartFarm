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
public class AppMetaDataBean {

    private int version;
    private ArtifactType artifactType;
    private String applicationId;
    private String variantName;
    private List<Elements> elements;
    public void setVersion(int version) {
        this.version = version;
    }
    public int getVersion() {
        return version;
    }

    public void setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
    }
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    public String getApplicationId() {
        return applicationId;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }
    public String getVariantName() {
        return variantName;
    }

    public void setElements(List<Elements> elements) {
        this.elements = elements;
    }
    public List<Elements> getElements() {
        return elements;
    }

}