package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;

/**
 * An object that returns the location of CAT information.
 * The wizard page that selects the CAT location implements this interface.
 * @author pickensd
 *
 */
public interface IBCFLocation {
	/**
	 * A file for now
	 * @return
	 */
	File getBcfLocation();

}
