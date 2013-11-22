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
package com.arc.dwarf2.model;

import java.io.IOException;


/**
 * A factory object that is provided by the client. It is used by the Dwarf reader to access
 * section data by name.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ISectionAccess {
    /**
     * Return a callback interface by which the contents of a data section can
     * be read.
     * @param name the name of the section to be accessed (e.g., ".debug_info").
     * @return a callback interface to read the section, or <code>null</code> if there
     * is no section with the given name.
     * @throws IOException if some sort of corruption appears in the underlying source from
     * which section information is extracted.
     */
    public ISectionReader getSection(String name) throws IOException;
    
    /**
     * Return true if the section data is in little endian; otherwise, the
     * data is assumed to be big endian.
     * @return whether or not section data is stored in little-endian format.
     */
    public boolean isLittleEndian();

}
