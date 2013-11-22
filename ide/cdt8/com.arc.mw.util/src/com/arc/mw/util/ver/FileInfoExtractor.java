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
package com.arc.mw.util.ver;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Extracts the version information from an exectutable or DLL.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class FileInfoExtractor {

    private static final byte[] PATTERN = "MetaWareeraWateM".getBytes();

    private File exe;

    private VersionInfo verInfo;

    private byte[] content;

    /**
     * @param exe the path to the executable or DLL whose version and/or licensing information is required.
     */
    public FileInfoExtractor(File exe) {
        this.exe = exe;
        this.verInfo = null;
        this.content = null;
    }

    public VersionInfo extractVersionInfo () throws IOException {

        if (verInfo == null) {
            byte[] buf = getContent();
            verInfo = extractVersionInfo(buf);
        }
        return verInfo;
    }

    private byte[] getContent () throws IOException {
        if (content == null) {
            FileInputStream fd = new FileInputStream(exe);
            MappedByteBuffer map;
            try {
                map = fd.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, exe.length());
            } finally {
                fd.close();
            }
            map.load();
            if (map.hasArray()) {
                content = map.array();
            }
            else {
                content = new byte[map.limit()];
                map.get(content);
            }
        }
        return content;
    }

    private VersionInfo extractVersionInfo (byte[] buffer) {
        VersionInfo info = new VersionInfo();
        boolean swap = false;
        int index = -1;
        index = findString(buffer, PATTERN);

        if (index == -1) {
            return null;
        }
        index += PATTERN.length;

        int s = ((buffer[index] & 0xFF) << 8 | buffer[index + 1] & 0xFF);
        index += 2;

        if (s > 255)
            swap = true;

        s = (!swap) ? (buffer[index] << 8 | buffer[index + 1] & 0xFF) : (buffer[index + 1] << 8 | buffer[index] & 0xFF);
        info.setMajorVersion(s);
        index += 2;

        s = (!swap) ? (short) (buffer[index] << 8 | buffer[index + 1] & 0xFF)
            : (short) (buffer[index + 1] << 8 | buffer[index] & 0xFF);
        info.setMinorVersion(s);
        index += 2;

        s = (!swap) ? (short) (buffer[index] << 8 | buffer[index + 1] & 0xFF)
            : (short) (buffer[index + 1] << 8 | buffer[index] & 0xFF);
        info.setPatchVersion(s);
        index += 2;

        s = (!swap) ? (short) (buffer[index] << 8 | buffer[index + 1] & 0xFF)
            : (short) (buffer[index + 1] << 8 | buffer[index] & 0xFF);
        info.setStartYear(s);
        index += 2;

        s = (!swap) ? (short) (buffer[index] << 8 | buffer[index + 1] & 0xFF)
            : (short) (buffer[index + 1] << 8 | buffer[index] & 0xFF);
        info.setCurrentYear(s);
        index += 2;

        String str1 = extractString(buffer, index, 128);
        info.setPublicVersion(str1.trim());
        index += 128;

        str1 = extractString(buffer, index, 128);
        info.setCSGVersion(str1.trim());
        index += 128;

        str1 = extractString(buffer, index, 32);
        info.setBuildToken(str1.trim());
        index += 32;

        str1 = extractString(buffer, index, 16);
        info.setProcessorName(str1.trim());
        return info;
    }

    private static String extractString (byte[] buffer, int index, int len) {
        int i = 0;
        for (; i < len; i++) {
            if (buffer[index + i] == 0)
                break;
        }
        return new String(buffer, index, i);
    }

    /**
     * Search buffer for occurrance of "pattern" and return its index, or -1 if not found.
     * @param buffer the buffer to search.
     * @param pattern the substring to search for.
     * @return the index of the pattern within the buffer, or -1.
     */
    private int findString (byte[] buffer, byte[] pattern) {
        int index = -1;
        for (int i = 0; i < buffer.length - 20; i++) {
            if (buffer[i] == pattern[0] && buffer[i + 1] == pattern[1] && buffer[i + 2] == pattern[2]) {
                boolean found = true;
                for (int j = 3; j < pattern.length; j++) {
                    if (buffer[i + j] != pattern[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
}
