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
package com.arc.seecode.display.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.Display;

import com.arc.mw.util.StringUtil;
import com.arc.seecode.display.Factory;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;
import com.arc.seecode.display.ISeeCodeTextViewerFactory;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IWindow;

/**
 * @author David Pickens
 */
public class Test1 {

    public static void main(String[] args) throws IOException {
        Display display = new Display();
        InputStream fileIn = Test1.class.getResourceAsStream("QUEENS.TXT");
        if (fileIn == null) {
            throw new IOException("can't find QUEENS.TXT");
        }
        String lines[] = readLines(fileIn);
        IComponentFactory widgetFactory = new com.arc.widgets.internal.swt.ComponentFactory(
                display);
        ISeeCodeTextViewerFactory factory = Factory
                .createTextDisplayViewFactory(widgetFactory,null,null,null,null);
        IWindow f = widgetFactory.makeFrame(null);
        MySeeCodeTextViewerCallback callback = new MySeeCodeTextViewerCallback(
                widgetFactory, lines);
        ByteArrayInputStream in = new ByteArrayInputStream(PROPS.getBytes());
        Properties props = new Properties();
        props.load(in);
        ISeeCodeTextViewer viewer = factory.createDisplay(0, props, f
                        .getContents(), callback);
        callback.setViewer(viewer);

        f.setSize(500, 700);
        f.open();
        widgetFactory.enterDispatchLoop(f);
    }

    static String[] readLines(InputStream in) throws IOException {
        BufferedReader bufin = new BufferedReader(new InputStreamReader(in));
        List<String> list = new ArrayList<String>();
        while (true) {
            String line = bufin.readLine();
            if (line == null)
                break;
            line = addHighlights(detab(line));
            list.add(line);
        }
        return list.toArray(new String[list.size()]);
    }

    private static String detab(String s) {
        int i = s.indexOf('\t');
        if (s.indexOf('\t') >= 0) {
            StringBuffer buf = new StringBuffer(s.length() + 50);
            int last = 0;
            while (i >= 0) {
                buf.append(s.substring(last, i));
                do {
                    buf.append(' ');
                } while (buf.length() % 8 != 0);
                last = i + 1;
                i = s.indexOf('\t', last);
            }
            buf.append(s.substring(last));
            return buf.toString();
        } else
            return s;
    }

    private static String addHighlights(String s) {

        int i = s.indexOf("if");
        int len = 2;
        if (i < 0) {
            i = s.indexOf("while");
            len = 5;
        }
        if (i >= 0) {
            StringBuffer buf = new StringBuffer(s.length());
            buf.append(addHighlights(s.substring(0, i)));
            buf.append('\u800E');
            buf.append(s.substring(i, i + len));
            buf.append('\u8003');
            buf.append(addHighlights(s.substring(i + len)));
            return buf.toString();
        }
        return s;
    }

