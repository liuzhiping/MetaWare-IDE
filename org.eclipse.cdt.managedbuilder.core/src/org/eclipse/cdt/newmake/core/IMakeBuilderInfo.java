/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.newmake.core;

import org.eclipse.core.runtime.CoreException;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakeBuilderInfo extends IMakeCommonBuildInfo {

	public final static String BUILD_TARGET_INCREMENTAL = ARGS_PREFIX + ".build.target.inc"; //$NON-NLS-1$
	public final static String BUILD_TARGET_AUTO = ARGS_PREFIX + ".build.target.auto"; //$NON-NLS-1$
	public final static String BUILD_TARGET_CLEAN = ARGS_PREFIX + ".build.target.clean"; //$NON-NLS-1$

	/** @deprecated */
	@Deprecated
	public final static String BUILD_TARGET_INCREAMENTAL = BUILD_TARGET_INCREMENTAL;
	/** @deprecated */
	@Deprecated
	public final static String BUILD_TARGET_FULL = ARGS_PREFIX + ".build.target.full"; //$NON-NLS-1$
	
	boolean isAutoBuildEnable();
	void setAutoBuildEnable(boolean enabled) throws CoreException;

	String getAutoBuildTarget();

	/**
	 * @deprecated
	 */
	@Deprecated
	void setAutoBuildTarget(String target) throws CoreException;

	boolean isIncrementalBuildEnabled();
	void setIncrementalBuildEnable(boolean enabled) throws CoreException;

	String getIncrementalBuildTarget();

	/**
	 * @deprecated
	 */
	@Deprecated
	void setIncrementalBuildTarget(String target) throws CoreException;

	boolean isFullBuildEnabled();
	void setFullBuildEnable(boolean enabled) throws CoreException;

	String getFullBuildTarget();

	/**
	 * @deprecated
	 */
	@Deprecated
	void setFullBuildTarget(String target) throws CoreException;

	String getCleanBuildTarget();

	/**
	 * @deprecated
	 */
	@Deprecated
	void setCleanBuildTarget(String target) throws CoreException;

	boolean isCleanBuildEnabled();
	void setCleanBuildEnable(boolean enabled) throws CoreException;

}
