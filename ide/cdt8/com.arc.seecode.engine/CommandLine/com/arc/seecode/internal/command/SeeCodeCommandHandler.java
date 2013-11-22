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

import java.util.StringTokenizer;

import com.arc.seecode.command.IErrorHandler;
import com.arc.seecode.engine.Breakpoint;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.LocationBreakpoint;
import com.arc.seecode.engine.Watchpoint;

/**
 * Processes most SeeCode commands. The "do_<i>name</i>" and 
 * "repeat_<i>name</i>" methods are invoked by reflection to process
 * command <i>name</i>.
 * <P>
 * NOTE: this class must be made public for reflection to work.
 * <P>
 * If the command has an embedded capitol X, then that marks the minimum
 * number of characters that must be specified for the command. Otherwise,
 * the entire command must be typed. For example:
 * <pre>
 *    do_bXreak
 * </pre>
 * Means that any one of these commands will invoke this method:
 * <pre>
 * b
 * br
 * bre
 * brea
 * break
 * </pre>
 * @author David Pickens
 */
public class SeeCodeCommandHandler {

    private EngineInterface mEngine;

    private IErrorHandler mErrorHandler;

    private CommandService mService;

    private boolean mTerminated = false;

    private String mLastArgs;

    /**
     * Create a command processor.
     * 
     * @param engine
     *            the debugger engine object.
     */
    public SeeCodeCommandHandler(EngineInterface engine, 
            IErrorHandler ehandler, CommandService service) {
        mEngine = engine;
        mService = service;
        mErrorHandler = ehandler;
    }

    /**
     * Called by reflection to process the "load" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * load <i>exe-path </i> <i>[args...] </i>
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>exe-path </i> is the path of an exe file to load.
     * <li><i>args </i> are arguments, if any, to be passed to the exe. Tokens
     * with whitespace must be quoted.
     * </ul>
     * 
     * @param args
     *            arguments to the load command
     */
    public void do_load(String args) throws EngineException {
        mService.loadProgram(args);
    }
    
    boolean isTerminated(){
        return mTerminated;
    }

    /**
     * Called by reflection to process the "load" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * go <i>[target] </i>]
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>target </i> target address,
     * </ul>
     * 
     * @param args
     *            arguments to the go command
     */
    public void do_go(String args) throws EngineException {
        args = args.trim();
        if (args.length() != 0) {
            try {
				mService.doRunUntilAddress(args);
			} catch (EvaluationException e) {
				error(e.getMessage());
			}
        } else
            do_run(args);
    }

    /**
     * Called by reflection to process the "stop" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * stop <i>[thread] </i>
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>thread </i> is the id of a thread to stop. If not present, then
     * all threads to be stopped.
     * </ul>
     * 
     * @param args
     *            arguments to the load command
     */
    public void do_stop(String args) throws EngineException {
        shouldBeNoArgs(args);
        mEngine.stop(0);
    }

    /**
     * Called by reflection to process the "threads" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * threads
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the command
     */
    public void do_thrXeads(String args) throws EngineException {
        shouldBeNoArgs(args);
        mService.displayThreads();
    }
    
    /**
     * Called by reflection to process the "quit" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * threads
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the command
     */
    public void do_quit(String args) throws EngineException {
        shouldBeNoArgs(args);
        mService.doQuit();
        mTerminated = true;
    }

    /**
     * Called by reflection to process the "locals" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * locals [ <i>thread </i>]
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the command
     */
    public void do_locXals(String args) throws EngineException {
        int tid = parseThreadID(args);
        mService.displayLocals(tid);
    }

    /**
     * Called by reflection to process the "mem" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * mem <i>address </i>
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the command
     */
    public void do_memXory(String args) throws EngineException {
        Location l;
        try {
            l = mEngine.evaluateLocation(args, 0);

        } catch (EvaluationException e) {
            throw new EngineException(e.getMessage(),e);
        }
        if (l != null) {
            mLastArgs = "0x" + Long.toHexString(l.getAddress() + 32);
            mService.displayMemory(l, 32);
        }
    }
    
    
    /**
     * Called by reflection to repeat the "memory" command
     * @todo davidp needs to add a method comment.
     * @throws EngineException
     */
    public void repeat_memory() throws EngineException {
        do_memXory(mLastArgs);
    }

 

    public void do_regs(String args) throws EngineException {
        shouldBeNoArgs(args);
        mService.displayRegisters();
    }
    