    static String PROPS = " hide=0\n"
            + "lines=15\n"
            + "kind=source\n"
            + "threadlock=1\n"
            + "profiling=0=kind=button\\n label=icnt\\n id=profile\\n tip=Instruction counts\\n doccontents=This button enables or disables a column displaying the sum of the Instruction counts for each address in the displayed line.\\n \n 1=kind=button\\n label=LIMM_cnt\\n id=profile\\n tip=LIMM counts\\n doccontents=This button enables or disables a column displaying the sum of the LIMM counts for each address in the displayed line.\\n \n 2=kind=button\\n label=killed\\n id=profile\\n tip=Killed instruction counts\\n doccontents=This button enables or disables a column displaying the sum of the Killed instruction counts for each address in the displayed line.\\n \n 3=kind=button\\n label=delay_killed\\n id=profile\\n tip=Delay slot killed instruction counts\\n doccontents=This button enables or disables a column displaying the sum of the Delay slot killed instruction counts for each address in the displayed line.\\n \n 4=kind=button\\n label=profint\\n id=profile\\n tip=Profiling interrupts\\n doccontents=This button enables or disables a column displaying the sum of the Profiling interrupts for each address in the displayed line.\\n \n 5=kind=button\\n label=Stmt cnt from icnt\\n id=profile\\n tip=Statement count from Instruction counts\\n doccontents=This button enables or disables a column displaying the sum of the Statement count from Instruction counts for each address in the displayed line.\\n \n 6=kind=button\\n label=Stmt cnt from icnt display\\n id=profile-ihist-5\\n tip=Sorted display for Statement count from Instruction counts\\n doccontents=Create an instruction history display showing instructions sorted by Statement count from Instruction counts.\\n \n 7=kind=button\\n label=icnt display\\n id=profile-ihist-6\\n tip=Sorted display for Instruction counts\\n doccontents=Create an instruction history display showing instructions sorted by Instruction counts.\\n \n 8=kind=button\\n label=LIMM_cnt display\\n id=profile-ihist-7\\n tip=Sorted display for LIMM counts\\n doccontents=Create an instruction history display showing instructions sorted by LIMM counts.\\n \n 9=kind=button\\n label=killed display\\n id=profile-ihist-8\\n tip=Sorted display for Killed instruction counts\\n doccontents=Create an instruction history display showing instructions sorted by Killed instruction counts.\\n \n A=kind=button\\n label=delay_killed display\\n id=profile-ihist-9\\n tip=Sorted display for Delay slot killed instruction counts\\n doccontents=Create an instruction history display showing instructions sorted by Delay slot killed instruction counts.\\n \n B=kind=button\\n label=profint display\\n id=profile-ihist-10\\n tip=Sorted display for Profiling interrupts\\n doccontents=Create an instruction history display showing instructions sorted by Profiling interrupts.\\n \n C=user_gui=<component=menu name=Profiling   <component=menu_item name=\"icnt\"  on_select=0 checkbox=1> <component=menu_item name=\"LIMM_cnt\"  on_select=1 checkbox=1> <component=menu_item name=\"killed\"  on_select=2 checkbox=1> <component=menu_item name=\"delay_killed\"  on_select=3 checkbox=1> <component=menu_item name=\"profint\"  on_select=4 checkbox=1> <component=menu_item name=\"Stmt cnt from icnt\"  on_select=5 checkbox=1><component=menu_separator >  <component=menu_item name=\"Stmt cnt from icnt display\"  on_select=\"profile-ihist-5\" > <component=menu_item name=\"icnt display\"  on_select=\"profile-ihist-6\" > <component=menu_item name=\"LIMM_cnt display\"  on_select=\"profile-ihist-7\" > <component=menu_item name=\"killed display\"  on_select=\"profile-ihist-8\" > <component=menu_item name=\"delay_killed display\"  on_select=\"profile-ihist-9\" > <component=menu_item name=\"profint display\"  on_select=\"profile-ihist-10\" > > \\n kind=user_gui\\n \n"
            + "infinite_slider=0\n"
            + "guic= \"button:Dis:Show corresponding disassembly:showdis\" \"button:stackup:Goto caller stack frame:stackup\" \"button:stackdown:Goto callee stack frame:stackdown\"\n";

