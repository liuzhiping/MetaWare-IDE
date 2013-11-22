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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration2;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;

import com.arc.cdt.debug.seecode.core.ISeeCodeConstants;
import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.SeeCodeDebugger;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.cdi.ICDISeeCodeSession;
import com.arc.cdt.debug.seecode.core.launch.ICMPDInfo;
import com.arc.cdt.debug.seecode.core.launch.IServerLauncher;
import com.arc.cdt.debug.seecode.core.launch.ServerLauncherFactory;
import com.arc.cdt.debug.seecode.internal.core.cdi.types.TypeFactory;
import com.arc.cdt.debug.seecode.options.ConfigurationException;
import com.arc.cdt.debug.seecode.options.SeeCodeOptions;
import com.arc.seecode.cmpd.CMPDFactory;
import com.arc.seecode.cmpd.ICMPDController;
import com.arc.seecode.connect.ConnectionFactory;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.ITypeFactoryFactory;
import com.arc.seecode.engine.ProcessIdList;
import com.arc.seecode.engine.config.ConfigException;
import com.arc.seecode.engine.type.ITypeFactory;

/**
 * A SeeCode debug session.
 * <P>
 * We make this implement {@link ICDISessionObject}so that it can qualify as a
 * reason for a {@link ICDISuspendedEvent}when the user requests the event.
 * 
 * @author David Pickens
 */