    public void do_restart(String args) throws EngineException{
        if (args.length()==0)
            args = null; // means to re-use original arguments
        mService.doRestart(args);       
    }

    public void do_modXules(String args) throws EngineException {
        shouldBeNoArgs(args);
        mService.displayModules();
    }

    /**
     * Called by reflection to process the "stack" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * stack
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the command
     */
    public void do_stack(String args) throws EngineException {
        int tid = parseThreadID(args);
        mService.stackTrace(tid);

    }

    /**
     * Called by reflection to process the "set_source_path" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * stack
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the command
     */
    public void do_set_source_path(String args) throws EngineException {
        mService.setSourcePath(args);

    }

    private int parseThreadID(String args) {
        int tid = 0;
        args = args.trim();
        if (args.length() > 0) {
            try {
                tid = Integer.parseInt(args);
            } catch (NumberFormatException x) {
                error("Invalid thread id: \"" + args + "\"");
            }
        }
        tid = this.mService.getSelectedThreadID();
        if (tid == 0) {
            int threads[];
            try {
                threads = mEngine.getThreads();
                if (threads.length > 0){
                    tid = threads[0]; // which thread is the default?
                }
            }
            catch (EngineException e) {
                //Couldn't retrieve threads; assume diagnosed later.
            }
        }       
        return tid;
    }
    
    private int parseOptionalCount(String args){
        args = args.trim();
        int cnt = 1;
        if (args.length() > 0){
            try {
                cnt = Integer.parseInt(args);
                if ( cnt < 1)
                    error("Step count must be > 0: " + args);
            } catch (NumberFormatException x) {
                error("Invalid step count: \"" + args + "\"");
            }
        }
        return cnt;
    }

    /**
     * Called by reflection to process the "run" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * run
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * </ul>
     * 
     * @param args
     *            arguments to the load command
     */
    public void do_run(String args) throws EngineException {
        shouldBeNoArgs(args);
        if (mEngine.queryState(0) == IEngineAPI.NOT_STARTED)
            mEngine.start();
        else
            mEngine.resume(0);
    }
    
    public void repeat_run() throws EngineException {
        mEngine.resume(0);
    }

    /**
     * Called by reflection to process the "args" command, which is used to pass
     * engine arguments (not program arguments).
     * <P>
     * An argument may be prefixed by "@" to denote a file containing arguments.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * args <i>[args...] </i>
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>args </i> are arguments, if any, to be passed to the exe. Tokens
     * with whitespace must be quoted.
     * </ul>
     * 
     * @param args
     *            arguments to the load command
     */
    public void do_args(String args) throws EngineException {
        mEngine.setEngineArguments(args);
    }

    /**
     * Called by reflection to process the "isi" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * isi [cnt]
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the isi command
     */
    public void do_isiXq(String args) throws EngineException {
        int cnt = parseOptionalCount(args);
        mEngine.instructionStep(0, false,cnt);
        mLastArgs = args;
    }
    
    public void repeat_isi()throws EngineException{
        do_isiXq(mLastArgs);
    }

    /**
     * Called by reflection to process the "ssi" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * isi
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the ssi command
     */
    public void do_ssiXq(String args) throws EngineException {
        int cnt = parseOptionalCount(args);
        mEngine.statementStep(0, false,cnt);
        mLastArgs = args;
    }
    
    public void repeat_ssi()throws EngineException{
        do_ssiXq(mLastArgs);
    }
    
    public void do_stepXq(String args) throws EngineException {
        do_ssiXq(args);
    }
    
    public void repeat_step() throws EngineException{
        do_ssiXq(mLastArgs);
    }
    
    public void do_stepiXq(String args) throws EngineException {
        do_isiXq(args);
    }
    
    public void repeat_stepi() throws EngineException{
        do_isiXq(mLastArgs);
    }

    /**
     * Called by reflection to process the "sso" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * sso
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the sso command
     */
    public void do_ssoXq(String args) throws EngineException {
        int cnt = parseOptionalCount(args);
        mEngine.statementStep(0, true,cnt);
        mLastArgs = args;
    }
    
    public void repeat_ssoXq() throws EngineException {
        do_ssoXq(mLastArgs);
    }
    
    public void do_retXurn(String args) throws EngineException {
        shouldBeNoArgs(args);
        mEngine.stepOut(0);
    }

