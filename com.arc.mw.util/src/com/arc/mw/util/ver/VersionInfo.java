/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.mw.util.ver;

/**
 * Simple class for storing version info for a MetaWare toolset component.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class VersionInfo {


// in case we add new things later on.
// ver 1 - initial implementation
// ver 2 - minimum field width for minor number
    private int tokenVersion;

// the regular utility's version number. Printed out if
// no public_version present.
    private int major;

    private int minor;

    private int patch;

// first year of copyright
    private int startYear;

// current year of copyright
    private int currentYear;

// the poked value by release group, if public_version[0] is
// non-zero, then this is printed from print_copyright.
    private String publicVersion;

// the poked value by CSG for the specific patch number.
// If csg_version[0] is non-zero, then it will be printed
// out in addition to the engineering version
    private String csgVersion;

// the unique build token - every time utility is compiled,
// this value will change. This is not to be modified by any tool!
    private String buildToken;

// processor name
    private String processorName;

// if present, contains the serial number of the product.
// Typically used by front-ends only.
    private String serialNumber;

// the minimum width of the minor field
    private int minorVersionFieldWidth;
    
    private String error = null;

    public String getErrorMessage () {
        return error;
    }

    public void setErrorMessage (String s) {
        error = s;
    }

    public int getTokenVersion () {
        return tokenVersion;
    }

    public void setTokenVersion (int s) {
        tokenVersion = s;
    }

    public int getMajorVersion () {
        return major;
    }

    public void setMajorVersion (int s) {
        major = s;
    }

    public int getMinorVersion () {
        return minor;
    }

    public void setMinorVersion (int s) {
        minor = s;
    }

    public String getPatchVersion () {
        return Character.isLetterOrDigit(patch)?""+(char)patch:""+patch;
    }

    public void setPatchVersion (int s) {
        patch = s;
    }

    public int getStartYear () {
        return startYear;
    }

    public void setStartYear (int s) {
        startYear = s;
    }

    public int getCurrenttYear () {
        return currentYear;
    }

    public void setCurrentYear (int s) {
        currentYear = s;
    }

    public String getPublicVersion () {
        return publicVersion;
    }

    public void setPublicVersion (String s) {
        publicVersion = s;
    }

    public String getCSGVersion () {
        return csgVersion;
    }

    public void setCSGVersion (String s) {
        csgVersion = s;
    }

    public String getBuildToken () {
        return buildToken;
    }

    public void setBuildToken (String s) {
        buildToken = s;
    }

    public String getProcessorName () {
        return processorName;
    }

    public void setProcessorName (String s) {
        processorName = s;
    }

    public String getSerialNumber () {
        return serialNumber;
    }

    public void setSerialNumber (String s) {
        serialNumber = s;
    }

    public int getMinimuWidth () {
        return minorVersionFieldWidth;
    }

    public void setMinimuWidth (char s) {
        minorVersionFieldWidth = s;
    }
    
    @Override
    public String toString(){
        return toString("");
    }

    public String toString (String componentName) {
        String version = this.getPublicVersion();
        if (version != null && version.length() > 0 ){
            String csg = getCSGVersion();
            if (csg != null && csg.length() > 0 && csg.charAt(0) != '\\'){
                version += "." + csg;              
            }
            if (componentName.indexOf("ebugger") > 0){
                version += ", Engine ID: " + (this.getMajorVersion()*100 + this.getMinorVersion());
            }
        }
        else {
            version = this.getMajorVersion() + "." + this.getMinorVersion() + "." + this.getPatchVersion();
        }
        return componentName + " v. " + version + " (Build: " + this.getBuildToken() + ")";
    }
}
