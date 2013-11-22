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
package com.arc.cdt.debug.seecode.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.mw.util.Cast;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;

public class MemorySearchView extends AbstractEngineBasedView {
    public static final String VIEW_ID = "com.arc.cdt.debug.seecode.ui.views.memsearch";

    private static final String DEFAULT_MASK = "0xFFFFFFFF";

    private static final String PREFIX = "memsearch.";

    private static final String KIND_KEY = PREFIX + "kind";

    private static final String INTEGER_SIZE_KEY = PREFIX + "size";

    private static final String INTEGER_VALUE_KEY = PREFIX + "intValue";

    private static final String INTEGER_MASK_KEY = PREFIX + "intMask";

    private static final String START_ADDRESS_KEY = PREFIX + "start";

    private static final String LENGTH_KEY = PREFIX + "length";

    private static final String INVERT_KEY = PREFIX + "invert";

    private static final String REVERSE_KEY = PREFIX + "reverse";

    private static final String COMBO_ITEM_KEY = PREFIX + "comboItems";
    private static final String CHAR_STRING_KEY = PREFIX + "charString";
    private static final String HEX_STRING_KEY = PREFIX + "hexString";

    private static final int MAX_COMBO_ITEM_COUNT = 6; // max history in combo
                                                        // boxes

    private int mKind = INTEGER_KIND;

    private static final int INTEGER_KIND = 0;

    private static final int CHAR_STRING_KIND = 1;

    private static final int HEX_STRING_KIND = 2;

    private Button mKindButtons[] = new Button[3];

    private Map<String, String[]> mComboItems = new HashMap<String, String[]>();

    private int[] mIntegerSize = { 4 };

    private String[] mIntegerValue = { "0x1234" };

    private String[] mIntegerMask = { DEFAULT_MASK };

    private int[] mInvertSearch = { 0 };

    private String[] mStartAddress = { "0x10000" };

    private String[] mLength = { "0x1000" };

    private int[] mReversedSearch = { 0 };

    private String mCharString[] = { "an ASCII string" };

    private String mHexString[] = { "0123456789ABCDEF" };

    private StackLayout mCardLayout;

    private Control[] mCards = new Control[3];

    private Composite mCardPanel;

    private String mSearchAgain = "mems ,gui";

    public MemorySearchView() {
        super();

    }

    @Override
    protected String getHelpID() {
        return IContextHelpIds.PREFIX + "memsearch_dialog";
    }

    @Override
    protected String computeViewTitle(EngineInterface engine) {
        return "Memory Search";
    }

    @Override
    protected void initEngineView(EngineInterface engine, Composite control) {
        restoreState();
        control.setLayout(new FillLayout());
        ScrolledComposite scroller = new ScrolledComposite(control,
                SWT.V_SCROLL|SWT.H_SCROLL);
        scroller.setExpandVertical(true);
        scroller.setData("name","memsearch.scroller"); // For GUI tester
        Composite parent = new Composite(scroller, SWT.NONE);
        scroller.setContent(parent);
        parent.setLayout(new GridLayout(1,false));
        makeKindPanel(parent);

        //drawSeparator(parent);
        
        makeSearchForPanel(parent);

        //drawSeparator(parent);

        makeAddressAndLengthPanel(parent);

        //drawSeparator(parent);
        
        makeDirectionPanel(parent);
        
        drawSeparator(parent);

        makeSearchButtons(parent);

        setKind(mKind); // must be called last because it tweaks card layout

        scroller.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        this.wireInHelp(control);

    }

    /**
     * Map the "kind" radio buttons in to the parent using
     * the FormLayout.
     * @param parent
     * @return the left most bottom control
     */
    private Control makeKindPanel(Composite parent) {
        Composite panel = new Composite(parent,SWT.NONE);
        panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        RowLayout layout = new RowLayout();
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        panel.setLayout(layout);
        
        Label label = new Label(panel, SWT.CENTER);
        label.setText("Search for:");
        makeKindButton(panel, "Integer value", INTEGER_KIND);
        makeKindButton(panel, "Char string", CHAR_STRING_KIND);
        makeKindButton(panel, "Hex string", HEX_STRING_KIND);
        return panel;
    }

