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
package com.arc.seecode.internal.command;

import java.io.PrintWriter;
import java.util.HashMap;

import com.arc.mw.util.StringUtil;
import com.arc.seecode.command.IErrorHandler;
import com.arc.seecode.engine.Breakpoint;
import com.arc.seecode.engine.BreakpointManager;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.LocationBreakpoint;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.Watchpoint;
import com.arc.seecode.engine.WatchpointHit;
import com.arc.seecode.engine.BreakpointManager.IObserver;


/**
 * Methods called by command parser to service the commands.
 * @author David Pickens
 */
class CommandService {
    private static final int MAX_WATCHPOINT_VALUE_DISPLAY_SIZE = 64;
    private EngineInterface mEngine;
    private PrintWriter mOut;
    private IErrorHandler mErr;
    private BreakpointManager mBreakpointManager;
    private StackFrameRef mStackFrame;
    private IObserver mBreakpointObserver = null;
    private HashMap<Breakpoint, String> mBreakpointExecMap = null;
    private ICommandInvoker mCommandInvoker = null;
    //private String mProgram;
    CommandService(EngineInterface engine, PrintWriter out, IErrorHandler ehandler) throws EngineException{
        mEngine = engine;
        mOut = out;
        mErr = ehandler;
        mBreakpointManager = engine.getBreakpointManager();
    }
    
    /**
     * Called to register the callback that invoke commands associated with
     * a breakpoint when it is hit.
     * @param invoker callback to invoke associated command.
     */
    void setCommandInvoker(ICommandInvoker invoker){
        mCommandInvoker = invoker;
    }
    
    
    
    /**
     * Display breakpionts
     *
     */
    public void displayBreakpoints () {
        Breakpoint bp[] = mBreakpointManager.getBreakpoints();
        boolean found = false;
        for (int i = 0; i < bp.length; i++) {
            Breakpoint b = bp[i];
            if (b instanceof LocationBreakpoint) {
                if (!found){
                    mOut.println("Breakpoints:");
                    found = true;
                }
                mOut.printf("   %2d",b.getBreakID());
                mOut.print(": ");
                if (!b.isEnabled()){
                    mOut.print(" - ");
                }
                else if (b.isHardware()){
                    mOut.print(" H ");
                }
                else {
                    mOut.print("   ");
                }
                mOut.print(b.toDisplayString());
                printExec(b);
                mOut.println();
            }
        }
        if (!found)
            mOut.println("No breakpoints");
    }
    
    private void printExec(Breakpoint b){
        if (mBreakpointExecMap != null){
            String cmd = mBreakpointExecMap.get(b);
            if (cmd != null){
                mOut.print(", exec " + cmd);
            }
        }
    }
    
    /**
     * Delete all location breakpoints.
     * @throws EngineException 
     */
    public void deleteAllBreakpoints() throws EngineException{
        deleteAllBreakpoints(LocationBreakpoint.class);
    }
    
    /**
     * Delete all location breakpoints.
     * @throws EngineException 
     */
    public void deleteAllWatchpoints() throws EngineException{
        deleteAllBreakpoints(Watchpoint.class);
    }
    
    private void deleteAllBreakpoints(Class<?> whichClass) throws EngineException{
        Breakpoint bp[] = mBreakpointManager.getBreakpoints();
        for (Breakpoint b: bp){
            if (whichClass.isInstance(b)){
                mBreakpointManager.remove(b);
            }
        }
    }
    
    /**
     * Enable or disable all location breakpoints.
     * @param enable if true, enable them; otherewise, disable them.
     * @throws EngineException 
     */
    public void setEnableAllBreakpoints(boolean enable) throws EngineException{
        setEnableAllBreakpoints(LocationBreakpoint.class,enable);
    }
    
    /**
     * Enable or disable all watchpoints.
     * @param enable if true, enable them; otherewise, disable them.
     * @throws EngineException 
     */
    public void setEnableAllWatchpoints(boolean enable) throws EngineException{
        setEnableAllBreakpoints(Watchpoint.class,enable);
    }
    
    private void setEnableAllBreakpoints(Class<?> whichClass, boolean enable) throws EngineException{
        Breakpoint bp[] = mBreakpointManager.getBreakpoints();
        for (Breakpoint b: bp){
            if (whichClass.isInstance(b)){
                b.setEnabled(enable);
            }
        }
    }
    
