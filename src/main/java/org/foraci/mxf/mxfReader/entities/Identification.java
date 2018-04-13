package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;

import java.util.Map;
import java.util.Date;

/**
 * Date: Oct 2, 2009 3:55:07 PM
 *
 * @author jforaci
 */
public class Identification implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private String companyName, productName;
    private Object productVersion;
    private String versionString;
    private UID productId;
    private Date modificationDate;
    private Object toolkitVersion;
    private String platform;

    public UID getId() {
        return id;
    }

    public void setId(UID id) {
        this.id = id;
    }

    public UID getGenId() {
        return genId;
    }

    public void setGenId(UID genId) {
        this.genId = genId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Object getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(Object productVersion) {
        this.productVersion = productVersion;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public UID getProductId() {
        return productId;
    }

    public void setProductId(UID productId) {
        this.productId = productId;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Object getToolkitVersion() {
        return toolkitVersion;
    }

    public void setToolkitVersion(Object toolkitVersion) {
        this.toolkitVersion = toolkitVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void resolve(Map map) {
    }
}
