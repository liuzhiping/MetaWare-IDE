/**
 * 
 */
package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.IToolChainFlavor;
import com.arc.cdt.toolchain.ui.bcf.ProcessorFamily;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;
import com.arc.cdt.toolchain.ui.bcf.ToolChainFlavorFactory;

/**
 * Creates the panel for choosing a BCF property file.
 * 
 * @author pickensd
 * 
 */
class BcfChooserListBlock implements IBCFBlock {
    private ProcessorFamily family = null;

    @Override
    public void updateConfig(IConfiguration config) {
        try {
            if (config == null) {
                fileList = new File[0]; // shouldn't happen
                family = null;
            } else {
                IToolChainFlavor flavor = ToolChainFlavorFactory.Get(config.getToolChain());
                if (flavor.isGenericARC(config.getToolChain())) family  = null;
                else family = flavor.getProcessorFamily(config.getToolChain());
                fileList = flavor.getTcfFileList(config.getToolChain());
            }

            listViewer.setInput(fileList);
            this.chooseFromListButton.setEnabled(fileList.length > 0);
            this.listViewer.getControl().setEnabled(fileList.length > 0);

            if (!explicitSelectionMade) {
                String s = null;
                s = config != null ? BcfUtils.getTcfFilePath(config) : null;
                setSelectedFile(s);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            Activator.log(e.getMessage(), e);
        }
    }

    private void setSelectedFile(String s) {
     	if (s == null || s.length() == 0) {
            setSelectionState(SelectionType.NO_TCF);
            browseToFile.setStringValue("");
        } else {
            File f = new File(s);
            boolean inListViewer = false;
            if (listViewer != null) {
                int i = Arrays.asList(fileList).indexOf(f);
                if (i >= 0) {
                    listViewer.getList().setEnabled(true);
                    listViewer.setSelection(new StructuredSelection(f), true);
                    setSelectionState(SelectionType.CHOOSE_FROM_LIST);
                    inListViewer = true;
                } else {
                    listViewer.setSelection(null);
                }
            }
            if (this.browseToPending == 0) {
                this.browseToFile.setStringValue(s);
            }
            if (!inListViewer) setSelectionState(SelectionType.BROWSE_TO);
        }
        fireChange();
    }

    @Override
    public void setTcfLocation(String f) {
        setSelectedFile(f);
    }

    @Override
    public File getTcfLocation() {

        switch (selectionState) {
        case NO_TCF:
            return null;
        case CHOOSE_FROM_LIST:{
            if (this.fileList == null || this.fileList.length == 0)
                return null;
            IStructuredSelection sel = (IStructuredSelection)this.listViewer.getSelection();
            if (sel == null || sel.isEmpty())
                return null;
        }
            //$FALL-THROUGH$
        case BROWSE_TO: {
            String s = this.browseToFile.getStringValue();
            if (s != null && s.length() > 0) {
                File f = new File(s);
                /* if (f.exists()) */return f;
            }
            break;
        }
        }
        return null;
    }

    @Override
    public Control getControl() {
        return control;
    }

    static IBCFBlock create(Composite parent) {
        return new BcfChooserListBlock(parent);
    }

    /**
     * An apparent bug in Windows: Composite.setEnable(false) doesn't disable
     * kids.
     * 
     * @param c
     * @param v
     */
    private static void setEnablement(Control c, boolean v) {
        c.setEnabled(v);
        if (c instanceof Composite) {
            Control kids[] = ((Composite) c).getChildren();
            for (Control k : kids) {
                setEnablement(k, v);
            }
        }
    }

    private class MyFileFieldEditor extends FileFieldEditor {
        MyFileFieldEditor(Composite browsePanel) {
            super("", "", browsePanel);
            this.getTextControl().addKeyListener(new KeyListener() {

                @Override
                public void keyPressed(KeyEvent e) {

                }

                @Override
                public void keyReleased(KeyEvent e) {
                    fireChange();

                }
            });
        }

        @Override
        public void setStringValue(String value) {
            // TODO Auto-generated method stub
            try {
                explicitSelectionMade = true;
                browseToPending++;
                if (value != null) {
                    if (!value.equals(getStringValue())) {
                        super.setStringValue(value);
                        setSelectedFile(value);
                    }
                } else if (getStringValue() != null && getStringValue().length() > 0) {
                    super.setStringValue(value);
                    setSelectedFile(value);
                }
            } finally {
                browseToPending--;
            }

        }
    };
    
    static enum SelectionType { NO_TCF, CHOOSE_FROM_LIST, BROWSE_TO };
    
    private void setSelectionState(SelectionType type){
        // We set enablement unconditionally in case this is called from a 'setVisible(true)' call.
        setEnablement(browsePanel,type == SelectionType.BROWSE_TO);
        setEnablement(viewerPanel,type == SelectionType.CHOOSE_FROM_LIST);
        noBCFButton.setSelection(type == SelectionType.NO_TCF);
        chooseFromListButton.setSelection(type == SelectionType.CHOOSE_FROM_LIST);
        browseToButton.setSelection(type == SelectionType.BROWSE_TO);
        if (type != selectionState) {
            selectionState = type;
            fireChange();
        }
    }
    
    class MySelectionListener implements SelectionListener{
        private SelectionType type;
        MySelectionListener(SelectionType type) { this.type = type; }
        @Override
        public void widgetSelected(SelectionEvent e) {
            setSelectionState(type);
            
        }
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // TODO Auto-generated method stub
            
        }
        
    }

