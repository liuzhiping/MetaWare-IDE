/*
 * PackageUtil
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.cdt.testutil;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Utilities related to packages.
 * 
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class PackageUtil {

	/**
	 * Find all classes within the given package name.
	 * <P>
	 * <b>NOTE:</b> there is undoubtedly a better way to do this, but I am not
	 * aware of it. The Java API does not appear to have a way to get all
	 * classes within a package. So, we resort to using Eclipse's Bundle
	 * mechanism. If anyone knows of a cleaner way to do this, then by all means
	 * change it.
	 * 
	 * @param packageName
	 *            name of a package.
	 * @param transitively
	 *            if true, also extract from nested packages.
	 * @return the classes within a package.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static Class<?>[] getClasses(String packageName, boolean transitively)
			throws IOException {
		String path = "bin/" + packageName.replace('.', '/');
		Bundle bundle = Platform.getBundle("WindowTester");
		if (bundle == null)
			throw new IOException("Can't access package " + packageName
					+ " as a plugin. "
					+ " You need to run JUnit test to regenerate things.");
		Enumeration<URL> classPaths = bundle.findEntries(path, "*.class",
				transitively);

		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		if (classPaths != null) {
			while (classPaths.hasMoreElements()) {
				String s = classPaths.nextElement().getPath();
				assert (s.startsWith("/" + path));
				String fullClassName = packageName
						+ "."
						+ s.substring(path.length() + 2, s.length() - 6)
								.replace('/', '.');
				try {
					classes.add(bundle.loadClass(fullClassName));
				} catch (ClassNotFoundException e) {
					System.err.println("Could not load: " + fullClassName);
				}
			}
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Return the classes within the given package that implements a given
	 * interface.
	 * 
	 * @param packageName
	 *            name of package.
	 * @param implementS
	 *            the superclass of each returned class, or an interface that
	 *            each class is to implement.
	 * @param transitively
	 *            if true, descend into nested packages.
	 * @return list of classes.
	 * @throws IOException
	 */
	public static Class<?>[] getClasses(String packageName,
			Class<?> implementS, boolean transitively) throws IOException {
		Class<?>[] classes = getClasses(packageName, transitively);
		List<Class<?>> resultList = new ArrayList<Class<?>>(classes.length);
		for (Class<?> c : classes) {
			if (implementS.isAssignableFrom(c)
					&& !Modifier.isAbstract(c.getModifiers())) {
				resultList.add(c);
			}
		}
		return resultList.toArray(new Class<?>[resultList.size()]);
	}
}