    private Button makeKindButton(Composite buttonPanel, String label,
            final int value) {
        final Button b = new Button(buttonPanel, SWT.RADIO);
        b.setText(label);
        b.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (b.getSelection()) {
                    setKind(value);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        mKindButtons[value] = b;
        return b;
    }

    private Control drawSeparator(final Composite parent) {
        final Canvas c = new Canvas(parent, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                int width = wHint == SWT.DEFAULT ? 22 : wHint;
                int height = hHint == SWT.DEFAULT ? 2 : hHint;
                return new Point(width, height);
            }
        };
        c.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;
                if (e.width > 10) {
                    Color save = gc.getForeground();
                    gc.setForeground(parent.getDisplay().getSystemColor(
                            SWT.COLOR_GRAY));
                    gc.drawLine(e.x + 5, 0, e.width - 10, 0);
                    gc.setForeground(save);
                }

            }
        });
        c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return c;
    }

    private void makeSearchForPanel(Composite parent) {
        Group borderedPanel = new Group(parent,SWT.NONE);
        borderedPanel.setLayout(new FillLayout());
        borderedPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite cards = new Composite(borderedPanel, SWT.NONE);
        mCardLayout = new StackLayout();
        mCardLayout.marginWidth = 1;
        mCardLayout.marginHeight = 1;
        mCardPanel = cards;
        cards.setLayout(mCardLayout);
        mCards[INTEGER_KIND] = makeSearchForIntegerPanel(cards);

        mCards[CHAR_STRING_KIND] = makeSearchForStringPanel(cards,
                "A char string", mCharString);

        mCards[HEX_STRING_KIND] = makeSearchForStringPanel(cards,
                "A hex string", mHexString);
    }

    private Composite makeSearchForIntegerPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        panel.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        panel.setLayout(gridLayout);

        Control radioPanel = makeRadioButtons(panel, "Integer size:", new String[] { "4",
                "2", "1" }, new int[] { 4, 2, 1 }, mIntegerSize);
        radioPanel.setLayoutData(new GridData(GridData.BEGINNING));
        Label label = new Label(panel,SWT.LEFT);
        label.setText("Invert:");
        final Button invert = new Button(panel,SWT.CHECK|SWT.LEFT);
        invert.setSelection(mInvertSearch[0] != 0);
        invert.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                mIntegerSize[0] = invert.getSelection()?1:0;               
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});

        Composite valueMaskPanel = new Composite(panel, SWT.NONE);
        GridLayout gridLayout2 = new GridLayout(2, true);
        gridLayout2.marginHeight = 0;
        gridLayout2.marginWidth = 0;
        valueMaskPanel.setLayout(gridLayout2);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        valueMaskPanel.setLayoutData(gridData);
        makeLabeledCombo(valueMaskPanel, "Value:", mIntegerValue)
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        makeLabeledCombo(valueMaskPanel, "Mask:", mIntegerMask).setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_FILL));       
        return panel;
    }

    private Composite makeRadioButtons(Composite parent, String label,
            String[] buttonLabels, final int[] values, final int[] result) {
        Composite buttonPanel = new Composite(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginLeft = 0;
        rowLayout.marginRight = 0;
        rowLayout.marginTop = 0;
        rowLayout.marginBottom = 0;
        buttonPanel.setLayout(rowLayout);
        Label l = new Label(buttonPanel, SWT.LEFT);
        l.setText(label);

        final Button buttons[] = new Button[buttonLabels.length];
        SelectionListener listener = new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.widget).getSelection()) {
                    for (int i = 0; i < buttons.length; i++) {
                        if (buttons[i] == e.widget) {
                            result[0] = values[i];
                        } else
                            buttons[i].setSelection(false);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);

            }
        };
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new Button(buttonPanel, SWT.RADIO);
            buttons[i].setText(buttonLabels[i]);
            if (result[0] == values[i]) {
                buttons[i].setSelection(true);
            }
        }
        for (Button b : buttons) {
            b.addSelectionListener(listener);
        }
        return buttonPanel;
    }
    
    interface ITextFilter{
        public String filterText(String text);
    }

    /**
     * Make a labeled combo box that is to take an integer. The labels are all
     * aligned to the same width.
     * 
     * @param parent
     *            parent container.
     * @param label
     *            the label
     * @param result
     *            a single element array in which result is to be placed.
     */
    private Composite makeLabeledCombo(Composite parent, final String label,
            final String[] result) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        panel.setLayout(gridLayout);

        Label labelWidget = new Label(panel, SWT.LEFT);
        labelWidget.setText(label);

        final Combo combo = new Combo(panel, SWT.DROP_DOWN);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        combo.setData("name","memsearch." + label.substring(0,label.length()-1).toLowerCase()); // for GUI tester
        
        wireInCombo(combo,label,new ITextFilter(){
            @Override
            public String filterText(String text){
                return text;
            }
        }, result);
        return panel;
    }
    
    /**
     * Given an editable combobox, add appropriate listeners to access it
     * and to add new entries to its drop down items.
     * @param combo the combo box
     * @param key the key in our comboitems map where the combo's items are stored.
     * @param filter called to apply any touchups to the text that was typed in.
     * @param result 1-element array where result is placed.
     */
    private void wireInCombo(final Combo combo, final String key, final ITextFilter filter, final String result[]){

        String[] items = mComboItems.get(key);
        int index = -1;
        if (items != null) {
            for (String s : items) {
                combo.add(s);
                if (s.equals(result[0])) {
                    index = combo.getItemCount() - 1;
                }
            }
        }
        if (index >= 0) {
            combo.select(index);
        } else
            combo.setText(result[0]);
        final boolean textTyped[] = {false};
        final SelectionListener listener = new SelectionListener() {
            private boolean _processingEvent = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!_processingEvent) {
                    _processingEvent = true;
                    try {
                        String text = filter.filterText(combo.getText());
                        if (text == null)
                            return; // error occurred
                        result[0] = text;
                        //Under Lunix/GTK, the combobox dropdown fails to
                        // work if this code is invoked from focusLost event
                        // unless we resort to delaying...
                        combo.getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                addSelectedComboItem(combo, result[0], key);
                            }
                        });

                    } finally {
                        _processingEvent = false;
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                textTyped[0] = false;
                widgetSelected(e);
            }
        };
        combo.addSelectionListener(listener);
        combo.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e) {
                textTyped[0] = true;
                
            }});
        combo.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                // When focus leaves, make sure combobox is updated.
                if (textTyped[0])
                    listener.widgetDefaultSelected(null);
            }
        });
    }

    private void addSelectedComboItem(final Combo combo, String text, String key) {
        String itemStrings[] = combo.getItems();
        List<String> itemList = new ArrayList<String>(Arrays
                .asList(itemStrings));
        itemList.remove(text);
        itemList.add(0, text); // Put at the start.
        if (itemList.size() > MAX_COMBO_ITEM_COUNT) {
            itemList = itemList.subList(0, MAX_COMBO_ITEM_COUNT);
        }
        itemStrings = itemList.toArray(new String[itemList.size()]);
        combo.removeAll();
        for (String s : itemStrings) {
            combo.add(s);
        }
        combo.select(0);
        mComboItems.put(key, itemStrings);
    }

    private void showErrorBox(String message) {
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        MessageBox m = new MessageBox(shell, SWT.OK);
        m.setMessage(message);
        m.open();
    }

    private Composite makeSearchForStringPanel(Composite parent,
            final String labelText, final String[] result) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        panel.setLayout(gridLayout);
        Label labelWidget = new Label(panel, SWT.LEFT);
        labelWidget.setText(labelText);

        final Combo combo = new Combo(panel, SWT.DROP_DOWN);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        combo.setData("name","memsearch."+labelText.replaceAll(" ","_").toLowerCase()); // for GUI tester.

        wireInCombo(combo,labelText,new ITextFilter(){
            @Override
            public String filterText(String text){
                // Legacy version requires that it be enclosed in quotes.
                if (labelText.indexOf("char") >= 0) {
                    if (!text.startsWith("\"") || !text.endsWith("\"")) {
                        text = "\"" + text + '"';
                    }
                } else {
                    text = text.trim();
                    if (!isHex(text)) {
                        showErrorBox("Invalid hex: " + text);
                        combo.setText(result[0]);
                        text = null; // means error
                    }
                }
                return text;
            }
        }, result);
        
    return panel;
    }

    private static boolean isHex(String s) {
        if (s.length() == 0)
            return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A'
                    && c <= 'F' || c == '?'))
                return false;
        }
        return true;
    }

    /**
     * Make address and length panel. Return the first label.
     * @param parent
     */
    private void makeAddressAndLengthPanel(Composite parent) {
        Composite subPanel = new Composite(parent, SWT.NONE);
        subPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gridLayout = new GridLayout(2, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        subPanel.setLayout(gridLayout);
        makeLabeledCombo(subPanel, "Address:", mStartAddress).setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        makeLabeledCombo(subPanel, "Length:", mLength).setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    }

    private Composite makeDirectionPanel(Composite parent) {
        Composite panel = makeRadioButtons(parent, "Direction of search:", new String[] {
                "Forward", "Reverse" }, new int[] { 0, 1 }, mReversedSearch);
        return panel;
    }

    private void makeSearchButtons(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new RowLayout());
        Button search = new Button(panel, SWT.PUSH);
        final Button searchAgain = new Button(panel, SWT.PUSH);
        search.setText("Search");
        search.setData("name","memsearch.search");  // for GUI tester
        searchAgain.setText("Search again");
        searchAgain.setData("name","memsearch.searchagain");  // for GUI tester
        search.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                doSearch();
                searchAgain.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        searchAgain.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                doSearchAgain();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        searchAgain.setEnabled(false);

    }

    private void doSearch() {
        StringBuilder buf = new StringBuilder(100);
        if (mKind == INTEGER_KIND) {
            if (mIntegerSize[0] == 1) {
                buf.append("/c ");
            } else if (mIntegerSize[0] == 2) {
                buf.append("/s ");
            }
        }
        buf.append("mems");
        if (mReversedSearch[0] != 0) {
            buf.append('b');
        }
        mSearchAgain = buf.toString() + ", gui";
        switch (mKind) {
            case INTEGER_KIND: {
                buf.append(" 0x");
                buf.append(mIntegerValue[0]);
                if (mIntegerMask[0] != DEFAULT_MASK) {
                    buf.append(", mask 0x");
                    buf.append(mIntegerMask[0]);
                }
                break;
            }
            case HEX_STRING_KIND: {
                buf.append(" 0h");
                buf.append(mHexString[0]);
                break;
            }
            case CHAR_STRING_KIND: {
                buf.append(mCharString[0]);
                break;
            }
        }
        buf.append(" 0x");
        buf.append(mStartAddress[0]);
        buf.append(" 0x");
        buf.append(mLength[0]);

        if (mKind == INTEGER_KIND && mInvertSearch[0] != 0) {
            buf.append(", invert");
        }
        buf.append(", gui");
        try {
            getEngine().invokeCommand(buf.toString());
        } catch (EngineException e) {
            showErrorBox(e.getMessage());
        }
    }

    private void doSearchAgain() {
        try {
            getEngine().invokeCommand(mSearchAgain);
        } catch (EngineException e) {
            showErrorBox(e.getMessage());
        }
    }

    private void setKind(int value) {
        mKind = value;
        for (int i = 0; i < mKindButtons.length; i++) {
            mKindButtons[i].setSelection(i == value);
        }
        mCardLayout.topControl = mCards[value];
        mCardPanel.layout(true);
        mCardPanel.redraw();
    }

    private void restoreState() {
        ILaunch launch = getLaunch();
        if (launch != null) {
            try {
                ILaunchConfiguration config = launch.getLaunchConfiguration();
                mKind = config.getAttribute(KIND_KEY, INTEGER_KIND);
                mIntegerSize[0] = config.getAttribute(INTEGER_SIZE_KEY, 4);
                String s = config.getAttribute(INTEGER_VALUE_KEY,(String)null);
                if (s != null)
                    mIntegerValue[0] = s;
                s = config.getAttribute(INTEGER_MASK_KEY,(String)null);
                if (s != null)
                    mIntegerMask[0] = s;
                s = config.getAttribute(START_ADDRESS_KEY,(String)null);
                if (s != null)
                    mStartAddress[0] = s;
                s = config.getAttribute(LENGTH_KEY,(String)null);
                if (s != null)
                    mLength[0] = s;
                mCharString[0] = config.getAttribute(CHAR_STRING_KEY,mCharString[0]);
                mHexString[0] = config.getAttribute(HEX_STRING_KEY,mHexString[0]);
                mInvertSearch[0] = config.getAttribute(INVERT_KEY, 0);
                mReversedSearch[0] = config.getAttribute(REVERSE_KEY, 0);
                Map<String, String> comboItems = Cast.toType(config.getAttribute(
                        COMBO_ITEM_KEY, (Map<String,String>) null));
                mComboItems.clear();
                if (comboItems != null) {
                    for (Map.Entry<String, String> comboItem : comboItems
                            .entrySet()) {
                        String string = comboItem.getValue();
                        String items[] = StringUtil.stringToArray(string);
                        mComboItems.put(comboItem.getKey(), items);
                    }
                }
            } catch (CoreException e) {
                SeeCodePlugin.log(e);
            }
        }
    }

    private void saveState() {
        ILaunch launch = getLaunch();
        if (launch != null) {
            ILaunchConfigurationWorkingCopy config;
            try {
                config = launch.getLaunchConfiguration().getWorkingCopy();
            } catch (CoreException e) {
                SeeCodePlugin.log(e);
                return;
            }
            config.setAttribute(KIND_KEY, mKind);
            config.setAttribute(INTEGER_SIZE_KEY, mIntegerSize[0]);
            config.setAttribute(INTEGER_VALUE_KEY, mIntegerValue[0]);
            if (!DEFAULT_MASK.equals(mIntegerMask[0]))
                config.setAttribute(INTEGER_MASK_KEY, mIntegerMask[0]);
            config.setAttribute(START_ADDRESS_KEY, mStartAddress[0]);
            config.setAttribute(LENGTH_KEY, mLength[0]);
            if (this.mInvertSearch[0] != 0) {
                config.setAttribute(INVERT_KEY, 1);
            }
            config.setAttribute(CHAR_STRING_KEY,mCharString[0]);
            config.setAttribute(HEX_STRING_KEY,mHexString[0]);
            if (this.mReversedSearch[0] != 0) {
                config.setAttribute(REVERSE_KEY, 1);
            }
            Map<String,String> itemMap = new HashMap<String,String>();
            for (Map.Entry<String, String[]> entry : mComboItems.entrySet()) {
                itemMap.put(entry.getKey(),StringUtil.arrayToArgString(entry
                        .getValue()));
            }
            if (itemMap.size() > 0){
                config.setAttribute(COMBO_ITEM_KEY,itemMap);
            }
            try {
                config.doSave();
            } catch (CoreException e) {
                SeeCodePlugin.log(e);
            }
        }
    }

    @Override
    protected void onDispose(EngineInterface engine) {
        saveState();
        super.onDispose(engine);
    }

}
