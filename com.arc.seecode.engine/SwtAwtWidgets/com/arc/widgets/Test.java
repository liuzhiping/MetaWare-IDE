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
package com.arc.widgets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;

class Test {
    public static void main(String args[]){
	IComponentFactory f;
	if (args.length > 0)
	    f = new com.arc.widgets.internal.swt.ComponentFactory();
	else
	    f = new com.arc.widgets.internal.swing.ComponentFactory();

	IWindow frame = f.makeFrame(null);
	frame.setTitle("Test Frame");

	ITabbedPane tp = f.makeTabbedPane(frame.getContents(), IComponentFactory.TABS_ON_TOP);
	//tp.layout();

	tp.addTab(make33Grid(f,tp),"3x3 Grid");
	tp.addTab(makeTextGrid(f,tp),"Text/Choice");
	tp.addTab(makeFileChooser(f,tp),"Filechooser");
	//frame.layout();
	frame.pack();
	frame.open();
	f.enterDispatchLoop(frame);
	}

    private static IContainer makeFileChooser(IComponentFactory f, ITabbedPane pane){
	IContainer panel = f.makeGridContainer(pane,2);
	panel.setHorizontalSpacing(5);
	IButton b = f.makeButton(panel);
	b.setText("Choose file");
	final IChoice choice = f.makeComboBox(panel,false);
	choice.getComponent();
	final IFileChooser fc = f.makeFileChooser(b.getComponent(),IComponentFactory.FILE_OPEN|
						    IComponentFactory.FILE_MULTI);
	b.addActionListener(
	    new ActionListener(){
		@Override
        public void actionPerformed(ActionEvent event){
		    fc.open();
		    File files[] = fc.getFiles();
		    if (files != null){
			choice.clear();
			for (int i = 0; i < files.length; i++)
			    choice.addItem(files[i].toString());
			}
		    }
		}
	    );
	return panel;
	}
    private static IContainer makeTextGrid(IComponentFactory f, ITabbedPane pane){
	IContainer panel = f.makeGridContainer(pane,2);
	panel.setVerticalSpacing(5);
	panel.setHorizontalSpacing(5);
	f.makeLabel(panel,"Choice:").getComponent();
	final IChoice choice = f.makeComboBox(panel,true);
	choice.addItem("One");
	choice.addItem("Two");
	choice.addItem("Three");
	choice.getComponent();
	choice.addActionListener(
			new ActionListener(){
			    @Override
                public void actionPerformed(ActionEvent event){
				System.out.println("Action for choice: " + choice.getText());
				}
			    });
	f.makeLabel(panel,"Text:").getComponent();
	final ITextField text = f.makeTextField(panel);
	text.setText("Enter change");
	text.getComponent();
	text.addActionListener(
			new ActionListener(){
			    @Override
                public void actionPerformed(ActionEvent event){
				System.out.println("Action for text: " + text.getText());
				}
			    });
	text.addTextListener(
			new TextListener(){
			    @Override
                public void textValueChanged(TextEvent event){
				System.out.println("text change: " + text.getText());
				}
			    });
	final IButton checkBox = f.makeCheckBox(panel);
	checkBox.setText("Check box");
	checkBox.addActionListener(
			new ActionListener(){
			    @Override
                public void actionPerformed(ActionEvent event){
				System.out.println("Check box selected: " + checkBox.isSelected());
				}
			    });
	return panel;
	}

    private static IContainer make33Grid(IComponentFactory f, ITabbedPane pane){
	IContainer panel = f.makeGridContainer(pane,3);
	panel.setVerticalSpacing(5);
	panel.setHorizontalSpacing(5);
/*
	ILabel label = f.makeLabel(c,"label at 0,0");
	label.setGridCell(0,0,2,1);
	label.getComponent();

	label = f.makeLabel(c,"label at 1,2");
	label.setGridCell(1,2,2,1);
	label.getComponent();
*/

        for (int i = 0; i < 3; i++){
	    for (int j = 0; j < 3; j++){
		IButton button = f.makeButton(panel);
		final String txt = "Button@" + i + "," + j;
		button.setText(txt);
		button.addActionListener( new ActionListener() {
		    @Override
            public void actionPerformed(ActionEvent event){
			System.out.println(txt + " clicked");
			}
		    });

		button.getComponent();
		}
	    }
	//panel.layout();
	return panel;
	}
    }