    public void repeat_ret() throws EngineException {
        mEngine.stepOut(0);
    }
    /**
     * Called by reflection to process the "iso" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * iso
     * 
     * </PRE>
     * 
     * @param args
     *            arguments to the break command
     */
    public void do_isoXq(String args) throws EngineException {
        int cnt = parseOptionalCount(args);
        mEngine.instructionStep(0, true,cnt);
        mLastArgs = args;
    }
    public void repeat_iso() throws EngineException {
        do_isoXq(mLastArgs);
    }
    
    /**
     * Called by reflection to process the "delete" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * enable [ break|watch] <i>breakpoint-index... </i>
     * delete [ break|watch] all
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>breakpoint-index </i> is the index of a breakpoint to remove.
     * 
     * </ul>
     * 
     * @param args
     *            arguments to the break command
     */
    public void do_enable(String args) throws EngineException {
        doEnable(args,true);
    }
    public void do_disable(String args) throws EngineException {
        doEnable(args,false);
    }
    private void doEnable(String args, boolean enable) throws EngineException {
        StringTokenizer each = new StringTokenizer(args, ", ");
        boolean first = true;
        Kind kind = Kind.NONE;
        boolean doALL = false;
        while (each.hasMoreTokens()){
            String id = each.nextToken(", ");
            if (first){
                if ("break".startsWith(id)){
                    kind = Kind.BREAK;
                } else if ("watch".startsWith(id)){
                    kind = Kind.WATCH;
                }
                if (kind != Kind.NONE){
                    if (!each.hasMoreTokens()){
                        error("Missing operand");
                    }
                    else id = each.nextToken(",").trim();
                }
                if (id.equals("all")){
                    doALL = true;
                    if (each.hasMoreTokens()){
                        error("extraneous operands after \"all\"");
                    }
                }
                first = false;
            }
            if (doALL){
                if (kind == Kind.NONE){
                    mService.setEnableAllBreakpoints(enable);
                    mService.setEnableAllWatchpoints(enable);
                }
                else if (kind == Kind.BREAK){
                    mService.setEnableAllBreakpoints(enable);
                }
                else if (kind == Kind.WATCH){
                    mService.setEnableAllWatchpoints(enable);
                }
            }
            else {
                int index = getInt(id);
                Breakpoint bp = mEngine.getBreakpointManager().getBreakpointFromID(index);
                if (kind == Kind.BREAK && !(bp instanceof LocationBreakpoint)){
                    error(id + " is not a valid location breakpoint");
                }
                else if (kind == Kind.WATCH && !(bp instanceof Watchpoint)) {
                    error(id + " is not a valid watchpoint");
                }
                else if (bp == null){
                    error(id + " is not a valid breakpoint");
                }
                else bp.setEnabled(enable);
            }
        }
        if (first) error("missing operands");
    }

    /**
     * Called by reflection to process the "delete" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * delete <i>breakpoint-index... </i>
     * delete [ break|watch|symbol|modules ] [all] <i>index...</i>
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>breakpoint-index </i> is the index of a breakpoint to remove.
     * 
     * </ul>
     * 
     * @param args
     *            arguments to the break command
     */
    public void do_delXete(String args) throws EngineException {
        StringTokenizer each = new StringTokenizer(args, ", ");
        boolean first = true;
        Kind kind = Kind.NONE;
        boolean doALL = false;
        while (each.hasMoreTokens()) {
            String id = each.nextToken();
            if (first){
                if ("watch".startsWith(id)){
                    kind = Kind.WATCH;
                }
                else if ("break".startsWith(id)){
                    kind = Kind.BREAK;
                }
                else if ("symbols".startsWith(id)){
                    kind = Kind.SYMBOLS;
                }
                else if ("modules".startsWith(id)){
                    kind = Kind.MODULES;
                }
                if (kind != Kind.NONE){
                    if (!each.hasMoreTokens()){
                        error("Missing ID");
                    }
                    id = each.nextToken().trim();
                }
                if ("all".equals(id)){
                    doALL = true;
                    if (each.hasMoreTokens()){
                        error("Extraneous tokens after \"all\"");
                    }
                }
                first = false;
            }
            if (doALL){
                if (kind == Kind.NONE){
                    mService.deleteAllBreakpoints();
                    mService.deleteAllWatchpoints();
                }
                else if (kind == Kind.WATCH){
                    mService.deleteAllWatchpoints();
                }
                else if (kind == Kind.BREAK){
                    mService.deleteAllBreakpoints();
                }
                else if (kind == Kind.SYMBOLS){
                    mEngine.invokeCommand("delete symbols all");
                    //mService.deleteAllSymbols();
                }
                else if (kind == Kind.MODULES){
                    mEngine.invokeCommand("delete modules all");
                    //mService.deleteAllModules();
                }
            }
            else {
                int index = getInt(id);
                if (kind == Kind.NONE)
                    mService.removeBreakpoint(index);
                else if (kind == Kind.BREAK){
                    Breakpoint bp = mEngine.getBreakpointManager().getBreakpointFromID(index);
                    if (bp instanceof LocationBreakpoint){
                        mService.removeBreakpoint(index);
                    }
                    else error("ID " + index + " does not correspond to a breakpoint");
                } else if (kind == Kind.WATCH){
                    Breakpoint bp = mEngine.getBreakpointManager().getBreakpointFromID(index);
                    if (bp instanceof Watchpoint){
                        mService.removeBreakpoint(index);
                    }
                    else error("ID " + index + " does not correspond to a watchpoint");
                } else if (kind == Kind.SYMBOLS){
                    mEngine.invokeCommand("delete symbol " + id);
                    //mService.removeSymbol(index);
                } else if (kind == Kind.MODULES){
                    mEngine.invokeCommand("delete module " + id);
                    //mService.removeModule(index);
                }
            }
        }
        if (first){
            error("Missing operands");
        }

    }