    /**
     * Display breakpionts
     *
     */
    public void displayWatchpoints(){
        Breakpoint bp[] = mBreakpointManager.getBreakpoints();
        boolean found = false;
        for (int i = 0; i < bp.length; i++) {
            Breakpoint b = bp[i];
            if (b instanceof Watchpoint) {
                if (!found){
                    mOut.println("Watchpoints:");
                    found = true;
                }
                Watchpoint w = (Watchpoint)b;
                WatchpointHit hit = w.getPendingHit();
                if (hit != null) {
                    mOut.print("  C  ");
                }
                else
                    mOut.print("     ");
                mOut.print(b.getBreakID());
                
                mOut.print(": " );
                if (!w.isEnabled()) { mOut.print("- ");}
                else if (w.isHardware()) {
                    mOut.print("H ");
                }
                else mOut.print("  ");
                mOut.print(w);
                printExec(w);
                if (w.isWrite()) 
                    if (w.isRead()) mOut.print(" data-read/write");
                    else mOut.print(" data-write");
                else if (w.isRead())
                    mOut.print(" data-read");
                if (w.getIngoreCount() > 0){}
                mOut.println();
                if (hit != null) mOut.print("  C");
                else mOut.print("   ");
                try {
                    if (mStackFrame != null){
                        String v = w.getVariable();
                        if (isDirectAddress(v)){
                            Location location = mStackFrame.evaluateLocation(v);
                            int len = w.getLength();
                            if (len == 0) len = 1;
                            String elipsis = "";
                            if (len >= MAX_WATCHPOINT_VALUE_DISPLAY_SIZE){
                                elipsis = "...";
                                len = MAX_WATCHPOINT_VALUE_DISPLAY_SIZE-3;
                            }
                            mOut.print("       value:");
                            byte[] value = mEngine.getMemoryBytes(location.getAddress(), len, 0);
                            if (hit != null) {
                                mOut.print(hit.getOldValue());
                                mOut.print(" => ");
                            }                           
                            printBytes(value);
                            mOut.println(elipsis);
                        }
                        else {
                            Value value = mStackFrame.evaluate(v);
                            mOut.print("       value: ");
                            if (hit != null) {
                                mOut.print(hit.getOldValue());
                                mOut.print(" => ");
                            }
                            mOut.println(value);
                        }
                    }
                }
                catch (EngineException e) {
                    
                }
                catch (EvaluationException e) {
                    mOut.println("       value: <" + e + ">");
                }
            }
        }
        if (!found)
            mOut.println("No watchpoints");
    }

    private void printBytes (byte[] value) {
        for (byte bytE: value){
            mOut.printf(" %02x",bytE);
        }
    }
    
    private static boolean isDirectAddress(String v){
        if (v.length() > 0 && Character.isDigit(v.charAt(0)))
            return true;
        return false;
    }
    
    public static final int NO_THREAD = -1;
    
    /**
     * Add a breakpoint
     * @param location
     * @param hitCount
     * @param condition
     * @param exec a command to be executed when breakpoint is hit.
     * @param tid the associated thread ID, or @{link #NO_THREAD}.
     * @param flags flags as defined in {@link IEngineAPI}.
     * @throws EngineException
     * @throws EvaluationException
     */
    public void addBreakpoint(String location,int hitCount,String condition,
            String exec, int tid, int flags) throws EngineException, EvaluationException{
        Location loc = createLocation(location);
        
        if (tid != NO_THREAD){
            int threads[] = mEngine.getThreads();
            boolean found = false;
            if (threads != null){
                for (int t: threads){
                    if (t == tid){
                        found = true;
                        break;
                    }
                }
            }
            if (!found){
                mErr.error("Thread ID " + tid + " is not recognized");
                return;
            }
        }
        
        LocationBreakpoint bp = loc==null?null:mBreakpointManager.create(loc,hitCount,condition,tid==NO_THREAD?0:tid,flags,true);
        if (bp != null) {
            if (exec != null){
                associateCommandWithBreakpoint(bp,exec);
            }
             mOut.println("Added " + bp);
        }
        else
            mOut.println("Breakpoint NOT added");
    }

	public Location createLocation(String location) throws EngineException, EvaluationException {
		int i = location.indexOf('!');
        Location loc = null;
        if (i >= 0){
            String srcFile=null;
            if (i > 0) srcFile = location.substring(0,i);
            else {
                if (mStackFrame != null){
                    Location srcLoc = mEngine.computeLocation(mStackFrame.getPC());
                    srcFile = srcLoc.getSource();
                }
            }
            if (srcFile == null || srcFile.length() == 0){
                throw new EvaluationException("Can't infer source file for \"" + location + "\" specification");
            }
            String lineString = location.substring(i+1);
            try {
                int line = Integer.parseInt(lineString);
                loc = mEngine.lookupSource(srcFile,line);
            }catch (NumberFormatException x){
                throw new EvaluationException("Invalid line designator in " + location);
            }
        }
        else {
            StackFrameRef sf = mStackFrame;
            loc = sf != null?sf.evaluateLocation(location):
                            mEngine.evaluateLocation(location,0);
        }
		return loc;
	}
    
