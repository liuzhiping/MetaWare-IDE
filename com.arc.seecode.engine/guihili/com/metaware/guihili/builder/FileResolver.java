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
package com.metaware.guihili.builder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.InputSource;

import com.metaware.guihili.IFileResolver;

/**
 * A file resolver, based on a sequence of directories to search
 * and multiple suffixes to try.
 * @author David Pickens, May 2002
 */
public class FileResolver implements IFileResolver {
	public FileResolver() {
	}
	public FileResolver(String directory) {
		this(new String[] { directory }, new String[0]);
	}
	public FileResolver(String directory, String suffix) {
		this(new String[] { directory }, new String[] { suffix });
	}
	/**
	 * @param directories the directories to search, in order.
	 * @param suffix a suffix to apply if one isn't already there.
	 */
	public FileResolver(String[] directories, String suffix) {
		this(directories, new String[] { suffix });
	}

	/**
	 * @param directories the directories to search, in order.
	 * @param suffices suffices to append, in order.
	 */
	public FileResolver(String[] directories, String[] suffices) {
		mDirs = directories;
		mSuffices = new String[suffices.length];
		for (int i = 0; i < suffices.length; i++) {
			String suffix = suffices[i];
			if (suffix.charAt(0) != '.')
				mSuffices[i] = "." + suffix;
			else
				mSuffices[i] = suffix;
		}
	}

	static boolean isSimpleName(File f) {
		if (f.isAbsolute())
			return false;
		if (f.getParent() == null || f.getParent().equals(""))
			return true;
		return false;
	}

	@Override
    public InputSource openFile(String name) {
		File f = new File(name);
		if (f.isAbsolute() && f.getName().indexOf('.') < 0) {
			for (int i = 0; i < mSuffices.length; i++) {
				f = new File(name + mSuffices[i]);
				if (f.exists())
					break;
			}
		}
		if (isSimpleName(f) && mDirs != null) {
			for (int i = 0; i < mDirs.length; i++) {
				f = new File(mDirs[i], name);
				if (f.exists())
					break;
				if (name.indexOf('.') < 0) {
					for (int j = 0; j < mSuffices.length; j++) {
						f = new File(mDirs[i], name + mSuffices[j]);
						if (f.exists())
							break;
					}
					if (f.exists())
						break;
				}
			}
		}
		try {
			InputSource s = new InputSource(new FileReader(f));
			s.setSystemId(f.getPath());
			return s;
		} catch (IOException x) {
			//System.out.println("Can't open " + f + ": " + x);
			return null;
		}
	}
	private String mDirs[];
	private String mSuffices[];
}
