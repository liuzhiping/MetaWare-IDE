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
package com.arc.cdt.importer.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.arc.cdt.importer.core.PSPException;


/**
 * Reads the contents of a CodeWright ".elx" file.
 * The format is a series of ASCII lines that represent <i>section attribute</i>
 * values.
 * <P>
 * The specifications occur in line pairs. The first of the pair identifies
 * the attribute and the second is the attribute value.
 * <P>
 * The first line consists of an integer number which 
 * is the number of attributes specified. Since each specification
 * is two lines, this number will be:
 * <pre>
 * (total-1)/2
 * </pre>
 * where <code>total</code> is the total number of lines in the file, which
 * is always odd.
 * <P>
 * Starting with the second line is the attribute specifications.
 * The attribute line (the first of each pair) consists of two integer
 * numbers separated by white space. The first number identifies the 
 * <i>section</i> and the second identifies the <i>attribute</i> in the
 * section. The value line is the value of the attribute.
 * <P>
 * Example:
 * <pre>
 * 143
 * 9 2
 * Executable
 * 9 9
 * hcarm.exe
 * 9 5
 * 0
 * 9 8
 * -Heos=mqx -Bstatic -Bgrouplib -Hnocplus -Hnocopyr -Hthumb -Hinter -HL -Hhostlink
 * 9 6
 * C:\ARM\mqx2.50_mwsim720t.met\lib\ep7312l.met\mqx.a;C:\ARM\mqx2.50_mwsim720t.met\lib\mwsim720tl.met\mwsim720t.a
 * 9 7
 * C:\ARM\mqx2.50_mwsim720t.met\lib\mwsim720tl.met\met.o
 * 9 4
 * demo.elf
 * ...
 * 4 3
 * C:\ARM\MQX2.50_mwsim720t.met\examples\demo\demo.dsn
 * 4 1
 * 1.01
 * 7 8
 * -Hnocopyr
 * 7 7
 * -O5
 * 7 10
 * 
 * 7 9
 * 
 * 7 14
 * 
 * 7 15
 * -Hon=Long_enums -Hoff=Behaved
 * 7 11
 * BSP_DEFAULT_HOSTLINK=1,MQX_CRIPPLED_EVALUATION=1,MQX_CPU=907,__MET__
 * ...
 * </pre>
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class ElxFileReader {
    private Map<Integer,Map<Integer,String>> mAttributes = new HashMap<Integer,Map<Integer,String>>();
    ElxFileReader(File elxFile) throws PSPException, IOException{
        readIt(elxFile);
    }
    
//    /**
//     * Return the attribute map for a section.
//     * @param sectionID the section ID.
//     * @return the attribute map for a section.
//     */
//    public Map<Integer,String> getSection(int sectionID){
//        Map<Integer,String> map = mAttributes.get(new Integer(sectionID));
//        if (map == null) map = new HashMap<Integer,String>();
//        return Collections.unmodifiableMap(map);       
//    }
    
    /**
     * Return the value of an attribute within a section.
     * @param sectionID the section id.
     * @param attributeID the attribute id.
     * @return the value of the attribute or <code>null</code>.
     */
    public String getAttribute(int sectionID, int attributeID){
        Map<Integer,String>map = mAttributes.get(new Integer(sectionID));
        if (map != null){
            return map.get(new Integer(attributeID));
        }
        return null;
    }
    
    private void readIt (File f) throws IOException, PSPException {
        FileReader reader = new FileReader(f);
        BufferedReader input = new BufferedReader(reader);
        //
        // The first line is a lone integer that is the count of
        // attribute sections.
        //
        String line = input.readLine();
        try {
            int attributeCount;
            try {
                attributeCount = Integer.parseInt(line);
            }
            catch (NumberFormatException e) {
                throw new PSPException("First line of " + f + " is not a valid attribute count: " + line, e);
            }
            //
            // We now have a sequence of 2-line pairs. The first consists of
            // to integers separated by white space. The first integer is
            // a section ID and the second is an attribute ID.
            //
            for (int i = 0; i < attributeCount; i++) {
                line = input.readLine();
                try {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    int sectionID = Integer.parseInt(tokenizer.nextToken());
                    int attributeID = Integer.parseInt(tokenizer.nextToken());
                    String value = input.readLine();
                    Map<Integer, String> map = mAttributes.get(new Integer(sectionID));
                    if (map == null) {
                        map = new HashMap<Integer, String>();
                        mAttributes.put(new Integer(sectionID), map);
                    }
                    map.put(new Integer(attributeID), value);
                }
                catch (NumberFormatException e1) {
                    throw new PSPException("At line "
                            + (i * 2 + 2)
                            + " in "
                            + f
                            + ":\n"
                            + "bad section or attribute ID: "
                            + line, e1);
                }
                catch (NullPointerException e1) {
                    throw new PSPException("Unexpected end of file at line " + (i * 2 + 2) + " in " + f);
                }
                catch (NoSuchElementException e){
                    throw new PSPException("At line "
                            + (i * 2 + 2)
                            + " in "
                            + f
                            + ":\n"
                            + "bad section or attribute ID: "
                            + line, e);
                }
            }
        }
        finally {
            input.close();
        }
    }

}