    private void associateCommandWithBreakpoint(Breakpoint bp, String cmd){
        if (mBreakpointObserver == null){
            mBreakpointObserver = new BreakpointManager.IObserver(){

                @Override
                public void onRemoved (Breakpoint b) {
                    mBreakpointExecMap.remove(b);                  
                }

                @Override
                public void onCreated (Breakpoint b) {}

                @Override
                public void onStateChange (Breakpoint b) {
                }

                @Override
                public void onConditionChange (Breakpoint b) {
                }

                   // Called when breakpoint hit. If it has an associated "exec"
                // command, then invoke it.
                @Override
                public void onHit (Breakpoint b) {
                    String command = mBreakpointExecMap.get(b);
                    if (command != null && mCommandInvoker != null){
                        try {
                            mCommandInvoker.processCommand(command);
                        }
                        catch (Exception e) {
                            if (mErr != null){
                                mErr.error(e.getMessage());
                            }
                        }
                    }
                    
                }};
            mBreakpointManager.addObserver(mBreakpointObserver);
            mBreakpointExecMap = new HashMap<Breakpoint,String>();
        }
        mBreakpointExecMap.put(bp,cmd);
    }
    
    /**
     * Add a breakpoint
     * @param var
     * @param length
     * @param condition
     * @param forWriting break when watched memory is written.
     * @param forReading break when watched memory is read.
     * @param value if not <code>null</code> then break if "mask&content == mask&value".
     * @param mask see description of value; may be <code>null</code>.
     * @param invert if we're to break if "mask&content != mask&value"
     * @throws EngineException
     * @throws EvaluationException
     */
    public void addWatchpoint(String var,int length,String condition,boolean forWriting, boolean forReading,int tid,
        String value, String mask, boolean invert) throws EngineException, EvaluationException{   
        int flags = 0;
        if (forWriting) flags |= IEngineAPI.WP_WRITE;
        if (forReading) flags |= IEngineAPI.WP_READ;
        String[] attributes = null;
        if (value != null && mask != null){
            attributes = new String[invert?3:2];
            attributes[0] = "value=" + value;
            attributes[1] = "mask=" + mask;
            if (invert){
                attributes[2] = "invert=1";
            }
        }
        Watchpoint bp = mBreakpointManager.create(var,length,condition,tid,flags,mStackFrame,true,attributes);
        if (bp != null)
             mOut.println("Added " + bp);
        else
            mOut.println("Watchpoint NOT added");
    }
    
    /**
     * Shutdown the engine.
     * @throws EngineException
     */
    public void doQuit() throws EngineException{
        mEngine.shutdown();
    }
    
    /**
     * Restart the engine.
     * @param args argument to be used, if not null.
     * @throws EngineException
     */
    public void doRestart(String args) throws EngineException {
        String cmd[] = args != null?StringUtil.stringToArray(args):null;
        if (mEngine.restart(cmd)){
            mEngine.resume(0); // GUI requires this because it
                    // doesn't yet process RestartEvent
        }
    }
    
    /**
     * Load a new program to execute.
     * @param args
     * @throws EngineException
     */
    public void loadProgram(String args) throws EngineException {
        if (mEngine.isActive()){
            mEngine.terminate();
        }
        String cmd[] = StringUtil.stringToArray(args);
        if (mEngine.loadProgram(cmd)){
            //mProgram = args;
        }
        else mErr.error("Can't load program \"" + args + "\"");
    }
    
    /**
     * Remove a breakpoint.
     * @param id
     * @throws EngineException
     */
    public void removeBreakpoint(int id) throws EngineException{
        if (mBreakpointManager.removeID(id)){
            mOut.println("Breakpoint #" + id + " removed");
        }
        else
            mOut.println("Could not remove breakpoint #" + id);
    }
    
    /**
     * Display memory.
     * @param l
     * @param len
     * @throws EngineException
     */
    public void displayMemory(Location l, int len) throws EngineException{
        byte buf[] = mEngine.getMemoryBytes(l.getAddress(),len,0);
        mOut.println(l);
        String hex = "0123456789ABCDEF";
        for (int i = 0; i < buf.length; i += 16){
            int hi = Math.min(i+16,buf.length);
            mOut.print("    0x" + Long.toHexString(l.getAddress()+i)+":");
            for (int j = i; j < hi; j++){
                mOut.print(' ');
                mOut.print(hex.charAt((buf[j]>>4)&0xF));
                mOut.print(hex.charAt(buf[j]&0xF));   
            }  
            mOut.println();
        }
    }
    
