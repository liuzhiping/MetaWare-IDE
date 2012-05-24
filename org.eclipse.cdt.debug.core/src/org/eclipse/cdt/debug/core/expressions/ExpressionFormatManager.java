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
package org.eclipse.cdt.debug.core.expressions;

import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;

public class ExpressionFormatManager {
	private static ExpressionFormatManager fManager = null;

	private static final String EXPRESSION_FORMATS_PREF_KEY = "ExpressionFormats";

	private Map<IWatchExpression, CVariableFormat> fFormatMap = null;
	
	private IExpression fJustAdded[] = null;

	private ExpressionFormatManager() {
	}

	public static ExpressionFormatManager getInstance() {
		if (fManager == null) {
			fManager = new ExpressionFormatManager();
		}
		return fManager;
	}

	/**
	 * Return the format to be applied to the given expression.
	 * 
	 * @param e
	 *            an expression in the expression view.
	 * @return the associated format.
	 */
	public CVariableFormat getFormat(IWatchExpression e) {
		Map<IWatchExpression, CVariableFormat> map = getFormatMap();
		CVariableFormat format = map.get(e);
		if (format == null)
			format = CVariableFormat.NATURAL;
		return format;
	}

	/**
	 * Associate a format to a list of expression.
	 * 
	 * @param exprs
	 *            the expression list.
	 * @param f
	 *            the format to be associated with the expression.
	 */
	public void setFormat(IWatchExpression exprs[], CVariableFormat f) {
		Map<IWatchExpression, CVariableFormat> map = getFormatMap();
		for (IWatchExpression e : exprs) {
			if (f != CVariableFormat.NATURAL) {
				map.put(e, f);
			} else
				map.remove(e);
		}
		writeSettings(map);

		for (IWatchExpression e : exprs) {
			e.evaluate();
		}
	}

	private static IExpression[] getAllExpressions() {
		IExpressionManager emgr = DebugPlugin.getDefault()
				.getExpressionManager();
		// Must get all expressions regardless of context, because if the
		// debugger isn't active,
		// there are no contexts and it defaults to org.eclipse.debug.core.
		return emgr.getExpressions(/* CDIDebugModel.getPluginIdentifier() */);
	}

	private Map<IWatchExpression, CVariableFormat> getFormatMap() {
		if (fFormatMap == null) {
			fFormatMap = new HashMap<IWatchExpression, CVariableFormat>();
			readSettings(fFormatMap);
			DebugPlugin.getDefault().getExpressionManager()
					.addExpressionListener(new IExpressionsListener() {

						@Override
						public void expressionsAdded(IExpression[] expressions) {
							fJustAdded = expressions;
						}

						@Override
						public void expressionsChanged(IExpression[] expressions) {
						}

						@Override
						public void expressionsRemoved(IExpression[] expressions) {
							for (IExpression e : expressions) {
								fFormatMap.remove(e.getExpressionText());
							}
							writeSettings(fFormatMap);

						}
					});
		}
		return fFormatMap;
	}

	/**
	 * Called when "CExpression" is created that corresponds to a IWatchExpression. We are not
	 * otherwise able to get the IWatchExpression (and thus the Format) unless we resort to this hack.
	 * @param text
	 * @return corresponding IWatchExpression for text.
	 */
	public IWatchExpression findWatchExpression(String text){
		if (fJustAdded != null){
			for (IExpression e: fJustAdded){
				if (text.equals(e.getExpressionText()) && e instanceof IWatchExpression){
					return (IWatchExpression)e;
				}
			}
		}
		// If there is the initial instantiation of a IWatchExpression, its value will be null.
		for (IExpression e: getAllExpressions()){
			if (text.equals(e.getExpressionText()) && e instanceof IWatchExpression && e.getValue() == null){
				return (IWatchExpression)e;
			}
		}
		
		// Punt: just choose the first with the same text. Rarely do users have more than one expression
		// that is identical to each other.
		for (IExpression e: getAllExpressions()){
			if (text.equals(e.getExpressionText()) && e instanceof IWatchExpression){
				return (IWatchExpression)e;
			}
		}
		return null;
		
	}
	/**
	 * Read expression format map from preference store.
	 * 
	 * @param map
	 *            map to be filled in.
	 */
	private static void readSettings(Map<IWatchExpression, CVariableFormat> map) {
		Preferences prefs = CDebugCorePlugin.getDefault()
				.getPluginPreferences();
		String encoding = prefs.getString(EXPRESSION_FORMATS_PREF_KEY);
		IExpression exprs[] = getAllExpressions();
		if (encoding != null) {
			String entries[] = encoding.split("\\n");
			for (String s : entries) {
				String keyValue[] = s.split(SEP_ESCAPED);
				if (keyValue.length == 3) {
					CVariableFormat f = CVariableFormat.NATURAL;
					switch (keyValue[2].charAt(0)) {
					case 'H':
						f = CVariableFormat.HEXADECIMAL;
						break;
					case 'D':
						f = CVariableFormat.DECIMAL;
						break;
					case 'O':
						f = CVariableFormat.OCTAL;
						break;
					case 'B':
						f = CVariableFormat.BINARY;
						break;
					case 'N':
						f = CVariableFormat.NATURAL;
						break;
					}
					int which = Integer.parseInt(keyValue[1]);
					int cnt = 1;
					for (IExpression e : exprs) {
						if (e instanceof IWatchExpression
								&& keyValue[0].equals(e.getExpressionText())
								&& cnt++ == which) {
							map.put((IWatchExpression) e, f);
							break;
						}
					}
				}
			}
		}
	}

	private static final String SEP = "??";
	private static final String SEP_ESCAPED = "\\?\\?";

	/**
	 * Write expression formats to preference store.
	 * 
	 * @param map
	 *            map of expression formats.
	 */
	private static void writeSettings(Map<IWatchExpression, CVariableFormat> map) {
		Preferences prefs = CDebugCorePlugin.getDefault()
				.getPluginPreferences();
		if (map.size() == 0) {
			prefs.setToDefault(EXPRESSION_FORMATS_PREF_KEY);
		} else {
			CharArrayWriter out = new CharArrayWriter();
			Map<String, Integer> matching = new HashMap<String, Integer>();
			for (IExpression e : getAllExpressions()) {
				CVariableFormat f = map.get(e);
				if (f == null) {
					Integer which = matching.get(e.getExpressionText());
					if (which != null){
						f = CVariableFormat.NATURAL;
					}
					else
						matching.put(e.getExpressionText(),new Integer(1));
				}
				if (f != null) {
					Integer which = matching.get(e.getExpressionText());
					out.append(e.getExpressionText());
					out.append(SEP);
					if (which == null) {
						out.append("1");
						which = new Integer(2);
					} else {
						which = new Integer(which.intValue() + 1);
						out.append(which.toString());						
					}
					matching.put(e.getExpressionText(), which);
					out.append(SEP);
					if (f == CVariableFormat.HEXADECIMAL) {
						out.append('H');
					} else if (f == CVariableFormat.DECIMAL) {
						out.append('D');
					} else if (f == CVariableFormat.OCTAL) {
						out.append('O');
					} else if (f == CVariableFormat.BINARY) {
						out.append('B');
					} else
						// Shouldn't get here
						out.append('N');
					out.append('\n');
				}
			}
			prefs.setValue(EXPRESSION_FORMATS_PREF_KEY, out.toString());
		}
	}
}
