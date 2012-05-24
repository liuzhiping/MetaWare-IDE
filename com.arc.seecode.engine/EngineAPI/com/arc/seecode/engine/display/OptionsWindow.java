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
package com.arc.seecode.engine.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.mw.util.IPropertyMap;
import com.arc.seecode.engine.config.OptFileResolver;
import com.arc.widgets.IButton;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;
import com.arc.widgets.IWindow;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IFileResolver;
import com.metaware.guihili.PropertyStorage;
import com.metaware.guihili.builder.Environment;

/**
 * Handles the parsing of a dynamic guihili specification that is to be be
 * formed into a modal dialog.
 * 
 * @author David Pickens
 */
class OptionsWindow {
    
    /**
     * 
     */
    private static final String RECORD = "record";
    /**
     * 
     */
    private static final String OPTIONS_NAME = "options_name";
    /**
     * 
     */
    private static final String DIALOG = "dialog";
    /**
     * 
     */
    private static final String FILE_PREFIX = "file_prefix";
    /**
     * 
     */
    private static final String OPTIONS_CODE = "options_code";
    private static final String OK_ENABLED = "OK_ENABLED";
    private static final String ARG_ACTION = "ARG_ACTION";
    private IComponentFactory mFactory;

    private boolean mOK_Pressed;

    /**
     * The directory where args and properties files are stored.
     */
    private IArgsFileLocator mLocator;
    private IWindow mDialog;
    private Object mDialogOwner;
	private String mSCDIR;

    /**
     * 
     * @param factory widget factory.
     * @param dialogOwner if any dialog is to be generated, this is its owner
     * (an instance of Swing's Window or SWT's Shell).
     * @param locator computes the args and property
     * file locations.
     * @param scdir the path to the SeeCode installation directory so that we can read the
     * guihili files.
     */
    OptionsWindow(IComponentFactory factory, Object dialogOwner, IArgsFileLocator locator,
    		String scdir) {
        mFactory = factory;
        mLocator = locator;
        mDialogOwner = dialogOwner;
        mSCDIR = scdir;
    }
    
    private static final Set<String> SPECIAL_PROPS = new HashSet<String>();
    static {
        SPECIAL_PROPS.add(OPTIONS_CODE);
        SPECIAL_PROPS.add(FILE_PREFIX);
        SPECIAL_PROPS.add(DIALOG);
        SPECIAL_PROPS.add(OPTIONS_NAME);
        SPECIAL_PROPS.add(RECORD);
    }

    /**
     * Construction the dialog from the properties and display it. When "OK"
     * selected, the callback runner is executed.
     * 
     * @param props
     *            the properties from which the dialog will be constructed.
     * @param run
     *            to be invoked when the dialog's OK button is pressed.
     */
    public void showDialog(Properties props, Runnable run, IHelpAssociator help) throws Exception {
        String guihili = props.getProperty(OPTIONS_CODE);
        if (guihili == null) {
            throw new IllegalArgumentException("Missing 'options_code'");
        }
        String filePrefix = props.getProperty(FILE_PREFIX, "");
        String optionsName = props.getProperty(OPTIONS_NAME,
                null);
        
        IWindow dialog = makeDialog();

        File propsFile = mLocator.computePropertiesFile(filePrefix);
        File argsFile = mLocator.computeArgsFile(filePrefix);
        mOK_Pressed = false;

        ActionListener okAction = makeOkAction(dialog);
        Gui gui = parseGuihili(guihili, dialog.getContents(),
                okAction);
        if (propsFile.exists()) {
            readPropertiesInto(propsFile, gui);
        }
        //Client may have explicitly set Guihili properties...
        for (Map.Entry<Object,Object> entry: props.entrySet()){
            try {
                String name = (String)entry.getKey();
                if (!SPECIAL_PROPS.contains(name)) {
                    gui.setProperty(name,entry.getValue());
                }
            } catch (PropertyVetoException e) {                
            }           
        }
        String title = optionsName;
        if (title == null){
            title = (String)gui.getProperty("title");
        }
        if (title == null){
            title = "Configure";
        }
        mDialog = dialog;
        Object s = gui.getProperty("OK_ON_ENTER");
        boolean press_OK_on_enter =  s != null && s.toString().startsWith("1");
       
        dialog.setTitle(title);
        makeButtons(dialog.getContents(),gui.getAction(Gui.GEN_ARG_ACTION),gui, press_OK_on_enter);
        dialog.pack();
       
        if (help != null){
            String doc = props.getProperty("docTitle");
            if (doc != null){
                help.associateHelp(dialog,doc);
            }
            else
            if (filePrefix.length() > 0){
                help.associateHelp(dialog,filePrefix);
            }
        }
        dialog.open();

        //Here we wait until the user dismisses the dialog.
        // If "OK" pressed, then we write out the properties
        // and args file...
        if (mOK_Pressed) {
            writePropertiesFile(gui, propsFile);
            writeArgsFile(gui, argsFile);
            run.run();
        }
    }
    