    /**
     * Display threads.
     * @throws EngineException
     */
    public void displayThreads() throws EngineException{
        int threads[] = mEngine.getThreads();
        if (threads == null || threads.length == 0) {
            mOut.println("No threads active");
            return;
        }
        mOut.println("Active threads:");
        for (int i = 0; i < threads.length; i++){
            mOut.println("   "+threads[i] + ": "+ mEngine.getThreadName(threads[i]));
        }
    }
    
    /**
     * Display stack trace.
     * @param thread
     * @throws EngineException
     */
    public void stackTrace(int thread) throws EngineException{
         StackFrameRef sf = mEngine.getTopStackFrame(thread);
         while (sf != null){
             Location loc = mEngine.computeLocation(sf.getPC());
             mOut.println("    " + loc);
             sf = sf.getCallerFrame();
         }
    }
    
    /**
     * Set source path
     * @param path
     * @throws EngineException
     */
    public void setSourcePath(String path) throws EngineException{
        String array[] = StringUtil.pathToArray(path,System.getProperty("path.separator",":"));
        mEngine.setSourceDirectories(array);
    }
    
    /**
     * Run until the PC is equal to an address.
     * @param address an expression representing the
     * address.
     * @throws EngineException
     * @throws EvaluationException 
     */
    public void doRunUntilAddress(String address) throws EngineException, EvaluationException{
    	Location loc = createLocation(address);
        if (loc != null) {
            mEngine.runToAddress(0, loc);
        }      
    }
    
    /**
     * Display local variables
     * @param tid the threadID from which
     * to access locals, or else 0 to mean "current" thread.
     * @throws EngineException
     */
    public void displayLocals(int tid) throws EngineException{
        StackFrameRef sf = mEngine.getTopStackFrame(tid);
        Variable[] vars = sf != null?sf.getLocals():null;
        boolean found = false;
        if (vars != null) {
            for (int i = 0; i < vars.length; i++) {
                found = true;
                if (vars[i].getType() != null)
                    mOut.print(vars[i].getType() + " ");
                mOut.println(vars[i].getName() + ": " + vars[i].getValue());
            }
        }
        if (!found){
            mOut.println("No locals");
        }
    }
    
    private void P(String s) {
        mOut.println(s);
    }

    private void P(Location l) {
        if (l.getFunction() != null) {
            mOut.print(l.getFunction());
            if (l.getFunctionOffset() != 0) {
                mOut.print("+" + l.getFunctionOffset());
            }
        }
        mOut.print("@0x" + Long.toHexString(l.getAddress()));
        if (l.getSource() != null) {
            mOut.print(" \"" + l.getSource() + "\"!" + l.getSourceLine());
            if (l.getSourceLineOffset() != 0)
                    mOut.print("[+" + l.getSourceLineOffset() + "]");
        }
        mOut.println();
    }
    
    /**
     * Display registers for current (stopped) thread.
     * @throws EngineException
     */
    public void displayRegisters () throws EngineException {
        if (true)
            mEngine.invokeCommand("regs");
//        else {
//            int bcnt = mEngine.getRegisterBankCount();
//            StackFrameRef frame = mEngine.getTopStackFrame(mEngine.getCurrentThread());
//            if (frame == null)
//                P("No stackframe for thread 0");
//            else {
//                P("Total register bank count = " + bcnt);
//                for (int i = 0; i < bcnt; i++) {
//                    P("   Bank " + i + ": " + mEngine.getRegisterBankName(i));
//                    int[] regs = mEngine.getRegisterIDsFromBank(i);
//                    for (int j = 0; j < regs.length; j++) {
//                        P("        " +
//                            mEngine.getRegisterName(regs[j]) +
//                            ":\t" +
//                            frame.getRegisterValue(regs[j], Format.NATURAL));
//                    }
//                }
//            }
//        }
    }
    
    public void displayModules() throws EngineException {
        int[] m = mEngine.getModules();
        if (m.length == 0) P("No modules!");
        for (int i = 0; i < m.length; i++) {
            P("Module " + m[i] + " = " + mEngine.getModuleName(m[i]));
            P("     base=0x"
                    + Long
                            .toHexString(mEngine.getModuleBaseAddress(m[i]) & 0xFFFFFFFFL));
            Location[] funcs = mEngine.getFunctionsWithinModule(m[i]);
            for (int j = 0; j < funcs.length; j++) {
                mOut.print("     ");
                P(funcs[j]);
            }
        }
        mOut.flush();
    }
    
    public void setStackFrame(StackFrameRef sref){
        mStackFrame = sref;
    }
    
    public int getSelectedThreadID() {
        return mStackFrame != null?mStackFrame.getThreadID():0;
    }
}