    private BcfChooserListBlock(Composite parent) {
        // note: parent layout assumed to have already been set.
        control = new Composite(parent, SWT.NONE);
        control.setLayout(new GridLayout(1, true));
        
        group = new Group(control, SWT.SHADOW_ETCHED_IN);
        group.setLayout(new GridLayout(1, true));
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalIndent = 12;
        group.setLayoutData(gd);

        noBCFButton = new Button(group,SWT.RADIO);
        noBCFButton.setText("No associated TCF file");
        
        
        chooseFromListButton = new Button(group,SWT.RADIO);
        chooseFromListButton.setText("Choose predefined TCF file");
             
        viewerPanel = new Composite(group,SWT.NONE);
        viewerPanel.setLayout(new GridLayout(2,false)); 
        GridData gdv = new GridData(GridData.FILL_HORIZONTAL);
        gdv.horizontalIndent = 50;
        viewerPanel.setLayoutData(gdv);

        listViewer = new ListViewer(viewerPanel, SWT.SINGLE | SWT.BORDER);
        listViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return fileList;
            }
        });
        listViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof File) {
                    File file = (File)element;
                    String path = file.getPath();
                    File tcf = file.getParentFile();
                    while (tcf != null && !tcf.getName().equals("tcf")){
                        tcf = tcf.getParentFile();
                    }
                    if (tcf != null) {
                        return path.substring(tcf.getPath().length()+1);
                    }
                    return path;
                }
                return super.getText(element); // shouldn't get here
            }
        });
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            private int level = 0;

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (level == 0) {
                    level++;
                    try {
                        if (browseToPending == 0) {
                            File f = (File) ((IStructuredSelection) event.getSelection())
                                    .getFirstElement();
                            if (f != null) {
                                browseToFile.setStringValue(f.getPath());
                                setSelectedFile(f.getPath());
                            } else {
                                browseToFile.setStringValue(null);
                                setSelectedFile(null);
                            }
                        }
                        explicitSelectionMade = true;
                    } finally {
                        level--;
                    }
                }
            }
        });

        gd = new GridData();
        gd.widthHint = 200;
        listViewer.getList().setLayoutData(gd);
        
        bcfDirLabel = new Label(viewerPanel,SWT.LEFT);
        Font f = bcfDirLabel.getFont();
        // Make label italic and smaller than other fonts so as to make it less obtrusive.
        f = new Font(bcfDirLabel.getDisplay(), f.getFontData()[0].getName(), f.getFontData()[0].getHeight()*9/10, SWT.ITALIC);
        bcfDirLabel.setFont(f);
        bcfDirLabel.setForeground(bcfDirLabel.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        bcfDirLabel.setLayoutData(gd);
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalIndent = 50;
        
        
        browseToButton = new Button(group,SWT.RADIO);
        browseToButton.setText("Browse to a TCF file");

        browsePanel = new Composite(group, SWT.NONE);
        browsePanel.setLayoutData(gd);

        browseToFile = new MyFileFieldEditor(browsePanel);
        StringBuilder buf = new StringBuilder();
        for (String s: BcfUtils.EXTENSIONS){
            if (buf.length() > 0) buf.append(';');
            buf.append(s);
        }
        browseToFile.setFileExtensions(new String[]{buf.toString(), "*.*"});
        browseToFile.setPropertyChangeListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                fireChange();
            }
        });
        
        
        noBCFButton.addSelectionListener(new MySelectionListener(SelectionType.NO_TCF));
        chooseFromListButton.addSelectionListener(new MySelectionListener(SelectionType.CHOOSE_FROM_LIST));
        browseToButton.addSelectionListener(new MySelectionListener(SelectionType.BROWSE_TO));
        setSelectionState(SelectionType.NO_TCF);

    }

    @Override
    public void addChangeListener(IChangeListener listener) {
        listeners.add(listener);

    }

    @Override
    public void removeChangeListener(IChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getErrorMessage() {
        if (selectionState  != SelectionType.NO_TCF) {
            File f = getTcfLocation();
            if (f == null) {
                 return "File not specified";
            }
            else if (!f.exists()) {
                return "BCF file does not exist";
            }
            if (family != null){
                ProcessorFamily sfam;
                try {
                    sfam = SettingsFileContent.read(f, null).getProcessorFamily();
                } catch (Exception e) {
                    return "Error reading " + f + ": " + e.getMessage();
                }
                if (sfam != null && sfam != family)
                    return "Selected TCF is for " + sfam + "; expected " + family;
            }
        }
        return null;
    }

    private void fireChange() {
        for (IBCFBlock.IChangeListener c : listeners) {
            c.onChange(this);
        }
    }

    private Button noBCFButton;
    private Button chooseFromListButton;
    private Button browseToButton;
    private Group group;
    private boolean explicitSelectionMade = false;
    private ListViewer listViewer;
    private File[] fileList;
    private FileFieldEditor browseToFile;
    private Composite control;
    private transient int browseToPending = 0;
    private List<IBCFBlock.IChangeListener> listeners = new ArrayList<IBCFBlock.IChangeListener>();
    private Label bcfDirLabel;
    private SelectionType selectionState = null;
    private Composite viewerPanel;
    private Composite browsePanel;
}
