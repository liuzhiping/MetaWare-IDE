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
package com.arc.cdt.importer.internal.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.ICodewrightProjectSpace;
import com.arc.cdt.importer.core.PSPException;


/**
 * Implementation of a CodeWrite project space.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class CodewrightProjectSpace implements ICodewrightProjectSpace {
    private File mPspFile;
    private List<ICodewrightProject> mProjects = new ArrayList<ICodewrightProject>();
    
    CodewrightProjectSpace(File pspFile, File[] projectFiles, IProgressMonitor monitor) throws IOException,
            PSPException {
        mPspFile = pspFile;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Reading project space", projectFiles.length);
        try {
            for (File f : projectFiles) {
                mProjects.add(new CodewrightProject(this, f, new SubProgressMonitor(monitor, 1)));
            }
        }
        finally {
            monitor.done();
        }
    }
    public ICodewrightProject[] getProjects () throws IOException, PSPException {
        return mProjects.toArray(new ICodewrightProject[mProjects.size()]);
    }

    public File getPspFile () {
        return mPspFile;
    }
    
    public String getName(){
        File f = getPspFile();
        String name = f.getName();
        int i = name.indexOf('.');
        if (i > 0){
            name = name.substring(0,i);
        }
        return name;
    }
    
    public File getLocation(){
        return computeOriginalSourceDirectory(mProjects.toArray(new ICodewrightProject[mProjects.size()]));
    }
    
    /**
     * Given one or more projects from the same project space, see if we can determine the source directory. Typically,
     * it is the directory where the .psp file is, but not necessarily.
     * <P>
     * Return <code>null</code> if we cannot find a single directory.
     * @param projects the projects to look at.
     * @return the single source directory, or <code>null</code>.
     */
    private static File computeOriginalSourceDirectory (ICodewrightProject projects[]) {
        File result = null;
        for (ICodewrightProject p : projects) {
            File[] sources = p.getSourceFiles();
            for (File f : sources) {
                if (result == null) {
                    result = f.getParentFile();
                    if (isRoot(result)) result = null;
                }
                else {
                    File d = computeCommonBaseDir(result, f.getParentFile());
                    if (d != null && !isRoot(d)) {
                        result = d;
                    }
                }
            }
        }
        return result;
    }

    private static boolean isRoot (File d) {
        return new Path(d.getAbsolutePath()).isRoot();
    }

    private static File computeCommonBaseDir (File d1, File d2) {
        if (d1.equals(d2))
            return d1;
        Path p1 = new Path(d1.getAbsolutePath());
        Path p2 = new Path(d2.getAbsolutePath());
        if (p1.getDevice() != null && !p1.getDevice().equals(p2.getDevice()) ||
            p1.getDevice() == null && p2.getDevice() != null)
            return null;
        int matching = p1.matchingFirstSegments(p2);
        return p1.uptoSegment(matching).toFile();

    }


}
