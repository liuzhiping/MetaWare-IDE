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
package org.eclipse.cdt.debug.internal.ui.views.registers;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IRawValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * Viewer for a set of registers.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class RegistersViewer extends Viewer implements IPropertyChangeListener {

    private static String REG_COLUMN_COLOR_KEY = "org.eclipse.cdt.debug.ui.register.column.color";

    private static String REG_COLUMN_FONT_KEY = "org.eclipse.cdt.debug.ui.register.column.font";

    private static String REG_VALUE_FONT_KEY = "org.eclipse.cdt.debug.ui.register.value.font";

    private static String REG_VALUE_COLOR_KEY = "org.eclipse.cdt.debug.ui.register.value.color";

    private Table table;

    private IStackFrame stackFrame;
    
    private Map<IDebugTarget,Integer> fBankMap = new HashMap<IDebugTarget,Integer>();
    private Map<IDebugTarget,Format> fFormatMap = new HashMap<IDebugTarget,Format>();

    private boolean mVisible = true;

    private String mFilter = null;

    private IRegister[] unfilteredRegisters = NULL_REGS;

    private IRegister[] registers = NULL_REGS;

    private Composite tableContainer;

    private Color mDiffForeground;

    private Color mDiffBackground;

    private Font mRegFont;

    private Color mRegColor;

    private TableCursor fCursor;

    private int mRow; // last selected row

    private int mColumn; // last selected column

    private ControlEditor fEditor;

    private Text fPendingText;

    private int mRowCount;

    private int mColumnCount;

    private Format fFormat = Format.HEXADECIMAL;

    private int fBank = 0; // 0 mean all banks

    private IDebugTarget fTarget;
    
    private Set<IRegister>fPendingDiffs = new HashSet<IRegister>();

    public enum Format {
        SIGNED_DECIMAL("Signed"), UNSIGNED_DECIMAL("Unsigned"), HEXADECIMAL("Hex"), OCTAL("Octal"), FLOAT(
            "Float"), // single precision floating point
        FRACTIONAL_31("Fraction"), // 32-bit fractional
        BINARY("Binary");

        Format(String name) {
            _name = name;
        }

        private String _name;

        @Override
        public String toString () {
            return _name;
        }
    }

    /**
     * Create instance of register viewer, a table of columns and rows.
     */
    RegistersViewer(Composite parent) {
        // Place table in container so that we can hide table when there is no data.
        tableContainer = new Composite(parent, 0);
        tableContainer.setLayout(new GridLayout(1, false));
        tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        table = new Table(tableContainer, SWT.BORDER | SWT.MULTI);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setVisible(false);
        mDiffForeground = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        mDiffBackground = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
        JFaceResources.getColorRegistry().addListener(this);
        JFaceResources.getFontRegistry().addListener(this);
        setFont();
        setColor();

        table.addControlListener(new ControlListener() {

            public void controlMoved (ControlEvent e) {
            }

            public void controlResized (ControlEvent e) {
                if (setRowAndColumnCount()) {
                    refresh(false,true);
                }
            }
        });
        fCursor = new TableCursor(table, SWT.NONE);

        table.addMouseListener(new MouseListener(){

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// Make cursor visible again since clicking on table
				// It is made invisible after a change so that the change can be seen
				fCursor.setVisible(true);				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}});


        fCursor.addMouseListener(new MouseListener() {

            public void mouseDoubleClick (MouseEvent e) {
                setInput(mRow, mColumn);

            }

            public void mouseDown (MouseEvent e) {
                // @todo Auto-generated method stub

            }

            public void mouseUp (MouseEvent e) {
                // @todo Auto-generated method stub

            }
        });
        fCursor.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected (SelectionEvent e) {
                if (fPendingText != null)
                    applyInput();
                mRow = table.indexOf(fCursor.getRow());
                mColumn = fCursor.getColumn() | 1;
                setInput(mRow, mColumn);

            }

            public void widgetSelected (SelectionEvent e) {
                if (fPendingText != null)
                    applyInput();
                mRow = table.indexOf(fCursor.getRow());
                mColumn = fCursor.getColumn() | 1;
                fCursor.setSelection(mRow, mColumn);
            }

        });
    }

    /**
     * Set the format to display the registers with.
     * 
     * @param format the format.
     */
    public void setFormat (Format format) {
        if (format != fFormat) {
            fFormat = format;
            if (fTarget != null)
                fFormatMap.put(fTarget,format);
            refresh(false);
        }
    }
    
    /**
     * Return the format that currently selected.
     * @return the format that currently selected.
     */
    public Format getFormat(){
        return fFormat;
    }

    /**
     * Called when user is changing value of a cell.
     * @param row  row of the table (0 based).
     * @param column column of table (0 based).
     */
    private void setInput (final int row, final int column) {
        fCursor.setSelection(row, column);
        if (fEditor == null) {
            fEditor = new ControlEditor(fCursor);
            fEditor.grabHorizontal = true;
            fEditor.grabVertical = true;
        }
        final TableItem rowItem = table.getItem(row);
        final Text text = new Text(fCursor, SWT.BORDER);
        text.setFont(table.getFont());
        text.setFont(rowItem.getFont(column));
        fPendingText = text;
        text.setText(rowItem.getText(column));
        text.selectAll();
        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed (KeyEvent e) {
                // close the text editor and copy the data over
                // when the user hits "ENTER"
                if (e.character == SWT.CR) {
                    applyInput();
                }
                // close the text editor when the user hits "ESC"
                if (e.character == SWT.ESC) {
                    text.dispose();
                }
            }
        });
        fEditor.setEditor(text);
        text.setFocus();
    }

    /**
     * The user has just hit ENTER key on entering text, or he's clicked outside of the cell.
     */
    private void applyInput () {
        Text text = fPendingText;
        fPendingText = null;
        if (text == null) return; // Happened one time
        final String value = text.getText();

        int index = (mColumn / 2) * table.getItemCount() + mRow;
        try {
            set(registers[index], value, fFormat); // Change listener will update table.
            // Move the selection so that the change shows.
            // There has to be a better way of doing this.
            fCursor.setVisible(false);
        }
        catch (final Exception e) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                public void run () {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    String message = e.getMessage();
                    if (e instanceof NumberFormatException)
                        message = "Inappropriate format (\"" + value + "\")";
                    MessageDialog.openError(shell, "Register update error", message);
                }
            });
        }
        finally {
            text.dispose();
        }

    }

    /**
     * Update the fonts.
     */
    private void setFont () {
        Font font = JFaceResources.getFontRegistry().get(REG_VALUE_FONT_KEY);
        table.setFont(font);
        mRegFont = JFaceResources.getFontRegistry().get(REG_COLUMN_FONT_KEY);
        setTableFontAndColor();
    }

    /**
     * Update the text foreground colors.
     */
    private void setColor () {
        Color color = JFaceResources.getColorRegistry().get(REG_VALUE_COLOR_KEY);
        table.setForeground(color);
        mRegColor = JFaceResources.getColorRegistry().get(REG_COLUMN_COLOR_KEY);
        setTableFontAndColor();
    }

    private void setTableFontAndColor () {
        int rows = table.getItemCount();
        int columns = table.getColumnCount();
        for (int i = 0; i < rows; i++) {
            TableItem item = table.getItem(i);
            for (int j = 0; j < columns; j += 2) {
                item.setForeground(j, mRegColor);
                item.setFont(j, mRegFont);
                item.setFont(j + 1, table.getFont());
            }
        }
    }

    @Override
    public Control getControl () {
        return tableContainer;
    }

    @Override
    public Object getInput () {
        return stackFrame;
    }

    public void applyFilter (String filter) {
        mFilter = filter;
        applyFilter();
        refresh();
    }

    private void applyFilter () {
        if (mFilter == null || mFilter.trim().length() == 0) {
            mFilter = "";
            registers = unfilteredRegisters;
        }
        else {
            int i = 0;
            StringBuilder perlFilter = new StringBuilder(mFilter.length());
            while (i < mFilter.length()) {
                switch (mFilter.charAt(i)) {
                    case '*':
                        perlFilter.append(".*");
                        break;
                    case '?':
                        perlFilter.append(".");
                        break;
                    case '.':
                        perlFilter.append("\\.");
                        break;
                    default:
                        perlFilter.append(mFilter.charAt(i));
                        break;

                }
                i++;
            }
            Pattern p = Pattern.compile(perlFilter.toString());
            List<IRegister> regs = new ArrayList<IRegister>(unfilteredRegisters.length);
            for (IRegister r : unfilteredRegisters) {
                try {
                    if (p.matcher(r.getName()).matches()) {
                        regs.add(r);
                    }
                }
                catch (DebugException e) {
                }
            }
            registers = regs.toArray(new IRegister[regs.size()]);
        }
        setRowAndColumnCount();
    }

    @Override
    public ISelection getSelection () {
        return null;
    }

    public void setVisible (boolean v) {
        if (mVisible != v) {
            mVisible = v;
            refresh();
        }
    }

    private static IRegister[] NULL_REGS = new IRegister[0];

    private IRegister[] getRegistersToDisplay () {
        if (stackFrame == null || !mVisible)
            return NULL_REGS;
        if (unfilteredRegisters.length == 0) {
            ArrayList<IRegister> list;
            try {
                IRegisterGroup groups[] = stackFrame.getRegisterGroups();
                if (groups.length == 1)
                    unfilteredRegisters = groups[0].getRegisters();
                else if (fBank > 0) {
                    unfilteredRegisters = groups[fBank - 1].getRegisters();
                }
                else {
                    list = new ArrayList<IRegister>();
                    for (IRegisterGroup g : groups) {
                        list.addAll(Arrays.asList(g.getRegisters()));
                    }
                    unfilteredRegisters = list.toArray(new IRegister[list.size()]);
                }
            }
            catch (DebugException e) {
                return NULL_REGS;
            }

            applyFilter();
        }
        return registers;
    }

    private String getRegValue (IRegister reg) {
        try {
            IValue value = reg.getValue(); // May set register content as side-effect
            IRawValue rawValue = (IRawValue) reg.getAdapter(IRawValue.class);
            long ivalue;
            int size = 4; // assume 4 bytes unless we find out differently.
            if (rawValue != null) {
                if (!rawValue.isValid()) {
                    return "--------".substring(0, Math.min(rawValue.getLength() * 2, 8));
                }
                if (rawValue.isSpecial())
                    return rawValue.getSpecialValue();
                ivalue = rawValue.getValue();
                size = rawValue.getUnitSize();
            }
            else {
                String s = value.getValueString();
                try {
                    if (s.startsWith("0x")) {
                        ivalue = Long.parseLong(s.substring(2), 16);
                    }
                    else
                        ivalue = Long.parseLong(s);
                }
                catch (NumberFormatException e) {
                    return value.getValueString();
                }
            }
            return convertToString(ivalue, fFormat, size);
        }
        catch (DebugException e) {
            return "???";
        }
    }

    @Override
    public void refresh () {
        refresh(false);
    }
    
    private void refresh(boolean showDiffs){
        refresh(showDiffs,false);
    }

    private void refresh (boolean showNewDiffs, boolean preserveOldDiffs) {
        IRegister regs[] = getRegistersToDisplay();
        if (regs.length == 0) {
            table.setVisible(false);
            return;
        }
        table.setVisible(true);
        int columnCount = mColumnCount;
        int rowCount = (regs.length + columnCount - 1) / columnCount;
        if (rowCount != table.getItemCount()) {
            table.setItemCount(rowCount);
            setTableFontAndColor();
        }
        if (table.getColumnCount() != columnCount*2) {
            for (int i = table.getColumnCount() - 1; i >= columnCount * 2; i--) {
                table.getColumn(i).dispose();
            }
            for (int i = table.getColumnCount(); i < columnCount * 2; i++) {
                new TableColumn(table, SWT.LEFT, i);
            }
            setTableFontAndColor();
        }
        int row = 0;
        int column = 0;
        for (int i = 0; i < regs.length; i++) {
            TableItem itemRow = table.getItem(row);
            try {
                itemRow.setText(column, regs[i].getName());
            }
            catch (DebugException e) {
                itemRow.setText(column, "???");
            }
            String newText = getRegValue(regs[i]);
            String oldText = itemRow.getText(column + 1);
            boolean highlightSet = false;
            if (!newText.equals(oldText)) {
                itemRow.setText(column + 1, newText);
                if (showNewDiffs && oldText != null && oldText.length() > 0) {
                    highlightCell(itemRow, column + 1);
                    fPendingDiffs.add(regs[i]);
                    highlightSet = true;
                }
            }
            if (!highlightSet) {
                if (!preserveOldDiffs || !fPendingDiffs.contains(regs[i])) {
                    unhighlightCell(itemRow, column + 1);
                    fPendingDiffs.remove(regs[i]);
                }
                else {
                    highlightCell(itemRow,column+1);
                }
            }
            row++;
            if (row == rowCount) {
                row = 0;
                column += 2;
            }
        }
        // Null out the rest of the column in case the the user has switched to a
        // bank with slightly fewer registers.
        while (row < rowCount) {
            TableItem itemRow = table.getItem(row);
            itemRow.setText(column,"");
            itemRow.setText(column+1,"");
            unhighlightCell(itemRow,column+1);
            row++;
        }
        for (int i = 1; i < columnCount * 2; i++) {
            table.getColumn(i).pack();
        }
        // BUG: Eclipse 3.3: 0'th column doesn't get set to reasonable value, at least
        // under Windows. So we resort to forcing it.
        if (true) {
            GC gc = new GC(table);
            try {
                int charWidth = gc.getCharWidth('W');
                int charCount = 0;
                for (int i = 0; i < rowCount; i++) {
                    charCount = Math.max(charCount, table.getItem(i).getText(0).length());
                }
                table.getColumn(0).setWidth((charCount + 2) * charWidth);
            }
            finally {
                gc.dispose();
            }
        }
    }

    /**
     * Highlight a cell in a row.
     * @param column the column coordinate.
     * @param itemRow the row.
     */
    private void highlightCell (TableItem itemRow, int column) {
        itemRow.setForeground(column, mDiffForeground);
        itemRow.setBackground(column, mDiffBackground);
    }

    /**
     * Unhighlight a cell in a row.
     * @param column the column coordinate.
     * @param itemRow the row.
     */
    private void unhighlightCell (TableItem itemRow, int column) {
        itemRow.setForeground(column, table.getForeground());
        itemRow.setBackground(column, table.getBackground());
    }

    /**
     * Set the bank to display. 0 means all banks; otherwise, its a bank number.
     * @param bank the bank number, or 0 if all banks.
     */
    public void setBank (int bank) {
        if (fBank != bank) {
            fBank = bank;
            if (fTarget != null) fBankMap.put(fTarget,bank);
            unfilteredRegisters = NULL_REGS; // force recomputation
            refresh(false);
        }

    }
    
    /**
     * Return the register bank number being displayed, or 0 if all banks are being displayed.
     * @return the register bank number being displayed, or 0 if all banks are being displayed.
     */
    public int getBank(){
        return fBank;
    }
    
    public void clearDiffs(){
        int rowCnt = table.getItemCount();
        int columnCnt = table.getColumnCount();
        for (int i = 0; i < rowCnt; i++){
            TableItem row = table.getItem(i);
            for (int j = 0; j < columnCnt; j += 2){
                unhighlightCell(row,j+1);
            }
        }
    }

    /**
     * Called each time the stackframe selection changes.
     * We may be called from different debugger sessions, so we must keep track of the
     * bank and format (or do we?)
     * @param input the new stack frame selection.
     */
    @Override
    public void setInput (Object input) {
        boolean showDiffs = true;
        if (stackFrame != input) {
            unfilteredRegisters = NULL_REGS; // force regeneration
            registers = NULL_REGS;
            if (input instanceof IStackFrame) {
                if (stackFrame != null) {
                    if (stackFrame instanceof ICStackFrame && input instanceof ICStackFrame) {
                        showDiffs = ((ICStackFrame) input).getLevel() == ((ICStackFrame) stackFrame).getLevel() &&
                                ((ICStackFrame)input).getThread() == stackFrame.getThread();
                    }
                }
                stackFrame = (IStackFrame) input;
                if (fTarget != stackFrame.getDebugTarget()){
                    fTarget = stackFrame.getDebugTarget();
                    restoreBankAndFormatSetting(stackFrame);                  
                }
            }
            else {
                stackFrame = null;
            }
            setRowAndColumnCount();
        }
        refresh(showDiffs);
    }
    
    /**
     * This view may be shared by more than one debugger session. Make sure the bank and
     * format settings track the target.
     * @param frame
     */
    private void restoreBankAndFormatSetting(IStackFrame frame){
        IDebugTarget target = frame.getDebugTarget();
        if (target != null) {
            Integer bank  = fBankMap.get(target);
            try {
                int bankNo = 0;
                if (bank == null) {
                    // We ordinarily default to "All" but if there are lots of registers
                    // in other than the first bank, then default to the first bank, which is typically
                    // "core", or something like that.
                    IRegisterGroup[] groups = frame.getRegisterGroups();
                    if (groups.length <= 1){
                        bankNo = 0;
                    }
                    else {
                        int coreCnt = groups[0].getRegisters().length;
                        int cnt = coreCnt;
                        for (int i = 1; i < groups.length; i++){
                            cnt += groups[i].getRegisters().length;
                        }
                        if (cnt > coreCnt + 32) {
                            bankNo = 1;
                        }
                    }           
                }
                else bankNo = bank.intValue();
                setBank(bankNo);
            }
            catch (DebugException e) {
                 // Can't set initial bank
            }
            
            Format format = fFormatMap.get(target);
            if (format == null) {
                format = Format.HEXADECIMAL;
            }
            setFormat(format);
        }      
    }

    @Override
    public void setSelection (ISelection selection, boolean reveal) {
        System.out.println("setSelection " + selection);

    }

    public void propertyChange (PropertyChangeEvent event) {
        String prop = event.getProperty();
        if (prop.equals(REG_COLUMN_COLOR_KEY) || prop.equals(REG_VALUE_COLOR_KEY))
            setColor();
        else if (prop.equals(REG_COLUMN_FONT_KEY) || prop.equals(REG_VALUE_FONT_KEY))
            setFont();
    }

    /**
     * Called when a single register up updated due to, say, user action.
     * @param reg register that was updated.
     */
    public void update (final IRegister reg) {
        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == reg) {
                final int index = i;
                table.getDisplay().asyncExec(new Runnable() {

                    public void run () {
                        final int column = (index / table.getItemCount() * 2) + 1;
                        final int row = index % table.getItemCount();
                        TableItem item = table.getItem(row);
                        String oldValue = item.getText(column);
                        String newValue = getRegValue(reg);
                        if (!oldValue.equals(newValue)) {
                            item.setText(column, newValue);
                            highlightCell(item, column);
                        }
                        if (fCursor.isVisible() && fCursor.getRow() == item && fCursor.getColumn() == column){
                        	fCursor.setVisible(false); // must turn off the cursor or change won't be seen!
                        }
                    }
                });
                break;
            }
        }
    }

    /**
     * Set the row count based on the font size. return true if changed.
     * @return true if changed.
     */
    private boolean setRowAndColumnCount () {
        int oldRowCount = mRowCount;
        int oldColumnCount = mColumnCount;
        Point size = table.getSize();
        int height = size.y;
        Font tfont = table.getFont();
        int rowHeight = tfont.getFontData()[0].getHeight() * 9/4 + 3; // trial and error
        // At startup the table height will be 0 (or something very small), give it a decent
        // number of rows so that the number of columns doesn't go thru the roof, especially for
        // such machines as VideoCore with over 200 registers!
        if (height < rowHeight)
            mRowCount = 10;
        else {
            mRowCount = (height + rowHeight - 1) / rowHeight;
            // System.out.println("TABLE HEIGHT IS " + height + " pixels; rowHieigh=" + rowHeight + "; rowCount=" +
            // mRowCount);
        }
        mColumnCount = (registers.length + mRowCount - 1) / Math.max(1, mRowCount);
        return oldRowCount != mRowCount || mColumnCount != oldColumnCount;
    }

    private static final BigInteger TWO_TO_64_POWER = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).multiply(
        BigInteger.valueOf(2));

    /**
     * Convert integer to string presentation for given radix. If field length is specified, zero-pad.
     * @param v the integer to convert.
     * @param radix the radix.
     * @param fieldLength field length to zero pad to, or 0.
     * @return string representation.
     */
    private static String convert (long v, int radix, int fieldLength) {
        String s;
        if (v >= 0) {
            s = Long.toString(v, radix);
        }
        else {
            // To get unsigned, we add 2**64 which is (2**63)*2
            BigInteger b = BigInteger.valueOf(v);
            b = b.add(TWO_TO_64_POWER);
            s = b.toString(radix);
        }

        if (fieldLength > s.length()) {
            StringBuilder buf = new StringBuilder(fieldLength);
            for (int i = s.length(); i < fieldLength; i++) {
                buf.append('0');
            }
            buf.append(s);
            s = buf.toString();
        }
        return s;
    }

    /**
     * Convert an integer into a string representation according to a format.
     * <P>
     * NOTE: we will undoubtedly move this a public place at some point.
     * @param v the value to be converted.
     * @param format the format.
     * @return string representation
     * @nojni
     */
    private static String convertToString (long v, Format format, int length) {
        if (length <= 4)
            v = v & 0xFFFFFFFFL;
        switch (format) {
            default:
            case HEXADECIMAL: {
                return convert(v, 16, length * 2);
            }
            case OCTAL: {
                return convert(v, 8, (length * 8 + 2) / 3);
            }
            case BINARY: {
                return convert(v, 2, length * 8);
            }
            case SIGNED_DECIMAL: {
                if (length == 1)
                    v = (byte) v;
                else if (length == 2)
                    v = (short) v;
                else if (length == 4)
                    v = (int) v;
                return Long.toString(v);
            }
            case UNSIGNED_DECIMAL: {
                if (length == 1)
                    v = v & 0xFF;
                else if (length == 2)
                    v = v & 0xFFFF;
                else if (length == 4)
                    v = v & 0xFFFFFFFFL;
                if (v >= 0)
                    return Long.toString(v);
                long u = v / -10;
                return Long.toString(u) + Long.toString(v % -10);
            }
            case FLOAT: {
                if (length == 4)
                    return String.format("%.6e", Float.intBitsToFloat((int) v));
                if (length == 8)
                    return Double.toString(Double.longBitsToDouble(v));
                return convert(v, 16, length * 2); // can't handle extended floating point.
            }

// case FRACTIONAL_15: {
// float f = (float) (short) v / (float) (1 << 15);
// return Float.toString(f);
// }
            case FRACTIONAL_31: {
                double f = (double) (int) v / (double) (1L << 31);
                return String.format("%.8f", f);
            }
// case FRACTIONAL_9_31: {
// double f = (double) v / (double)(1L << 31);
// return Double.toString(f);
// }

        }
    }

    private void set (IRegister regID, long value) throws DebugException {
        regID.setValue("0x" + Long.toHexString(value));
    }

    private int getRegLength (IRegister reg) {
        IRawValue rawValue = (IRawValue) reg.getAdapter(IRawValue.class);
        if (rawValue != null)
            return rawValue.getLength();
        return 4; // punt
    }

    /**
     * Set value given its ascii representation in a particular format.
     * @param regID the register ID.
     * @param value the value of the register.
     * @param format the format that the value presumably conforms to.
     * @exception IllegalArgumentException if invalid length or unrecognized format.
     * @exception NumberFormatException if format of the string is invalid.
     * @throws DebugException
     */
    private void set (IRegister regID, String value, Format format) throws IllegalArgumentException,
        NumberFormatException, DebugException {

        // For flexibility, allow C-style hex in any format.
        if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
            set(regID, Long.parseLong(value, 16));
            return;
        }
        switch (format) {
            case HEXADECIMAL:
                set(regID, Long.parseLong(value, 16));
                break;
            case SIGNED_DECIMAL:
            case UNSIGNED_DECIMAL:
                set(regID, Long.parseLong(value));
                break;
            case FLOAT:
                if (getRegLength(regID) <= 4) {
                    set(regID, Float.floatToRawIntBits(Float.parseFloat(value)));
                }
                else {
                    set(regID, Double.doubleToRawLongBits(Double.parseDouble(value)));
                }
                break;
            case OCTAL:
                set(regID, Long.parseLong(value.replace("_", ""), 8));
                break;
            case BINARY:
                set(regID, Long.parseLong(value.replace("_", ""), 2));
                break;
            case FRACTIONAL_31: {
                double d = Double.parseDouble(value);
                if (Math.abs(d) >= 1.0)
                    throw new IllegalArgumentException(
                        "Absolute value must be between 0 (inclusive) and 1.0(exclusive)");
                int length = getRegLength(regID);
                if (length <= 2) {
                    d = d * (1 << 15);
                }
                else if (length <= 4) {
                    d = d * (1 << 31);
                }
                else {
                    d = d * (1L << 63);
                }
                set(regID, (long) d);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown format");

        }
    }

}
