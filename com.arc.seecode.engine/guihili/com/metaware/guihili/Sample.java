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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.Action;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IWindow;



/**
 * Guihili sample.
 */
public class Sample {
    /**
     * Invoke with guihili file.
     */
    public static void main(String args[]){
	if (args.length != 1){
	    System.err.println("Form is: java -Dgui={swt|swing} -jar guihili.jar <file>");
	    System.exit(1);
	    }
	String which = System.getProperty("gui");
	IComponentFactory factory = null;
	//Toggle.set("TRACE_ENABLE",true);
	if ("swt".equals(which))
	    factory = new com.arc.widgets.internal.swt.ComponentFactory();
	else
	    factory = new com.arc.widgets.internal.swing.ComponentFactory();
	try {
	    File file = new File(args[0]);
	    final IWindow f = factory.makeFrame(null);
	    f.setTitle(args[0]);
	    final Gui g = new Gui(new String[]{file.getParent(), file.getParentFile().getParent()});
	    g.setComponentFactory(factory);
	    g.setFrame(f.getComponent());
	    IContainer pane = factory.makeGridContainer(f.getContents(),1);
	    g.setParent(factory.makeGridContainer(pane,1));

	    // Add Lisp function for "run_driver" function.
	    g.addLispFunctions( new Sample() );

	    IEnvironment env = g.getEnvironment();
	    g.setProperty("whichOptions","machine");
	    env.putSymbolValue("TARG",System.getProperty("TARG","arc"));
	    env.putSymbolValue("HOST","windows");
	    env.putSymbolValue("SCDIR",file.getParent());
	    env.putSymbolValue("PROJECTSDIR",file.getParent());
	    env.putSymbolValue("CURRENT",file.getParent() + "/current.txt");
	    env.putSymbolValue("SC_TMPFILE","C:/temp/tmpfile");
	    env.putSymbolValue("SC_ARGFILE","C:/temp/argfile");
	    g.readXML(args[0]);
	    g.addPropertyChangeListener(
		new PropertyChangeListener() {
		    @Override
            public void propertyChange(PropertyChangeEvent event){
			System.out.println("Property " + event.getPropertyName() + 
				"=" + event.getNewValue());
			}
		    }
		);
	    Action okAction = g.addAction("OK",
		new ActionListener(){
		    @Override
            public void actionPerformed(ActionEvent a){
			System.out.println("OK selected");
			System.out.println("Arg list is: " + 
				g.getProperty("ARG_ACTION"));
			System.exit(0);
			}
		    }
		);

	    IContainer buttons = factory.makeContainer(
				pane,IComponentFactory.FLOW_STYLE);
	    IButton ok = factory.makeButton(buttons);
	    ok.setText("OK");
	    ok.addActionListener(okAction);
	    ok.setHorizontalAlignment(IComponent.FILL);
	    f.pack();
	    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
	    f.setLocation((s.width - f.getWidth())/2, 
			  (s.height - f.getHeight())/2);
	    f.open();
	    factory.enterDispatchLoop(f);
	    }
	catch(SAXParseException x){
	    System.err.println("At " + x.getSystemId() + ", line " + 
		x.getLineNumber());
	    Exception e = x.getException();
	    if (e != null)
		e.printStackTrace(System.err);
	    else
		x.printStackTrace(System.err);
	    }
	catch(SAXException x){
	    Exception e = x.getException();
	    if (e != null)
		e.printStackTrace(System.err);
	    else
		x.printStackTrace(System.err);
	    }
	catch(Exception x){
	    x.printStackTrace(System.err);
	    }
	}

    /**
     * Called by Lisp interpreter with reflection to process
     * "run_driver" command
     */
    public Object do_run_driver(List<Object> list, IEvaluator eval, IEnvironment env){
	try{
	    System.out.println("run_driver invoked: " + 
			eval.expandString((String)list.get(1),env));
	    }
	catch(Exception x){
	    System.err.println("Error in processing run_driver: " + x);
	    }
	return null;
	}

    }