    static class MySeeCodeTextViewerCallback implements
            ISeeCodeTextViewerCallback {
        //private IComponentFactory mWidgetFactory;

        private String[] mLines;

        private int mTopLine = 0;
        private int mOldTopLine = 0;

        private int mLineCount;

        private ISeeCodeTextViewer mViewer;
        private int _pc;
        private int _selectedLine;
        private boolean[] _bkpts;

        MySeeCodeTextViewerCallback(IComponentFactory f, String lines[]) {
            //mWidgetFactory = f;
            mLines = lines;
            _bkpts = new boolean[lines.length];
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#sendValueUpdate(com.arc.seecode.display.ISeeCodeTextViewer,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public void sendValueUpdate(ISeeCodeTextViewer d, String propertyName,
                String value) {
            sendValueUpdate(d, propertyName, value, -1);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#sendValueUpdate(com.arc.seecode.display.ISeeCodeTextViewer,
         *      java.lang.String, java.lang.String, int)
         */
        @Override
        public boolean sendValueUpdate(ISeeCodeTextViewer d,
                String propertyName, String value, int timeout) {
            System.out.println("sendValueUpdate(\"" + propertyName + "\",\""
                    + value + "\"," + timeout + ")");
            if (propertyName.equals("lines")) {
                String tokens[] = StringUtil.stringToArray(value);
                int area = Integer.parseInt(tokens[0]);
                int width = Integer.parseInt(tokens[1]);
                mLineCount = width != 0 ? area / width : 0;
                sendBackLines(mTopLine, mLineCount);
            } else if (propertyName.equals("scroll")) {
                String tokens[] = StringUtil.stringToArray(value);
                int amount = Integer.parseInt(tokens[0]);
                mOldTopLine = mTopLine;
                mTopLine = Math.max(0, mTopLine + amount);
                sendBackLines(mTopLine, mLineCount);
            }
            else if (propertyName.equals("sel_line")){
                String tokens[] = StringUtil.stringToArray(value);
                _selectedLine = mTopLine + Integer.parseInt(tokens[0]);
            } 
            else if (propertyName.equals("double_click") ||
                    propertyName.equals("breakpoint")){
                if (_selectedLine >= 0){
                    int code;
                    if (_bkpts[_selectedLine]){
                        code = 8;
                    }
                    else code = 2;
                    mViewer.setHighlight(_selectedLine-mTopLine,code);
                    _bkpts[_selectedLine] = !_bkpts[_selectedLine];
                    mViewer.refresh();
                }
            }
            return true;
        }

        private void sendBackLines(int first, int count) {
            if (mViewer != null) {
                mViewer.setVerticalScroller(first, count, 0, mLines.length, 1);
                for (int i = 0; i < count; i++) {
                    if (first + i >= mLines.length)
                        break;
                    String num = "" + (first + i + 1);
                    while (num.length() < 4)
                        num += ' ';
                    mViewer.setLine(i, num + " " + mLines[first + i]);
                    if (_bkpts[mOldTopLine+i] != _bkpts[first+i]){
                        int code;
                        if (_bkpts[first+i])
                            code = 2;
                        else code = 8;
                        mViewer.setHighlight(i,code);
                    }
                }
                if (_pc < count)
                    mViewer.setHighlight(_pc,4);
                if (20 >= first && 20 < first+count){
                    mViewer.setHighlight(20-first,0);
                    _pc = 20-first;
                }
                mViewer.refresh();
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#onClose(com.arc.seecode.display.ISeeCodeTextViewer)
         */
        @Override
        public void onClose(ISeeCodeTextViewer d) {
            System.out.println("onClose");

        }

        void setViewer(ISeeCodeTextViewer v) {
            mViewer = v;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#createDisplayMenuFor(com.arc.seecode.display.ISeeCodeTextViewer)
         */
        @Override
        public MenuDescriptor createDisplayMenuFor(ISeeCodeTextViewer d) {
            MenuDescriptor m = new MenuDescriptor();
            m.addMenuItem("alpha", "Alpha", new MenuDescriptor.IActionObserver() {

                @Override
                public void actionPerformed(String arg0) {
                    System.out.println("Alpha clicked");

                }
            });
            m.addMenuItem("beta", "Beta", new MenuDescriptor.IActionObserver() {

                @Override
                public void actionPerformed(String arg0) {
                    System.out.println("Beta clicked");

                }
            });
            m.addSeparator();
            m.addCheckBoxMenuItem("one", "One",
                    new MenuDescriptor.ICheckBoxObserver() {

                        @Override
                        public void selectionChanged(String name, boolean value) {
                            System.out.println("Checkbox One set to " + value);

                        }
                    }, true, null);
            m.addCheckBoxMenuItem("two", "Two",
                    new MenuDescriptor.ICheckBoxObserver() {

                        @Override
                        public void selectionChanged(String name, boolean value) {
                            System.out.println("Checkbox Two set to " + value);

                        }
                    }, true, null);
            m.addCheckBoxMenuItem("three", "Three",
                    new MenuDescriptor.ICheckBoxObserver() {

                        @Override
                        public void selectionChanged(String name, boolean value) {
                            System.out
                                    .println("Checkbox Three set to " + value);

                        }
                    }, true, null);
            m.addSeparator();
            m.setRadioListener("COLOR",
                    new MenuDescriptor.IRadioButtonObserver() {

                        @Override
                        public void selectionChanged(String groupName,
                                String value) {
                            System.out.println(groupName + " set to " + value);

                        }
                    });
            m.addRadioMenuItem("red", "Red", "COLOR", "red", true);
            m.addRadioMenuItem("green", "Green", "COLOR", "green", false);
            m.addRadioMenuItem("blue", "Blue", "COLOR", "blue", false);
            return m;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#internalError(com.arc.seecode.display.ISeeCodeTextViewer,
         *      java.lang.String, java.lang.Throwable)
         */
        @Override
        public void internalError(ISeeCodeTextViewer viewer, String message,
                Throwable t) {
            System.err.println(message);
            if (t != null)
                t.printStackTrace(System.err);

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#notifyError(com.arc.seecode.display.ISeeCodeTextViewer,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public void notifyError(ISeeCodeTextViewer viewer, String message,
                String title) {
            System.err.println("notifyError: " + message);

        }

        @Override
        public boolean copyAllToClipboard (ISeeCodeTextViewer d) {
            // TODO Auto-generated method stub
            return false;
        }

    }
}
