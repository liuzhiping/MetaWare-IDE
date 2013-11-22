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
package com.metaware.guihili;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.arc.mw.util.IPropertyMap;
import com.metaware.guihili.eval.Evaluator;
/**
 * Methods for reading and writing properties to a file. We use the format of
 * <code>java.util.Properties</code>, but encode lists as "{ x y z }"
 * 
 * @author David Pickens, May 15,2002
 */
public class PropertyStorage {
	/**
	 * Write contents of a properties table into a file for later retrieval. We
	 * use the <code>java.util.Properties</code> encoding.
	 * 
	 * @param keys
	 *            the set of property names
	 * @param map
	 *            to get the value for each property name.
	 * @param file
	 *            the file into which we are storing the property values
	 * @param banner
	 *            a banner to appear at the head of the list
	 * @exception Error
	 *                occurred in writing.
	 */
	public static void write(Collection<String> keys, IPropertyMap map, File file,
			String banner) throws IOException {
		Properties p = encode(keys, map);
		OutputStream out = new FileOutputStream(file);
		p.store(out, banner);
		out.close();
	}
	/**
	 * Convert our abstract property map into a plain Java Properties object.
	 * This means that all values must be encoded as Strings.
	 * 
	 * @param keys
	 *            the set of key names.
	 * @param map
	 *            the map.
	 * @return Properties object.
	 */
	public static Properties encode(Collection<String> keys, IPropertyMap map) {
		// We translate our property map into Java Properties, and then have it
		// written the canonical way.
		Properties p = new Properties();
        for (String key: keys){
			Object value = map.getProperty(key);
			if (value != null) {
				try {
					p.setProperty(key, encode(value));
				} catch (IllegalArgumentException x) {
					System.err.println("Couldn't encode property " + key + ": "
							+ x.getMessage());
					System.err.println("value is " + value);
				}
			}
		}
		return p;
	}
	/**
	 * Read properties for a file
	 * 
	 * @param map
	 *            the callback to set the property values as they are read in.
	 * @param file
	 *            the file to read from.
	 * @throws IOException
	 *             couldn't open, or couldn't read file.
	 */
	public static void read(IPropertyMap map, File file) throws IOException {
		InputStream in = new FileInputStream(file);
		Properties p = new Properties();
		p.load(in);
		decode(map, p);
	}
	/**
	 * Convert a Properties object into our own {@link IPropertyMap}object.
	 * Values are decoded back into their original representation.
	 * 
	 * @param map
	 * @param p
	 */
	public static void decode(IPropertyMap map, Properties p) {
		// We use Evaluator only to parse lists
		// Need to refactor the list parsing code!
		IEvaluator e = new Evaluator(null);
		Iterator<Object> names = p.keySet().iterator();
		while (names.hasNext()) {
			String name = (String) names.next();
			String value = p.getProperty(name);
			try {
				map.setProperty(name, decode(value, e));
			} catch (PropertyVetoException x) {
			}
		}
	}
	private static Object decode(String value, IEvaluator eval) {
		int slen = value.length();
		if (slen > 0 && value.charAt(0) == '{') {
			StringBuffer buf = new StringBuffer(value.length() + 10);
			buf.append("$( ");
			for (int i = 1; i < slen; i++) {
				char c = value.charAt(i);
				if (c == '{')
					buf.append('(');
				else if (c == '}')
					buf.append(')');
				else
					buf.append(c);
			}
			try {
				return eval.parseList(buf.toString());
			} catch (MalformedExpressionException x) {
				throw new IllegalArgumentException("Corrupt list");
			}
		}
		return value;
	}
	@SuppressWarnings("unchecked")
    private static String encode(Object value) {
		if (value instanceof String || value instanceof Integer
				|| value instanceof Boolean) {
			return value.toString();
		}
		if (value instanceof List) {
			return listEncode((List<Object>) value);
		} else if (value instanceof Object[]) {
			return listEncode(Arrays.asList((Object[]) value));
		}
		else if (value.toString().indexOf("@") < 0){
		    return value.toString();
		}
		throw new IllegalArgumentException("Value is not encodable as string");
	}
	private static String listEncode(List<Object> list) {
		StringBuffer buf = new StringBuffer(100);
		int cnt = list.size();
		buf.append('{');
		for (int i = 0; i < cnt; i++) {
			String s = encode(list.get(i));
			buf.append('"');
			if (s.indexOf('"') >= 0 || s.indexOf('\\') >= 0) {
				int slen = s.length();
				for (int j = 0; j < slen; j++) {
					if (s.charAt(j) == '"' || s.charAt(j) == '\\')
						buf.append('\\');
					buf.append(s.charAt(j));
				}
			} else
				buf.append(s);
			buf.append("\" ");
		}
		buf.append('}');
		return buf.toString();
	}
}
