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
package com.arc.mw.util.elf;

/**
 * Exception that is thrown when detecting something unexpected when
 * reading an ELF file.
 * @author David Pickens
 */
public class ElfFormatException extends Exception {

    public ElfFormatException() {
        super();

    }

    public ElfFormatException(String message) {
        super(message);

    }

    public ElfFormatException(String message, Throwable cause) {
        super(message, cause);

    }

    public ElfFormatException(Throwable cause) {
        super(cause);

    }

}
