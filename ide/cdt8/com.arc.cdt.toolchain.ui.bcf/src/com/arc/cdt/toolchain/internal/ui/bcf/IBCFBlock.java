package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.swt.widgets.Control;

/**
 * Interface to the UI block for specifying the ARChitect-generated configuration file
 * @author pickensd
 *
 */
interface IBCFBlock {
	interface IChangeListener{
		void onChange(IBCFBlock block);
	}
	public void updateConfig(IConfiguration config);
	public File getTcfLocation();
	public void setTcfLocation(String f);
	public Control getControl();
	public void addChangeListener(IChangeListener listener);
	public void removeChangeListener(IChangeListener listener);
	public String getErrorMessage();
}