    /**
     * Deal when to enable the OK button for the File/Memory/Fill dialog.
     * It is derived from the property settings the debugger's "guihili" file: "memop.opt".
     * <P>
     * Guihili isn't sophisticated enough to do this on its own.
     * @param map
     * @return
     */
    private boolean enableFileMemDialog(IPropertyMap map){
    	String kase = (String)map.getProperty("fillmem");
    	if (kase == null) return true; // Shouldn't happen
    	if (kase.equals("file2mem")){
    		String addr = (String)map.getProperty("addr_file2mem");
    		String file = (String)map.getProperty("from_file");
    		if (!isEmpty(addr)){
    			 if (file != null){
    				 File f = new File(file);
    				 return f.exists() && f.isFile();
    			 }
    		}
    		return false;
    	}
    	if (kase.equals("mem2file")){
    		String addr = (String)map.getProperty("addr_mem2file");
    		String file = (String)map.getProperty("to_file");
    		String len = (String)map.getProperty("len1");
    		if (!isEmpty(addr) && !isEmpty(len) && !isEmpty(file)){
    			return true;
    		}
    		return false;
    	}
    	if (kase.equals("fillmem")){
    		String addr = (String)map.getProperty("fill_addr");
    		String len = (String)map.getProperty("len2");
    		String byteValue = (String)map.getProperty("f_byte_value");
    		String stringValue = (String)map.getProperty("f_string_value");
    		String which = (String)map.getProperty("which_fill");
    		if ( !isEmpty(addr) && !isEmpty(len) && !isEmpty(which)){
    			if (which.endsWith("byte")){
    				return !isEmpty(byteValue);
    			}
    			return !isEmpty(stringValue);
    		}
    		return false;
    	}
    	return true; // Must not be a Fill/Memory dialog; enable "OK" button
    }
    
    private static boolean isEmpty(String s){
    	return s == null || s.trim().length() == 0;
    }
    