    /**
     * Called by reflection to process the "break" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * break address [,[count] <i>NNN </i>] [,eval <i>expr </i>] [,thread <i>tid
     * </i>]
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>NNN </i> is a postive integer that indicates the number of times
     * the breapoint is hit before stopping.
     * 
     * <li><i>expr </i> is a conditional expression; the breakpoint only works
     * if the expression evaluates to true.
     * 
     * <li><i>tid </i> the thread to be stopped if breakpoint is hit.
     * </ul>
     * 
     * @param args
     *            arguments to the break command
     */
    public void do_bXreak(String args) throws EngineException {
        args = args.trim();
        if (args.length() == 0) {
            mService.displayBreakpoints();
        } else {
            StringTokenizer each = new StringTokenizer(args, ",");
            String location = each.nextToken(", ");
            int hitCount = 0;
            String condition = null;
            String exec = null;
            int tid = CommandService.NO_THREAD;
            int flags = 0;
            boolean okay = true;
            while (each.hasMoreTokens()) {
                String p = each.nextToken(",").trim();
                StringTokenizer subEach = new StringTokenizer(p);
                String s = subEach.nextToken();
                if ("count".startsWith(s)) {
                    hitCount = getInt(subEach);
                } else if (s.length() >=2 && "eval".startsWith(s)) {
                    condition = getText(subEach);
                } else if ("thread".startsWith(s) || "task".startsWith(s)) {
                    tid = getInt(subEach);
                    if (tid == CommandService.NO_THREAD){
                        error("Invalid thread ID: " + subEach);
                    }
                } else if (s.length() >= 2 && "exec".startsWith(s)){
                    exec = getText(subEach);
                } else if (s.length() >= 4 && "hardware".startsWith(s)){
                    flags |= IEngineAPI.BP_HARDWARE;
                    if ((flags & IEngineAPI.BP_SOFTWARE) != 0){
                        error("\"soft\" and \"hard\" qualifiers are mutually exclusive");
                        okay = false;
                    }
                } else if (s.length() >= 4 && "software".startsWith(s)){
                    flags |= IEngineAPI.BP_SOFTWARE;
                    if ((flags & IEngineAPI.BP_HARDWARE) != 0){
                        error("\"soft\" and \"hard\" qualifiers are mutually exclusive");
                        okay = false;
                    }
                } else if (s.length() >= 4 && "temporary".startsWith(s)) {
                    flags |= IEngineAPI.BP_TEMPORARY;
                } else if (s.length() >= 4 && "global".startsWith(s)) {
                    //???
                } else if (s.length() >= 3 && "disabled".startsWith(s)) {
                    flags |= IEngineAPI.BP_DISABLED;
                } else if (p.length() > 0 && Character.isDigit(p.charAt(0))) {
                    hitCount = getInt(p);
                } else {
                    error("Unrecognized breakpoint sub-qualifier: " + p);
                    okay = false;
                }
                if (subEach.hasMoreTokens()) {
                    error("Extraneous tokens in break command: "
                            + getText(subEach));
                    okay = false;
                }
            }
            if (okay) {
                try {
                    mService.addBreakpoint(location, hitCount, condition, exec, tid, flags);
                } catch (EvaluationException e) {
                    error(e.getMessage());
                }
            }
        }
    }
    
