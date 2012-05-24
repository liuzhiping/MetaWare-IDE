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


public class TestTable {

    /**
     * @todo davidp needs to add a method comment.
     * @param args
     */
    public static void main (String[] args) {
        IComponentFactory f;
        if (args.length == 0)
            f = new com.arc.widgets.internal.swt.ComponentFactory();
        else
            f = new com.arc.widgets.internal.swing.ComponentFactory();

        IWindow frame = f.makeFrame(null);
        frame.setTitle("Test Frame");
        final ITable table = f.makeTable(frame.getContents());
        table.setColumnCount(8);
        IColor blue = f.makeColor(0,0,255);
        for (int i = 0; i < 8; i+=2){
            table.setColumnForeground(i,blue);
            table.setColumnAlignment(i,ITable.Alignment.RIGHT);
        }
        //table.setColumnWidths(new int[]{50,50,50,50,50,50,50,50});
        table.addSelectionListener(new ITable.ISelectionListener(){

            @Override
            public void onTableItemDoubleClicked (int row, int column) {
                if ((column&1) == 0) table.setSelection(row,++column);
                table.setInput(row,column);              
            }

            @Override
            public void onTableItemSelected (int row, int column) {
                if ((column&1) == 0) table.setSelection(row,column+1);
                System.out.println("" + row + "," +column + " selected");
                
            }

            @Override
            public String onNewValueEntered (int row, int column, String value) {
                System.out.println("" + row + "," + column + "  changed to \"" + value + "\"");
                return value;
            }});
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 10; j++){
                table.setItem(j,i,"" + j + "," + i);
            }
        }
//      frame.layout();
        frame.pack();
        frame.open();
        frame.addWindowObserver(new IWindow.IObserver(){

            @Override
            public void windowActivated (IWindow w) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void windowClosed (IWindow w) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void windowClosing (IWindow w) {
                System.exit(0);
                
            }

            @Override
            public void windowDeactivated (IWindow w) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void windowDeiconified (IWindow w) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void windowIconified (IWindow w) {
                // @todo Auto-generated method stub
                
            }});
        f.enterDispatchLoop(frame);
    }

}