    private static boolean isTrue(Object v) {
        if (v == null)
            return false;
        if (v instanceof Boolean)
            return ((Boolean) v).booleanValue();
        if (v instanceof Integer)
            return ((Integer) v).intValue() != 0;
        if (v instanceof String) {
            String s = v.toString();
            return s.equals("true") || s.equals("1") || s.equals("t");
        }
        return false;
    }

    
    private void applyEnablementSetter(final IButton button, final Gui gui){
    	if (gui.getProperty("fillmem") != null) {
    		// Fill/Memory dialog. 
    		gui.addPropertyChangeListener(new PropertyChangeListener(){

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					button.setEnabled(enableFileMemDialog(gui));
					
				}}); 
    		button.setEnabled(enableFileMemDialog(gui));
    	}
    	 gui.addPropertyChangeListener(OK_ENABLED, new PropertyChangeListener() {

             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 button.setEnabled(isTrue(evt.getNewValue()));

             }
         });
         button.setEnabled(gui.getProperty(OK_ENABLED) == null || isTrue(gui.getProperty(OK_ENABLED)));
    }
    
    private void makeButtons(IContainer parent, ActionListener okAction, Gui gui, boolean pressOKOnEnter){
        IContainer buttonPanel = mFactory.makeContainer(parent,IComponentFactory.ROW_STYLE);
        ILabel filler = mFactory.makeLabel(buttonPanel,"");
        filler.setHorizontalWeight(1.0);
        IButton ok = mFactory.makeButton(buttonPanel);
        ok.setText("OK");
        IButton cancel = mFactory.makeButton(buttonPanel);
        cancel.setText("Cancel");
        ok.addActionListener(okAction);
        if (pressOKOnEnter){
            mDialog.setDefaultButton(ok);
        }
        cancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();                
            }});
        applyEnablementSetter(ok,gui);
    }
    
    private void onOK(){
        mOK_Pressed = true;
        mDialog.dispose();       
    }
    
    private void onCancel(){
        mOK_Pressed = false;
        mDialog.dispose();
    }
    
    /**
     * Read the properties from the property file and
     * initialize the dialog widgets accordingly.
     * @param file file contaiing properties.
     * @param gui the "model" backing our dialog.
     * @throws IOException
     */
    private void readPropertiesInto(File file, Gui gui)
        throws IOException
        {
        if (!file.exists()) return;
        PropertyStorage.read(gui,file);
        try {
            // Clear this because it is appended to as
            // OK button pressed.
            gui.setProperty(ARG_ACTION, null);
            //HACK: an old anchronism - "ACTION" is
            // a argument list that is appeneded to each
            // time "OK" action is fired.
            gui.setProperty("ACTION", null);
        } catch (PropertyVetoException e) {
        }
    }
    
    private void writePropertiesFile(Gui gui, File file) throws IOException{
        PropertyStorage.write(gui.getPropertyNames(),gui,file,"Debugger engine properties");
    }

    /**
     * Make action listener for when OK button pressed.
     * 
     * @param dialog
     *            the dialog to be dismissed.
     * @return action listener for when OK button pressed.
     */
    private ActionListener makeOkAction(final IWindow dialog) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        };
    }

    private IWindow makeDialog() {
        final IWindow d = mFactory.makeDialog(mDialogOwner, true);
        d.addWindowObserver(new IWindow.IObserver() {

            @Override
            public void windowClosing(IWindow w) {
                d.dispose(); // cancel
            }

            @Override
            public void windowClosed(IWindow w) {
            }

            @Override
            public void windowActivated(IWindow w) {
            }

            @Override
            public void windowDeactivated(IWindow w) {
            }

            @Override
            public void windowIconified(IWindow w) {
            }

            @Override
            public void windowDeiconified(IWindow w) {
            }
        });
        return d;
    }

    private IFileResolver makeFileResolver() {
        return new OptFileResolver(mSCDIR);
    }

    private Gui parseGuihili(String guihili, IContainer parent,
            ActionListener okAction) throws SAXParseException, SAXException {
        IFileResolver resolver = makeFileResolver();
        IEnvironment env = makeEnvironment();
        Gui gui = new Gui(env, resolver);
        gui.setFrame(mDialogOwner);
        gui.setComponentFactory(mFactory);
        StringReader reader = new StringReader(guihili);
        InputSource input = new InputSource(reader);
        input.setSystemId("<string>.opt");
        gui.setParent(parent);
        gui.readXML(input);
        gui.addAction(Gui.GEN_ARG_ACTION, okAction);
        return gui;
    }

    private static IEnvironment makeEnvironment() {
        IEnvironment e = Environment.create();
        // e.putSymbol(...) ...
        return e;
    }
    
    @SuppressWarnings("unchecked")
	private void writeArgsFile(Gui gui, File file) throws IOException{
        List<Object> list = (List<Object>)gui.getProperty(ARG_ACTION);
        if (list == null){
            throw new IOException(ARG_ACTION + " not generated");
        }
        FileWriter fw = new FileWriter(file);
        PrintWriter out = new PrintWriter(fw);
        for (Object arg: list){
            out.println(arg);
        }
        out.close();
    }

}
