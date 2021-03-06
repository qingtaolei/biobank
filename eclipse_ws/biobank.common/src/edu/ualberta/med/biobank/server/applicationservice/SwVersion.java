package edu.ualberta.med.biobank.server.applicationservice;

import edu.ualberta.med.biobank.server.applicationservice.exceptions.VersionFormatInvalidException;

/**
 * The following is as per http://wiki.eclipse.org/index.php/Version_Numbering
 * 
 */
public class SwVersion {

    private int major;

    private int minor;

    private int service;

    private String qualifier;

    public SwVersion() {
        this.major = 0;
        this.minor = 0;
        this.service = 0;
        this.qualifier = ""; //$NON-NLS-1$
    }

    public SwVersion(String versionString) throws VersionFormatInvalidException {
        String[] versionSplit = versionString.split("\\."); //$NON-NLS-1$

        if (versionSplit.length < 3 || versionSplit.length > 4) {
            throw new VersionFormatInvalidException();
        }

        try {
            this.major = Integer.parseInt(versionSplit[0]);
            this.minor = Integer.parseInt(versionSplit[1]);
            this.service = Integer.parseInt(versionSplit[2]);
        } catch (NumberFormatException e) {
            throw new VersionFormatInvalidException();
        }
        if (versionSplit.length == 4)
            this.qualifier = versionSplit[3];
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return getMajor() + "." + getMinor() + "." + getService()
            + (qualifier != null ? "." + getQualifier() : "");
    }
}