    enum Kind { NONE, BREAK, WATCH, SYMBOLS, MODULES}
    
    /**
     * Called by reflection to process the "watch" command.
     * <p>
     * Syntax:
     * 
     * <PRE>
     * 
     * break address [,length] [,eval <i>expr </i>] [,thread <i>tid
     * </i>]
     * 
     * </PRE>
     * 
     * where:
     * <ul>
     * <li><i>length </i> is a postive integer that indicates the length of
     * the memory to be watched, if it isn't obvious form the address expression.
     * 
     * <li><i>expr </i> is a conditional expression; the breakpoint only works
     * if the expression evaluates to true.
     * 
     * <li><i>tid </i> the thread to be stopped if breakpoint is hit.
     * </ul>
     * 
     * @param args
     *            arguments to the break command
     */
    public void do_wXatch(String args) throws EngineException {
        args = args.trim();
        if (args.length() == 0) {
            mService.displayWatchpoints();
        } else {
            StringTokenizer each = new StringTokenizer(args, ",");
            String address = each.nextToken(", ");
 
            int dataSize = 0;
            String condition = null;
            int tid = 0;
            boolean okay = true;
            boolean first = true;
            boolean forReading = false;
            boolean forWriting = true;
            String valueAttribute = null;
            String maskAttribute = null;
            boolean invert = false;
            while (each.hasMoreTokens()) {
                String p = each.nextToken(",").trim();
                StringTokenizer subEach = new StringTokenizer(p);
                String s = subEach.nextToken();
                if (first && p.length() > 0 && Character.isDigit(p.charAt(0)))
                    dataSize = getInt(p);
                else
                if ("length".startsWith(s)){
                    dataSize= getInt(subEach);
                }
                else
                if (s.equals("eval")) {
                    condition = getText(subEach);
                } else if (s.equals("thread")) {
                    tid = getInt(subEach);
                }
                else if (s.equals("mode")){
                    String mode = getText(subEach);
                    if (mode.equalsIgnoreCase("write") || mode.equalsIgnoreCase("w")){
                        forWriting = true;
                        forReading = false;
                    }
                    else if (mode.equalsIgnoreCase("read") || mode.equalsIgnoreCase("r")){
                        forWriting = false;
                        forReading = true;
                    }
                    else if (mode.equalsIgnoreCase("rw")){
                        forWriting = true;
                        forReading = true;
                    }
                    else error("Unrecognized watchpoint mode: " + mode);
                } else if (s.equals("value")){
                    valueAttribute = getText(subEach);   
                } else if (s.equals("mask")){
                    maskAttribute = getText(subEach);           
                } else if (s.equals("invert")){
                    invert = false;        
                } else {
                    error("Unrecognized watchpoint sub-qualifier: " + p);
                    okay = false;
                }
               
                if (subEach.hasMoreTokens()) {
                    error("Extraneous tokens in watch command: "
                            + getText(subEach));
                    okay = false;
                }
                first = false;
            }
            if (okay) {
                try {
                    mService.addWatchpoint(address, dataSize, condition, forWriting,forReading,tid,
                        valueAttribute,maskAttribute,invert);
                } catch (EvaluationException e) {
                    throw new EngineException(e.getMessage(),e);
                }
            }
        }
    }

    private int getInt(StringTokenizer each) {
        String s = each.nextToken();
        return getInt(s);
    }

    private int getInt(String s) {
        try {
            if (s.startsWith("0x"))
                    return (int) Long.parseLong(s.substring(2), 16);
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            error("Integer expected: " + s);
        }
        return 0;
    }
    
    /**
     * A temporary hook to get commands directly to the engine.
     * @param args
     * @throws EngineException
     */
    public void do_command(String args) throws EngineException{
        mEngine.invokeCommand(args);
    }

    private String getText(StringTokenizer each) {
        return each.nextToken("").trim();
    }

    private void error(String msg) {
        mErrorHandler.error(msg);
        throw new CommandExitException(msg);
    }


    
    /**
     * Confirm that args is an empty string.
     * @param args
     */
    private boolean shouldBeNoArgs(String args){
        if (args != null && args.trim().length() > 0){
            error("Superfluous operands: " + args);
            return false;
        }
        return true;
    }
}
