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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.engine.AssemblyRecord;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.Location;

/**
 * @author David Pickens
 */
class SourceManager extends Manager {

    private ArrayList<String> mSrcPaths = new ArrayList<String>();

    /**
     * @param target the associated target.
     */
    public SourceManager(Target target) {
        super(target, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#addSourcePaths(java.lang.String[])
     */
    public void addSourcePaths(String[] srcPaths) throws CDIException {
        mSrcPaths.addAll(Arrays.asList(srcPaths));
        String[] paths = mSrcPaths.toArray(new String[mSrcPaths
                .size()]);
        Target target = getTarget();
        EngineInterface engine = target.getEngineInterface();
        try {
            engine.setSourceDirectories(paths);
        } catch (EngineDisconnectedException x){
            // Debugger being shut down before initialization complete (?)
            // Don't complain.
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }
    
    public void setDirectoryTranslationPaths (String paths[]) throws CDIException {
        Target target = getTarget();
        EngineInterface engine = target.getEngineInterface();
        try {
            engine.setDirectoryTranslation(paths);
        }
        catch (EngineDisconnectedException x) {
            // Debugger being shut down before initialization complete (?)
            // Don't complain.
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getSourcePaths()
     */
    public String[] getSourcePaths() {
        return  mSrcPaths.toArray(new String[mSrcPaths.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(long,
     *      long)
     */
    public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress)
            throws CDIException {
        try {
            long start = startAddress.longValue();
            long end = endAddress.longValue();
            Target target = getTarget();
            EngineInterface engine = target.getEngineInterface();
            // get at least one instruction.
            if (end == start) end++;
            // Compute approximate number of instruction.
            int instrCount = (int)(end-start)/2;
            AssemblyRecord record[] = engine.disassemble(start,instrCount);
            if (record == null || record.length == 0)
                throw new CDIException("Unable to disassemble from 0x" + Long.toHexString(start));

            ArrayList<ICDIInstruction> list = new ArrayList<ICDIInstruction>(record.length);
            for (int i = 0; i < record.length && record[i].getAddress() < end; i++){
                if (record[i].getAddress() < end){
                    Location loc = engine.computeLocation(record[i].getAddress());
                    list.add(new AssemblyInstruction(target,loc,record[i]));            
                }
            }
            return  list.toArray(new ICDIInstruction[list.size()]);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(java.lang.String,
     *      int)
     */
    public ICDIInstruction[] getInstructions(String filename, int linenum)
            throws CDIException {
        try {
            Target target = getTarget();
            EngineInterface engine = target.getEngineInterface();
            Location loc1 = engine.lookupSource(filename,linenum);
            if (loc1 == null) throw new CDIException("Can't find line " + linenum + " of " + filename);
            Location loc2 = engine.lookupSource(filename,linenum+1);
            int i = 1;
            // Look for next executable line. 
            while (loc2 != null && loc1.getAddress() == loc2.getAddress() && i < 10){
                i++;
                loc2 = engine.lookupSource(filename,linenum+i);
            }
            if (loc2 == null) // should not happenA
                throw new CDIException("can't find end of line " + linenum + " of " + filename);
            return getInstructions(BigInteger.valueOf(loc1.getAddress()),BigInteger.valueOf(loc2.getAddress()));
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(java.lang.String,
     *      int, int)
     */
    public ICDIInstruction[] getInstructions(String filename, int linenum,
            int lines) throws CDIException {
        try {
            Target target = getTarget();
            EngineInterface engine = target.getEngineInterface();
            Location loc1 = engine.lookupSource(filename,linenum);
            if (loc1 == null) throw new CDIException("Can't find line " + linenum + " of " + filename);
            long highAddress = loc1.getAddress() + lines*4; // approximate guess
            return getInstructions(BigInteger.valueOf(loc1.getAddress()),BigInteger.valueOf(highAddress));
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(long,
     *      long)
     */
    public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress,
            BigInteger endAddress) throws CDIException {
        throw new CDIException("Not yet implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(java.lang.String,
     *      int)
     */
    public ICDIMixedInstruction[] getMixedInstructions(String filename,
            int linenum) throws CDIException {
        ICDIInstruction[] instr = getInstructions(filename,linenum);
        ICDIMixedInstruction i = new MixedAssemblyInstructions(getTarget(),filename,linenum,instr);
        return new ICDIMixedInstruction[]{i};
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(java.lang.String,
     *      int, int)
     */
    public ICDIMixedInstruction[] getMixedInstructions(String filename,
            int linenum, int lines) throws CDIException {
        ICDIInstruction[] instr = getInstructions(filename,linenum,lines);
        ArrayList<ICDIMixedInstruction> list = new ArrayList<ICDIMixedInstruction>();
        int lastIndex = 0;
        int lastLine = linenum;
        for (int i = 0; i < instr.length;i++){
            Location loc = ((AssemblyInstruction)instr[i]).getLocation();
            if (loc.getSourceLine() != lastLine || i+1==instr.length){
                ICDIInstruction cut[] = new ICDIInstruction[i-lastIndex];
                System.arraycopy(instr,lastIndex,cut,0,i-lastIndex);
                list.add(new MixedAssemblyInstructions(getTarget(),filename,lastLine,cut));
                lastLine = loc.getSourceLine();
                lastIndex = i;
            }           
        }   
        return  list.toArray(new ICDIMixedInstruction[list.size()]);
    }
}