public class Session implements ICDISeeCodeSession, ICDISessionObject, ICDIEventListener, IAdaptable,
ISuspendResume, IStep, IRestart {

    private ICDISessionConfiguration2 mCDIConfig;

    //private String[] mArgs = new String[0]; // arguments to program

   // private Properties mEnv = null; // Environment

    //private String mWorkingDir = null; // working directory

    private Target[] mTargets = null;

    private Properties mProps = new Properties();

    private EventManager mEventManager;

    private SeeCodeEngineProcess mSessionProcess = null;

    private boolean mShutdown;

    private EngineObserver mEngineObserver;

    private Process mProcess;

    private boolean mProcessDied;

    private boolean mWaitForAccept;
    
    private Object mProcessLock = new Object();

    private boolean mShutdownPending;
    private String mProjectName;

    private IProject mProject;

    private ILaunch mLaunch;

    private Writer mSeeCodeConsoleWriter = null;
    private Writer mSeeCodeConsoleErrorWriter = null;
    
    private List<ISessionDisposeListener> mDisposeListeners = null;

    private InputStream mInputStream;  //stdout of the engine process

    private InputStream mErrorStream; // stderr of the engine process

    private StreamReader mStdoutReader;

    private StreamReader mStderrReader;
    
    private ICMPDController mCMPDController = null;
    
    private String[] fCmpdStartupCommands = null;
    
    private boolean mIsNoProject = false;
    
    private String mNoProjectSessionDir = null;

    /**
     * Attach to an {@link EngineInterface}instance and load a program.
     * 
     * @param launch
     *            the launcher
     * @param exe
     *            the file to be loaded.
     * @param engineOptions
     *            an array of strings of the form "-Xtoggle1" "-Xtoggle2", etc,
     *            for setting toggles in the engine process.
     * @param monitor progress monitor
     * @throws CoreException
     *             an error of some sort occurred.
     */
    public Session(ILaunch launch, File exe, List<String> engineOptions, IProgressMonitor monitor)
            throws CoreException {
        boolean startedOkay = false;
        mLaunch = launch;
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        ICMPDInfo cmpdInfo = null;
        mIsNoProject = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_NOPROJECT, false);
        if(mIsNoProject)
        	mNoProjectSessionDir = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_NOPROJECT_SESSION_DIRECTORY, (String)null);
        
        if (config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT, 0) != 0){
            cmpdInfo = ServerLauncherFactory.getCMPDInfo(config);
            fCmpdStartupCommands = cmpdInfo.getStartupCommands(); // invoked when last process is created.
        }
     
        if (monitor == null){
            monitor = new NullProgressMonitor();
        }
        
        monitor.beginTask("Debugger Session Creation",100);
        try {
           
        
          if(!mIsNoProject){
                  
            mProjectName = config.getAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                    "Project?");
            mProject = ResourcesPlugin.getWorkspace().getRoot().getProject(mProjectName);
            if (mProject.getLocation() == null) mProject = null; // doesn't exist or is "Project?".
            // If this is CMPD session, reference the project of the first process that
            // has an associated project.
            if (mProject == null && cmpdInfo != null){
                for (ICMPDInfo.IProcess i: cmpdInfo.getProcesses()){
                    if (i.getProject() != null){
                        mProject = i.getProject();
                        mProjectName = mProject.getName();
                        break;
                    }
                }
                
            }
          }
            //
            // Before we invoke the swahili script to
            // build the engine arguments, we want to
            // capture stdout and stderr and pipe them
            // to the build console.
            IConsole console = null;

            console = CCorePlugin.getDefault().getConsole();
            //Allow console for null Project
            //  if (mProject != null)
            console.start(mProject);
            mEventManager = new EventManager(this);
            mEngineObserver = new EngineObserver(this, mEventManager);
            try {
                monitor.setTaskName("Launching engine");
                monitor.worked(10);  // total == 10
        
                String[] env = DebugPlugin.getDefault().getLaunchManager().getEnvironment(config);
                String engineArgs = null;
                SeeCodeOptions.TermSimPort[] termSimPorts = null;

                try {
                    if (cmpdInfo == null) {
                        // Extract arguments to be passed to Swahili processor (e.g. "scac")
                        String swahiliArgs[] = SeeCodeOptions.computeArguments(config);
                        //
                        // Invoke the swahili script to expand the
                        // arguments into a string to pass to the engine, checking
                        // for errors.

                        engineArgs = ServerLauncherFactory.createEngineConfiguration(swahiliArgs, console, env);
                        termSimPorts = SeeCodeOptions.extractTermSimPorts(config);
                    }
                    else {
                        // CMPD session
                        engineArgs = ServerLauncherFactory.createEngineConfiguration(cmpdInfo, console, env);
                        termSimPorts = SeeCodeOptions.extractTermSimPorts(cmpdInfo);
                    }
                }
                catch (ConfigException e) {
                    throw new CoreException(SeeCodePlugin.makeErrorStatus(e.getMessage(),e));
                }
                
                // If the engine will be simulating a UART, then set up terminal simulator.
                if (termSimPorts != null){
                    for (SeeCodeOptions.TermSimPort p: termSimPorts){
                        SeeCodePlugin.getDefault().createTermSimView(this, p.tcpPort, p.uartPort);
                    }
                }
                //NOTE: the Target will be given the
                // working directory before starting the
                // program, but we should go ahead and
                // and grab it so that the engine process
                // is running within the proper working directory.
                File cwd = getWorkingDirectory(config);
                
                String cpu = SeeCodeOptions.getTargetCpuName(config);
                if (cpu == null) cpu = "ac"; // CMPD may not have an explicit project, thus no CPU; assume "ac"
                
                int pidList[] = cmpdInfo != null?computePidList(cmpdInfo):new int[]{1};
                         
                EngineInterface engines[] = launchEngine(env, cwd, cpu, engineOptions, pidList);
                monitor.worked(70); // total == 80

                //
                // Now pass the arguments to the the (first) engine. They apply to the
                // entire session, so it doesn't matter which engine processes it.
                engines[0].setEngineArguments(engineArgs);
                mTargets = new Target[engines.length];
                if (cmpdInfo == null) {
                    String exeName = exe.toString();
                    mTargets[0] = makeTarget(exeName,mProject,engines[0],cwd);
                }                  
                else {
                    int cnt = 0;
                    for (ICMPDInfo.IProcess info: cmpdInfo.getProcesses()){
                        for (ProcessIdList.Range range: info.getIDList().getRanges()){
                            for (int pid = range.getFirst(); pid <= range.getLast(); pid++) {
                                mTargets[cnt] = makeTarget(info.getCommand()[0],info.getProject(),engines[cnt],cwd);
                                mTargets[cnt].setCMPDInfo(info.getProcessName(),pid,info.getInstanceCount(),info.getCommand());
                                cnt++;
                            }
                        }
                    }
                }
               
                startReaderThreads();

                for (int i = 0; i < mTargets.length; i++) {
                    DebuggeeProcess p = new DebuggeeProcess(mTargets[i], mProcess);
                    mTargets[i].setProcess(p);
                }
 
            } catch (RuntimeException e){
                throw e;
            } catch (CoreException e){
                throw e;
            } catch (SocketTimeoutException e){
                // Socket timed out; engine is apparenly hung
                // during initialization but before accepting connection.
                throwSessionCreationException(e);
            } catch (InterruptedIOException e){
                // Engine process died before connection was established.
                // Presumably it was already properly diagnosed.
                if (mSessionProcess != null)
                    mSessionProcess.destroy();
                throw new CoreException(SeeCodePlugin.makeErrorStatus("Debugger engine aborted without connecting to the IDE"));
            } catch (EngineDisconnectedException e){
                // Engine process died before connection was established.
                // Presumably it was already properly diagnosed.
                if (mSessionProcess != null)
                    mSessionProcess.destroy();
                throw new CoreException(SeeCodePlugin.makeErrorStatus("Debugger engine aborted without connecting to the IDE"));
            } catch (Exception e) {
                throwSessionCreationException(e);
            }
            mCDIConfig = new ICDISessionConfiguration2(){

                @Override
                public boolean terminateSessionOnExit() {
                    return true;
                }

                @Override
                public ICDISession getSession() {
                    return Session.this;
                }

                @Override
                public String getSessionProcessName () {
                    try {
                        String versionStrings[] = mTargets[0].getEngineInterface().getEngineVersionStrings();
                        if (versionStrings != null) {
                            StringBuilder buf = new StringBuilder();
                            buf.append("Debugger Engine [v");
                            if (versionStrings[0] != null) {
                                buf.append(versionStrings[0]);
                            }
                            if (versionStrings[2] != null) { //Build ID
                                if (buf.length() > 0) buf.append(",");
                                buf.append(versionStrings[2]);
                            }
                            if (versionStrings[1] != null) {
                                if (buf.length() > 0) buf.append(",");
                                buf.append(versionStrings[1]); // Build date
                            }
                            buf.append("]");
                            return buf.toString();
                        }
                    } catch (EngineException x){                       
                    }
                    return "Debugger Engine";
                }};
           
            mEventManager.addEventListener(this);
            startedOkay = true;
            monitor.worked(20); // total = 100
        } finally {
            // If we didn't get all the way to the end,
            // then kill the session to avoid having
            // an orphaned engine sitting there.
            if (!startedOkay) {
                EngineInterface engine = mTargets != null && mTargets.length > 0 && mTargets[0] != null
                                    ?mTargets[0].getEngineInterface():null;
                if ( engine != null) {
                    try {
                        if (engine.isActive())
                            terminate();
                    } catch (Exception e) {
                        // ingore exceptions; engine may
                        // have already shutdown or aborted
                    }
                }
                if (mEventManager != null)
                    mEventManager.shutdown();
                killReaders();
            }
            monitor.done();
        }
        if (startedOkay){
            SeeCodePlugin.getDefault().addSession(this);
        }

    }
    
    /**
     * Called when the given target's associated program is loaded.
     * For CMPD, we must invoke any startup commands if the last process was invoked.
     * For standalone debugger, startup commands are done by the engine because it loads all
     * processes itself. Since the IDE loads each one individually, we must invoke the startup commands
     * ourselves.
     * @param target the target whose program was just loaded.
     */
    void onLoaded(Target target){
        if (fCmpdStartupCommands != null && fCmpdStartupCommands.length > 0) {
            for (Target t: mTargets) {
                if (!t.isLoaded()) return;
            }
            // OK, all CMPD processes are now loaded. Now, invoke startup commands.
            for (String cmd: fCmpdStartupCommands){
                try {
                    this.mCMPDController.invokeCommand(cmd);
                }
                catch (EngineException e) {
                    SeeCodePlugin.log(e);
                }
            }
        }      
    }
    
    /**
     * Start reader threads that reads stdout and stderr from the engine and forwards it so
     * so that it appears to come from the SeeCodeEngineProcess.
     */
    private void startReaderThreads(){
     
        try {
            mStdoutReader = new StreamReader(mInputStream,
                    ((SeeCodeEngineProcess)this.getSessionProcess()).getProcessOutputStream());
            mStderrReader = new StreamReader(mErrorStream,((SeeCodeEngineProcess)this.getSessionProcess()).getProcessErrorStream());
            new Thread(mStdoutReader,"StdoutReader").start();
            new Thread(mStderrReader,"StderrReader").start();
        } catch (CDIException e) {
            // shouldn't happen
        }
        
    }
    
    /**
     * Kill off the reader threads that are reading stdout and stderr
     * and piping them to the SeeCode Session console.
     * <P>
     * NOTE: we make this synchronized to avoid race condition since it can
     * be called from an event thread or main GUI thread.
     */
    synchronized void killReaders() {
        // Since we have loaded the program without throwing
        // an exception, the caller will have successfully
        // constructed a debug process and will start
        // reading stdout/stderr appropriately. So, we can now
        // kill off our threads that read stdout/stderr.
        if (mStdoutReader != null){
            mStdoutReader.drainInput();
            mStderrReader.drainInput();
            mStdoutReader.terminate();
            mStderrReader.terminate();
            mStdoutReader = null;
            mStderrReader = null;
        }
    }


    private Target makeTarget(String exeName, IProject project, EngineInterface engine, File cwd) throws EngineException{
        Target t = new Target(exeName, this, mProject, engine);
        try {
        	t.getRuntimeOptions().setWorkingDirectory(cwd.toString());
        }
        catch (CDIException e) {
           // Shouldn't happen
        }
        //NOTE: to avoid circular dependencies between this package
        // and the UI package, the
        // CustomDisplayCallback class cannot be directly accessed.
        // Instead, a callback was deposited from the UI
        // package to create it...
        engine.setCustomDisplayCallback(SeeCodePlugin.getDefault().createCustomDisplayCallback(t));
        return t;
    }

    /**
     * Compute the number of CMPD processes we are targeting.
     * @param cmpdInfo CMPD description.
     * @return the number of CMPD process we are targeting.
     */
    private int computeCmpdInstances(ICMPDInfo cmpdInfo){
        if (cmpdInfo == null) return 1;
        int cnt = 0;
        for (ICMPDInfo.IProcess info: cmpdInfo.getProcesses()){
            cnt += info.getInstanceCount();
        }
        return cnt;      
    }
    
    private int[] computePidList(ICMPDInfo cmpdInfo){
        if (cmpdInfo == null) return new int[]{1};
        int cnt = computeCmpdInstances(cmpdInfo);
        int i = 0;
        int pid[] = new int[cnt];
        for (ICMPDInfo.IProcess info: cmpdInfo.getProcesses()){
            ProcessIdList list = info.getIDList();
            for (ProcessIdList.Range range: list.getRanges()){
                for (int p = range.getFirst(); p <= range.getLast(); p++){
                    pid[i++] = p;
                }
            }
        }
        if (i != cnt) throw new IllegalStateException("CMPD PID count not consistent");
        return pid;
    }

    /**
     * @param e
     * @throws CoreException
     */
    private void throwSessionCreationException(Exception e) throws CoreException {
        if (mSessionProcess != null)
            mSessionProcess.destroy();
        SeeCodePlugin.log(e);
        throw new CoreException(SeeCodePlugin.makeErrorStatus(e.getMessage(),e));
    }
    
    public String getProjectName(){
        return mProjectName;
    }
    
    /**
     * Return the directory where we pass "args" back
     * to the engine.
     * @return the directory where we store per-project
     * or directory of elf file for the case of no project
     * arguments and such.
     */
    @Override
    public File getSessionDirectory(){
    	if(mIsNoProject){
    		if(mNoProjectSessionDir != null)
    			return new File(mNoProjectSessionDir);
    		return null;
    		
    	}
    	//project case
    	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(mProjectName);
        return project.getLocation() != null? project.getLocation().toFile():null;
    
    }
    
   
    /**
     * Return the path of where the engine is to
     * read the ".args" file from an Options window.
     * The string has a "%s" where the prefix is
     * to be inserted.
     * @return the path where the engine is to
     * read a file produced by an Optionw dialog window.
     */
    @Override
    public String getArgsPattern(){
        File d = getSessionDirectory();
        return d.toString() + File.separator + ".%s.args";       
    }

    Writer getStdout() {
        if (mSeeCodeConsoleWriter == null){
            mSeeCodeConsoleWriter = new OutputStreamWriter(mSessionProcess.getProcessOutputStream());
        }
        return mSeeCodeConsoleWriter;
    }

    Writer getStderr() {
        if (mSeeCodeConsoleErrorWriter == null){
            mSeeCodeConsoleErrorWriter = new OutputStreamWriter(mSessionProcess.getProcessErrorStream());
        }
        return mSeeCodeConsoleErrorWriter;
    }
    
    /**
     * Launch the engine as a separate process, and create an EngineInterface proxy per
     * (CMPD) process. For non-CMPD, it will be a single instance.
     * @param environment the OS environment, or <code>null</code> if the IDE's environment is to be used.
     * @param workingDirectory the working directory for the new process.
     * @param cpu the target CPU to identify which version of the debugger to invoke; CMPD will be "ac" for
     *   ARCompact.
     * @param engineOptions toggle settings for server.
     * @param instances the number (CMPD) processes to be instantiated (1 if not CMPD).
     * @param highestPID the high process ID number if CMPD; 1 if non CMPD.
     * @return an array of EngineInterface instances, one per CMPD process.
     * @throws CoreException
     * @throws IOException
     * @throws EngineException
     */
    private EngineInterface[] launchEngine(String environment[],
            File workingDirectory,
            String cpu, List<String> engineOptions,
            int pid[])
            throws CoreException, IOException, EngineException {

        IServerLauncher serverLauncher = ServerLauncherFactory.createServerLauncher();
        final SocketTransport transport = serverLauncher.makeConnection();
        serverLauncher.setEngineToggles(engineOptions); 
        serverLauncher.setPids(pid);
        if (engineToBeDebuggerAttachable()){
            serverLauncher.setDebuggerAttachPort(8000);
        }
        if (mEngineObserver == null) throw new IllegalStateException("Engine observer not yet created");
        
        //cr98056: if the use doesn't have SystemRoot under XP, things will timeout.
        if ("Windows XP".equals(System.getProperty("os.name")) && environment != null){
        	boolean found = false;
            for (String s:environment){
            	if (s.startsWith("SystemRoot=")){
            		found = true;
            		break;
            	}
            }
            if (!found){
            	SeeCodePlugin.getDefault().displayError("Environment Error",
            			"To establish a connection to the debugger under Windows XP,\n"+
            			"the environment variable \"SystemRoot\" must be set.\n");
            	throw new IOException("SystemRoot missing from environment.");
            }
        }
       
        mProcess = serverLauncher.launch(cpu, workingDirectory, environment);
       
        mSessionProcess = new SeeCodeEngineProcess(mProcess,this); 
        
        final Thread acceptThread = Thread.currentThread();

        //
        //Arrange to terminate the session when the
        // engine terminates. Its seems like this should
        // be automatically done at a higher later since
        // a debug event if fired when the target process
        // dies.
        // We could wait for the termination event of the
        // IProcess but we can't confirm which target the
        // IProcess instance refers. So, we just spawn a
        // a thread to wait for the process to die.
        Thread waitThread = new Thread(new Runnable() {


            @Override
            public void run() {
                try {
                    mSessionProcess.waitFor();
                    if (!mShutdown){
                        onShutdown(); // force it.
                    }
                } catch (InterruptedException e) {
                    //shouldn't happen
                }
                try {
					mSessionProcess.closeStreams(); // make sure streams are closed so as to 
										// terminate threads that are reading them.
				} catch (IOException e) {
					SeeCodePlugin.log(e);
				}
                if (mWaitForAccept){
                    acceptThread.interrupt();
                }
                synchronized(mProcessLock){
                    mProcessDied = true;
                    mProcessLock.notifyAll();
                }
//                stdoutReader.terminate();
//                stderrReader.terminate();
                // System.out.println("Engine process died; mTarget.isTerminated = " + (mTarget==null?"NULL":""+mTarget.isTerminated()));     
                // Terminate the threads that are reading the engine's stdout and stderr
                // streams. CR1860
                closeInputThreads();
                //If we in the process of shutting down, then the SeeCodeDebug
                // plugin may have already shutdown. If that's the case, we
                // need to let this thread die immediately because class loader
                // for this plugin is no longer active.
                boolean forceTermination = false;
                synchronized (Session.this){
                    if (!mShutdownPending && !mShutdown || (mTargets != null && hasUnterminatedTarget())) {
                        forceTermination = true;
                    }
                }
                if (forceTermination){
                    //System.out.println("Forced termination");
                    if (mTargets != null
                            && !mTargets[0].isTerminated()) {
                        for (Target t: mTargets){
                            if (!t.isTerminated()){
                                t.fireTerminateEvent();
                            }
                        }
                    }
                    if ( mTargets != null) {
                        mEngineObserver.engineShutdown(mTargets[0].getEngineInterface());
                        // The target process will normally already be
                        // terminated, but just in case the engine crashed
                        // we need to force its termination in so far
                        // as the GUI is concerned.
                        for (Target target: mTargets){
                            target.getProcess().destroy();
                        }
                    }
                }
            }
        }, "EngineShutdownWaiter");
        waitThread.setDaemon(true);
        mProcessDied = false;
        waitThread.start();
        //Check if engine process did not get off the ground.
        synchronized(mProcessLock){
            if (!mProcessDied) try {
                mProcessLock.wait(100); // give it a chance to fail
            } catch (InterruptedException e1) {
                //OKAY to interrupt
            }
        }
        if (mProcessDied)
            throw new EngineException("Engine server terminated prematurely");
        
        mInputStream = new EngineInputStreamWrapper(mProcess.getInputStream(),"stdout");
        mErrorStream = new EngineInputStreamWrapper(mProcess.getErrorStream(),"stderr");
       
        // 1 minute timeout unless engine is to hit breakpoint for debugging.
        transport.setAcceptTimeout(engineToInvokeBreak()?300*1000:
                SeeCodePlugin.getDefault().getPreferences().getInt(ISeeCodeConstants.PREF_REQUEST_LAUNCH_TIMEOUT,
                            ISeeCodeConstants.DEF_REQUEST_LAUNCH_TIMEOUT)*1000);
        mWaitForAccept = true;
        try {
            transport.accept();
        } finally {
            mWaitForAccept = false;
        }

        ITypeFactoryFactory tff = new ITypeFactoryFactory(){

            @Override
            public ITypeFactory makeTypeFactory () {
                return new TypeFactory();
            }};
 
        int timeout = SeeCodePlugin.getDefault().getPreferences().getInt(ISeeCodeConstants.PREF_REQUEST_TIMEOUT,ISeeCodeConstants.DEF_REQUEST_TIMEOUT)*1000;
        IConnection connection = ConnectionFactory.makeConnection(transport,timeout);
        EngineInterface[] engines = EngineInterface.CreateRemote(connection,tff,SeeCodePlugin.getDefault().getCallbackRunner(),
                    timeout,
                    SeeCodePlugin.getDefault().getPreferences().getInt(ISeeCodeConstants.PREF_REQUEST_LOAD_TIMEOUT, ISeeCodeConstants.DEF_REQUEST_LOAD_TIMEOUT)*1000,
                    SeeCodePlugin.getDefault().getLoadTimeoutCallback(),
                    pid
                    );
        
        if (engines.length > 1 && engines[0].isCMPDControllerSupported()) {
            mCMPDController = CMPDFactory.createRemote(connection,timeout);
        }

        engines[0].setArgsFilePattern(getArgsPattern());
        for (EngineInterface e: engines){
            e.setEngineObserver(mEngineObserver);
        }
       

        return engines;

    }
    
    private boolean hasUnterminatedTarget(){
        for (ICDITarget t: mTargets){
            if (!t.isTerminated()) return true;
        }
        return false;
    }
    
    /**
     * Wait for the engine process to terminate and
     * return true if it did.
     * @param timeout timeout in milliseconds.
     * @return true if engine process terminated before
     * timeout expired.
     */
    boolean waitForProcessDeath(int timeout){
        synchronized(mProcessLock){
            if (!mProcessDied){
                try {
                    mProcessLock.wait(timeout);
                } catch (InterruptedException e) {
                    //Okay to interrupt
                }
            }
        }
        return mProcessDied;
    }

    /**
     * Return true if the engine process is to be launched
     * in such as way as to be attachable to the JDT debugger.
     * @return true if the engine process is to be attachable to
     * the JDT debugger.
     */
    private boolean engineToBeDebuggerAttachable() {
        return "true".equals(Platform.getDebugOption(SeeCodeDebugger.ENGINE_PLUGIN_ID+"/serverDebugEnabled"));
    }
    
    private boolean engineToInvokeBreak(){
        return "true".equals(Platform.getDebugOption(SeeCodeDebugger.ENGINE_PLUGIN_ID+"/server/debugbreak"));
    }

    private File getWorkingDirectory(ILaunchConfiguration config)
            throws CoreException {
        IPath path = CDebugUtils.getWorkingDirectoryPath(config);
        //if path is null - there is no user setting directory
        if (path != null) {
            return path.toFile();
        }
        if (mProject != null) {
            return mProject.getLocation().toFile();
        }
        //for NoProject selection we will use ELF_DIRECTORY for default working directory
        if(mIsNoProject){
        	if(mNoProjectSessionDir != null){
        	    path = new Path(mNoProjectSessionDir);
        		return path.toFile();
        	}
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getTargets()
     */
    @Override
    public ICDITarget[] getTargets() {
        return mTargets;
    }
    
    @Override
    public String getSeeCodeInstallationDirectory() {
    	try {
			return SeeCodeOptions.computeSCDIR(mLaunch.getLaunchConfiguration());
		} catch (ConfigurationException e) {
			return null;
		} catch (CoreException e) {
			return null;
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setAttribute(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setAttribute(String key, String value) {
        mProps.setProperty(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getAttribute(java.lang.String)
     */
    @Override
    public String getAttribute(String key) {
        return mProps.getProperty(key);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getEventManager()
     */
    @Override
    public ICDIEventManager getEventManager() {
        return mEventManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getConfiguration()
     */
    @Override
    public ICDISessionConfiguration2 getConfiguration() {
        return mCDIConfig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#terminate()
     */
    @Override
    public void terminate() throws CDIException {
        try {
            //System.out.println("Session::terminate()");
            //Due to the way termination events are
            // poliferated, we can get called more than
            // once from the same event packet. 
            // Check for this because the second time
            // may be to a disconnected engine!
        	boolean shutdownPending;
        	synchronized(this) {
        		shutdownPending = mShutdownPending;
                mShutdownPending = true;      	
        	}
        	if (!shutdownPending) {
            
                // The engineShutdown event will call
                // "shutdown()", which, in turn, will
                // make sure the engine process is terminated.
                //System.out.println("Calling Engine::shutdown()");
                for (Target t: mTargets){
                    if (!t.isTerminated()){
                        t.terminate();
                    }
                }
                //NOTE: shutdown on any engine interface shuts them all down!
                if (mTargets != null && mTargets.length > 0)
                    mTargets[0].getEngineInterface().shutdown();
            }
        } catch (EngineDisconnectedException e){
            //  Engine got ahead of us and shut itself down.
            // We need to accomodate that.
            for (Target t: mTargets){
                mEngineObserver.engineShutdown(t.getEngineInterface()); // force it!
            }
            mProcess.destroy(); // Just in case.
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            // Engine didn't shutdown -- likely because
            // it seg faulted or something like that.
            // To get the GUI windows to acknowledge that
            // the debugger went away, we must force it!
            for (Target t: mTargets){
                mEngineObserver.engineShutdown(t.getEngineInterface()); // force it!
            }
            mProcess.destroy(); // just in case engine is hung
            throw new CDIException(e.getMessage());
        } catch (RuntimeException e){
            //Don't leave engine running as an orphan if
            // an error occurs
            for (Target t: mTargets){
                mEngineObserver.engineShutdown(t.getEngineInterface()); // force it!
            }
            mProcess.destroy();
            throw e;
        }
    }

    boolean isShutdown() {
        return mShutdown;
    }
    
    @Override
    public boolean isTerminated() {
        return isShutdown();
    }
    
    protected  void fireDisposeEvent(){
        if (mDisposeListeners != null && mDisposeListeners.size() > 0){
            // Make copy; listeners may be removing themselves.
            for (ISessionDisposeListener l: new ArrayList<ISessionDisposeListener>(mDisposeListeners)){
                l.onSessionDisposed(this);
            }
        }
    }

    /**
     * Called when the engine has sent
     * {@link EngineObserver#engineShutdown(EngineInterface) engineshutdown}
     * event.
     *  
     */
    synchronized void onShutdown () {
        if (!mShutdown) {
            try {
                if (mTargets != null) {
                    for (Target t : mTargets) {
                        if (!t.isTerminated()) {
                            t.fireTerminateEvent();
                        }
                    }
                }
                mShutdown = true;
                fireDisposeEvent();
                if (mTargets != null) {
                    for (Target t : mTargets) {
                        t.getEngineInterface().onShutdown();
                    }
                }
            }
            finally {
            	if (mProcessDied) {
					unwireStuff();
				} else {
					//Spawn a thread to wait for debugger process to die then
					// unwire everything.
					Thread waitForShutdownThread = new Thread("WaitForShutdown") {
						@Override
						public void run() {
							// Wait for the process to die; kill it if it
							// doesn't
							// die in reasonable time (10 seconds)
							if (!waitForProcessDeath(10 * 1000))
								mSessionProcess.destroy();
							unwireStuff();
						}
					};
					waitForShutdownThread.setDaemon(true);
					waitForShutdownThread.start();
				}
            }
        }
    }
    
    public boolean isShuttingDown(){
        return mShutdownPending || mShutdown;
    }
    
    /**
     * This method is called when the SeeCode
     * plugin it being shutdown while debug sessions
     * exist.
     */
    @Override
	public void forceEmergencyShutdown() {
        boolean doit = false;
        synchronized (this) {
            if (!mShutdown) {
                if (!mShutdownPending) {
                    mShutdownPending = true;
                    doit = true;
                }
            }
        }
        if (doit) {
            try {
                for (Target t: mTargets)
                    t.terminate();
            } catch (CDIException e) {
                //ignore
            }
            try {
                for (Target t: mTargets){
                    t.getEngineInterface().shutdown();
                }
            } catch (EngineException e) {
                //Ignore errors. The engine may
                // have already been in the process
                // of shutting down.
            }
        }
        onShutdown();
        //Delay for engine to actually shutdown
        // so that it isn't firing events after
        // the SeeCode plugin is stopped.
        this.waitForProcessDeath(10000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
     */
    @Override
    public Process getSessionProcess() throws CDIException {
        return mSessionProcess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
     */
    @Override
    public ICDISession getSession() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
        if (adapter.isInstance(this))
            return this;
        if (adapter == ILaunch.class){
            return mLaunch;
        }
        if (adapter == IProject.class){
            return mProject;
        }
        if (adapter == ICDITarget.class && mTargets.length > 0){
            return mTargets[0];
        }
        if (adapter == EngineInterface.class && mTargets.length > 0){
            return mTargets[0].getEngineInterface();
        }
        if (adapter == ICMPDController.class) {
            return mCMPDController;
        }
        return null;
    }
      
    @Override
    public void updateViews(){
        for (Target t: mTargets)
            t.updateViews();
    }
    
    private void handleRestartedEvent(ICDIRestartedEvent e){
        //During a restart, the engine closes the sockets that drive the Terminal view(s), and
        // then attempt to re-connect. Reconnect. We may have a timing problem here if
        // the engine gets ahead of us on this.
        try {
            if (e.getSource() == mTargets[mTargets.length-1])
                SeeCodePlugin.getDefault().reconnectTermSimViews(this);
        }
        catch (Exception e1) {
            SeeCodePlugin.log(e1);
        }
    }

    @Override
    public void handleDebugEvents (ICDIEvent[] events) {
        for (int i = 0; i < events.length; i++){
            ICDIEvent e = events[i];
            if (e instanceof ICDIRestartedEvent && e.getSource().getTarget().getSession() == this){
                handleRestartedEvent((ICDIRestartedEvent)e);
            }
        }      
    }

    @Override
    public void addSessionDisposeListener (ISessionDisposeListener listener) {
        if (mDisposeListeners == null){
            mDisposeListeners = new ArrayList<ISessionDisposeListener>();
        }
        mDisposeListeners.add(listener);
        
    }

    @Override
    public void removeSessionDisposeListener (ISessionDisposeListener listener) {
        if (mDisposeListeners != null){
            mDisposeListeners.remove(listener);
        }       
    }   
    
    /**
     * Return the stream that reads stdout from the engine process.
     * @return the stream that reads stdout from the engine process.
     */
    public InputStream getInputStream(){
        return mInputStream;
    }
    
    /**
     * Return the stream that reads stderr from the engine process.
     * @return the stream that reads stdout from the engine process.
     */
    public InputStream getErrorStream(){
        return mErrorStream;
    }
    
    /**
     * This is called from Session when the engine process has terminated.
     * Since the other end of the mInputStream and mErrorStream pipes originate
     * from there, we cannot safely close them until the engine process dies.
     * <P>
     * If we terminate them when the application does before the engine terminates,
     * then the engine could hang while flushing stdout (Microsoft C runtime does this).
     */
    private void closeInputThreads () {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mErrorStream != null) {
                mErrorStream.close();
            }
        }
        catch (IOException e) {
            //shouldn't happen
        }
    }

    @Override
    public boolean canResume () {
        for (Target t: mTargets){
            if (!t.isTerminated() && !t.isAllThreadsResumed())
                return true;
        }
        return false;
    }

    @Override
    public boolean canSuspend () {
        for (Target t: mTargets){
            if (!t.isTerminated() && !t.isAllThreadsSuspended())
                return true;
        }
        return false;
    }

    @Override
    public boolean isSuspended () {
        for (Target t: mTargets){
            if (!t.isTerminated() && t.isAllThreadsSuspended())
                return true;
        }
        return false;
    }

    @Override
    public void resume () throws DebugException {
        if (mCMPDController != null) {
            try {
                mCMPDController.resume();
            }
            catch (EngineException e) {
                throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
            }
        }
        else {
            for (Target t : mTargets) {
                if (!t.isTerminated() && !t.isAllThreadsResumed()) {
                    try {
                        t.resume();
                    }
                    catch (CDIException e) {
                        throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
                    }
                }
            }
        }
    }

    @Override
    public void suspend () throws DebugException {
        if (mCMPDController != null) {
            try {
                mCMPDController.suspend();
            }
            catch (EngineException e) {
                throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
            }
        }
        else {
            for (Target t : mTargets) {
                if (!t.isTerminated() && !t.isAllThreadsSuspended())
                    try {
                        t.suspend();
                    }
                    catch (CDIException e) {
                        throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
                    }
            }
        }
    }

    @Override
    public boolean canStepInto () {
        for (Target t: mTargets){
            if (!t.isTerminated() && !t.isAllThreadsResumed())
                return true;
        }
        return false;
    }

    @Override
    public boolean canStepOver () {
        for (Target t: mTargets){
            if (!t.isTerminated() && !t.isAllThreadsResumed())
                return true;
        }
        return false;
    }

    @Override
    public boolean canStepReturn () {
        for (Target t: mTargets){
            if (!t.isTerminated() && !t.isAllThreadsResumed())
                return true;
        }
        return false;
    }

    @Override
    public boolean isStepping () {
        for (Target t: mTargets){
            if (!t.isTerminated())
                if (!t.isSuspended() && t.getPendingRunState(0) != ICDIResumedEvent.CONTINUE){
                    return true;
                }
        }
        return false;
    }

    @Override
    public void stepInto () throws DebugException {
        if (mCMPDController != null) {
            try {
                mCMPDController.statementStep(false);
            }
            catch (EngineException e) {
                throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
            }
        }
        else {
            for (Target t : mTargets) {
                if (!t.isTerminated() && t.isSuspended()) {
                    try {
                        t.stepInto();
                    }
                    catch (CDIException e) {
                        throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
                    }
                }
            }
        }
    }

    @Override
    public void stepOver () throws DebugException {
        if (mCMPDController != null) {
            try {
                mCMPDController.statementStep(true);
            }
            catch (EngineException e) {
                throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
            }
        }
        else {
            for (Target t : mTargets) {
                if (!t.isTerminated() && t.isSuspended()) {
                    try {
                        t.stepOver();
                    }
                    catch (CDIException e) {
                        throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
                    }
                }
            }
        }
    }

    @Override
    public void stepReturn () throws DebugException {
        if (mCMPDController != null) {
            try {
                mCMPDController.stepOut();
            }
            catch (EngineException e) {
                throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
            }
        }
        else {
            for (Target t : mTargets) {
                if (!t.isTerminated() && t.isSuspended()) {
                    try {
                        t.stepReturn(0);
                    }
                    catch (CDIException e) {
                        throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
                    }
                }
            }
        }
    }

    @Override
    public boolean canRestart () {
        for (Target t: mTargets){
            if (!t.isTerminated()) return true;
        }
        return false;
    }

    @Override
    public void restart () throws DebugException {
        /*if (false && mCMPDController != null) { // doesn't work
            try {
                mCMPDController.restart();
            }
            catch (EngineException e) {
                throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
            }
        }
        else */{
            // Old way of doing it.
            for (Target t : mTargets) {
                if (!t.isTerminated())
                    try {
                        t.restart();
                    }
                    catch (CDIException e) {
                        throw new DebugException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
                    }
            }
        }
    }

    /**
     * Called when session process has terminated.
     *
     */
	private void unwireStuff() {
		mEventManager.shutdown();
		killReaders();
		SeeCodePlugin.getDefault().removeSession(Session.this);
	}
	
	
}
