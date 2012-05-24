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
package com.arc.seecode.internal.display;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.dom4j.Element;

import com.arc.mw.util.Cast;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.display.IContext;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;
import com.arc.seecode.display.ISeeCodeTextViewerFactory;
import com.arc.seecode.display.IToolBarBuilderFactory;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.engine.display.IDisplayCreator;
import com.arc.seecode.internal.display.panels.ExtensionsPanel;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImageWidget;
import com.arc.widgets.IToolBarBuilder;
import com.arc.widgets.IToolItem;

/**
 * @author David Pickens
 */
public class SeeCodeTextViewerFactory implements ISeeCodeTextViewerFactory {
    private static final String PANELS_PACKAGE = "com.arc.seecode.internal.display.panels";

    private List<ISeeCodeTextViewer> mDisplayList = new ArrayList<ISeeCodeTextViewer>();

    private IComponentFactory mWidgetFactory;
    private Map<IImageWidget,SeeCodeImage>mImageWidgetWrapper = null;

    private IToolBarBuilderFactory mToolBarBuilderFactory;

    private IContext fContext;
    
    private String fDebuggerInstallPath = null; //e.g. "C:/arc/metaware/arc"
    private IDisplayCreator fDisplayCreator;

    /**
     * 
     * @param factory the widget factory for creating widgets (Swing or SWT).
     * @param tbBuilderFactory a toolbar builder factory
     * @param context a callback to get thread information if a display needs
     * to provide, say, a thread selection combobox.
     * @param scDir the debugger installation directory (e.g., "C:/arc/metaware/arc").
     * @param displayCreator a callback for creating a display.
     */
    public SeeCodeTextViewerFactory(IComponentFactory factory, IToolBarBuilderFactory tbBuilderFactory,
        IContext context, String scDir, IDisplayCreator displayCreator) {
        if (tbBuilderFactory == null || factory == null){
            throw new IllegalArgumentException("factory is null");
        }
        mWidgetFactory = factory;
        mToolBarBuilderFactory = tbBuilderFactory;
        fContext = context;
        fDebuggerInstallPath = scDir;
        fDisplayCreator = displayCreator;
    }
    

    private static String capitalize(String s) {
        if (s == null)
            return null;
        if (s.length() == 0)
            return s;
        if (s.indexOf('_') >= 0){
            //Get rid of unconventional underscores
            // E.g., "mem_no_examine" ==> "MemNoExamine"
            StringBuffer buf = new StringBuffer(s.length());
            int i = s.indexOf('_');
            while (i >= 0){
                if (i > 0)
                    buf.append(s.substring(0,1).toUpperCase() + s.substring(1,i));
                s = s.substring(i+1);
                i = s.indexOf('_');
            }
            if (s.length() > 0){
                buf.append(s.substring(0,1).toUpperCase() + s.substring(1));
            }
            return buf.toString();
        }
        else
            return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ISeeCodeTextViewerFactory#createDisplay(java.util.Properties,
     *      java.lang.Object, java.lang.Object,
     *      com.arc.seecode.display.ISeeCodeTextViewerCallback)
     */
    @Override
    public ISeeCodeTextViewer createDisplay(int id,
            Properties properties, IContainer parent, ISeeCodeTextViewerCallback callback) {
        ISeeCodeTextViewer existing = lookupDisplay(id);
        if (existing != null && !(existing instanceof DummyViewer)) {
            callback.internalError(lookupDisplay(id),"Window ID is not unique! (=" + id + ")",null);
            return existing;
        }
        // create command
        // The UI has asked us to create a display.
        // Let's ensure that what follows are properties.
        // I.e.:
        //  hide=0/1 (or missing: implies 0)
        //  guic=xxx (specify gui components to add)
        //  lines=NNN (lines in the display)
        //  kind=keyword (kind of display)
        //  UM_window=1 or missing. If 1, says window is user-managed
        //  connectable=1 or missing. If 1, says window
        //          needs to have the connect field for cmpd.
        // Add a dummy element, so windows start at 1.
        // 0 -> command window
        // 1 -> mdi frame
        // The GUI returns the number of this display, which the engine
        // tucks away and uses to talk back to Java.
        // The future window id, after we add it to
        // the set of windows.

        boolean UM_window = properties.getProperty("UM_window", "0").charAt(0) == '1';
        if (UM_window) {
            callback.internalError(null,"UM_windows no longer supported",null);
            // return create_UM_window(properties, window_id,toEngine);
        }
        //        boolean initially_hide =
        //            properties.getProperty(Globals.hide_string, "0").charAt(0) == '1';
        final String kind_of_display = properties
                .getProperty("kind", "unknown");
        String gui_components = properties.getProperty("guic", "");
        String extra_class_name = properties.getProperty("class");

        //
        //"clone_from=<wid>" means that the display that we're about to
        // create is actually a clone of window id "wid" in the parent
        // desktop frame.
        //        String clone_from_string = properties.getProperty("cloned_from");
        //        int cloneFrom = 0;
        //        if (clone_from_string != null) {
        //            cloneFrom = Integer.parseInt(clone_from_string);
        //        }
        SeeCodeTextViewer display = new SeeCodeTextViewer(id, kind_of_display,
                parent, callback, mWidgetFactory, this);
        mDisplayList.add(display);
        ValueSender sender = new ValueSender(display, callback);
        // persistent=0 means that we don't save the window layout
        String persistentString = properties.getProperty("persistent");
        boolean persistent = persistentString == null
                || !persistentString.equals("0");
        ExtensionsPanel panel = null;
        // Now try to find the XXX_extra panel to add to this display.
        try {
            String S = extra_class_name != null ? extra_class_name
                    : kind_of_display;
            Class<?> extra_panel_class = Class.forName(PANELS_PACKAGE + "."
                    + capitalize(S) + "Panel");
            panel = (ExtensionsPanel) extra_panel_class.newInstance();
        } catch (Exception e) {
            panel = new ExtensionsPanel();
        }
        IToolBarBuilder toolBarBuilder = mToolBarBuilderFactory.createToolBarBuilder(display.getComponent());
        panel.init(display, sender, toolBarBuilder,mWidgetFactory,fContext);
        display.setPersistent(persistent);
        //display.setName(makeWindowName(kind_of_display));

        if (gui_components.length() > 0) {
            addWidgetsToExtensionsPanel(panel, gui_components, display, sender,
                    callback, new CheckBoxRecorder(display));
        }
        //Now finish constructing the display since the
        // extensions panel is built.
        display.init(properties, panel, mWidgetFactory,toolBarBuilder);

        panel.finish();
        return display;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ISeeCodeTextViewerFactory#getDisplay(int)
     */
    @Override
    public ISeeCodeTextViewer getDisplay(int idNumber)
            throws IllegalArgumentException {
        ISeeCodeTextViewer v = lookupDisplay(idNumber);
        if (v == null) {
            mDisplayList.add(new DummyViewer(idNumber)); // prevent duplicate errors.
            throw new IllegalArgumentException("Invalid window ID: " + idNumber);
        }
        return v;
    }
    
    /**
     * Return the {@link SeeCodeImage} object that wraps
     * a particular {@link IImageWidget}.
     * @param w the ImageWidget that we're looking for the
     * wrapper for.
     * @return the wrapper for the image widget.
     */
    SeeCodeImage getSeeCodeImageFor(IImageWidget w){
        if (mImageWidgetWrapper == null) return null;
        return mImageWidgetWrapper.get(w);       
    }
    
    /**
     * @param idNumber
     * @return the display corresponding to the id number.
     */
    private ISeeCodeTextViewer lookupDisplay(int idNumber) {
        for (ISeeCodeTextViewer v: mDisplayList){
            if (v.getDisplayID() == idNumber)
                return v;
        }
        return null;
    }

    void remove(ISeeCodeTextViewer viewer){
        mDisplayList.remove(viewer);
    }
    
    /**
     * Called to add a dummy viewer when we're forceably
     * closing one that the engine is possibly updating.
     * @param viewer
     */
    void add(ISeeCodeTextViewer viewer){
        ISeeCodeTextViewer existing = lookupDisplay(viewer.getDisplayID());
        if (existing != null){
            throw new IllegalArgumentException("viewer in use: " + viewer.getDisplayID());
        }
        mDisplayList.add(viewer);
    }

    @Override
    public void addUserGUI(int ugui_number, String specification,
            MenuDescriptor menu, ISeeCodeTextViewerCallback callback, final ISeeCodeTextViewer viewer) {
        ValueSender sender = new ValueSender(viewer, callback);
        addUserGui(ugui_number, specification, menu, callback, viewer, sender);
    }

    /**
     * @param ugui_number
     * @param specification
     * @param menu
     * @param callback
     * @param viewer
     * @param sender
     */
    void addUserGui (int ugui_number, String specification, MenuDescriptor menu, ISeeCodeTextViewerCallback callback, final ISeeCodeTextViewer viewer, IValueSender sender) {
        ICheckBoxRecorder cbrecorder = new CheckBoxRecorder(viewer);
        addUserGUI(ugui_number, specification, menu, callback, sender,cbrecorder);
    }

    private void addUserGUI(final int ugui_number, String specification,
            MenuDescriptor menu, final ISeeCodeTextViewerCallback callback,
            IValueSender sender, ICheckBoxRecorder cbrecorder) {
        // Add user GUI. This is expressed via the XML-like language
        // used by the options processor.
        // Globals.SOP("Processing user GUI specification "+specification);
        Element root = null;
        try {
            root = com.metaware.guihili.GuihiliParser
                    .parseString(specification).getRootElement();
            List<Element> elements = Cast.toType(root.elements());
            if (elements.size() == 1)
                root = elements.get(0);
        } catch (Exception x) {
            callback.internalError(null, "Guihili processing error for "
                    + specification, x);
            return;
        }
        if (root == null) {
            callback.notifyError(null, "Invalid GUI specification: "
                    + specification, "Configuration error");
            return;
        }
        new UserGuiWalker(sender, ugui_number, cbrecorder, fDisplayCreator).walk(root, menu);
    }

    IComponentFactory getWidgetFactory() {
        return mWidgetFactory;
    }

//    private String makeWindowName(String kind_of_display) {
//        // Assign this a name so we can use it symbolically
//        // for recordings.
//        char suffix = '0';
//        boolean done;
//        String wname;
//        do {
//            done = true;
//            wname = (kind_of_display + suffix).intern();
//            for (int i = 0; i < mDisplayList.size(); i++) {
//                if (((SeeCodeTextViewer) mDisplayList.get(i)).getName() == wname) {
//                    // Duplication.
//                    done = false;
//                    break;
//                }
//            }
//            if (!done) {
//                suffix = (char) (suffix + 1);
//            }
//        } while (!done);
//        // Now add the wname.
//        return wname;
//    }

    // Inherited from Tom
    private void addWidgetsToExtensionsPanel(ExtensionsPanel panel, String s,
            SeeCodeTextViewer dtd, IValueSender sender,
            ISeeCodeTextViewerCallback callback, ICheckBoxRecorder cbRecorder) {
        if (s.indexOf("=") > 0) {
            // New format.
            List<String> things = extract_properties_from_property_sequence(s);
            for (String thing: things){
                makeWidgetFromPropertyString(panel, thing,sender, callback,cbRecorder);
            }
        } else {
            // Old format, that is still used.
            String options[] = StringUtil.stringToArray(s);
            for (int i = 0; i < options.length; i++) {
                makeOldStyleGuiComponent(options[i], panel,sender);
            }
        }
    }
    
    //Inherited from Tom
    private void makeOldStyleGuiComponent(String s, ExtensionsPanel panel, final IValueSender sender) {
        StringTokenizer ST = new StringTokenizer(s, ":");
        String A = ST.nextToken();
        if (A.equals("choice")) {
            // Format of such direction:
            // choice:Tip:A,B,C:index:value_name:docname
            //         ^ tip to be displayed
            //          ^^^ the choices
            //               ^^ the index of the default choice
            //                ^^^ the value name to be sent to engine
            //                    when a value changes.
            //                         ^^^ doc file name.
            // Get a choice box.
            String tip = ST.nextToken();
            String choice_spec = ST.nextToken();
            String Default = ST.nextToken();
            String value_id = ST.nextToken();
            //String docname = ST.hasMoreTokens()?ST.nextToken():null;
            panel.makeChoice(
                    StringUtil.pathToArray(choice_spec,","),
                    Default,value_id,
                    tip,  true,sender);
        } else if (A.equals("text")) {
            String Label = ST.nextToken();
            String tip = ST.nextToken();
            String size = ST.nextToken();
            String value_id = ST.nextToken();
            //String docname = ST.hasMoreTokens()?ST.nextToken():null;
            panel.makeTextField(Label,value_id, tip,  Integer.parseInt(size),
            true, true, sender);
        } else if (A.equals("button")) {
            // button:name:tip:value_name
            String Label = ST.nextToken();
            String tip = ST.nextToken();
            String value_id = ST.nextToken();
            //String docname = ST.hasMoreTokens()?ST.nextToken():null;
            panel.makeButton(Label,value_id,tip);
        } else if (A.equals("boolean_button")) {
            // bool_button:name0:tio0:name1:tip1:default#:id
            String name0 = ST.nextToken();
            String tip0 = ST.nextToken();
            String name1 = ST.nextToken();
            String tip1 = ST.nextToken();
            // Default value must be 0 or 1.
            String default_value = ST.nextToken();
            String value_id = ST.nextToken();
            //String docname = ST.hasMoreTokens()?ST.nextToken():null;
            panel.makeBooleanToggle(value_id,"1".equals(default_value),name0,name1,tip0,tip1,true);
        }
    }

    /**
     * Append a widget to "extensions panel" of display as described in property
     * string.
     * 
     * @param component_prop
     *            string representation of property values.
     */
    void makeWidgetFromPropertyString(ExtensionsPanel panel,
            String component_prop, IValueSender sender,
            ISeeCodeTextViewerCallback callback,
            ICheckBoxRecorder cbRecorder) {
        Properties props = loadProperties(component_prop);
        String sep = props.getProperty("separator");
        boolean needsSeparator = sep != null && sep.startsWith("1");
        if (needsSeparator){
            panel.makeSeparator();
        }
        
        // Globals.SOP("GUI properties:"+P.toString());
        String kind = props.getProperty("kind");
        String tip = props.getProperty("tip");
        String value_id = props.getProperty("id");
        //        String docname = props.getProperty("docname");
        //        String doc_contents = props.getProperty("doccontents");
        String save_component_state_string = props
                .getProperty("save_component_state");
        // We by default save state unless otherwise told.
        boolean saveComponentState = save_component_state_string == null
                || save_component_state_string.charAt(0) == '1';
        if (kind.equals("choice")) {
            String choiceString = props.getProperty("choices", "");
            String init_choice = props.getProperty("init_choice");
            String[] itemList = StringUtil.pathToArray(choiceString,",");
            panel.makeChoice(itemList, init_choice, value_id,
                    tip, saveComponentState, sender);
        } else if (kind.equals("text")) {
            String label = props.getProperty("label");
            if (label.equals("save_to_file")) {
                panel.setSaveToFileAction(value_id);
            } else {
                String size = props.getProperty("min_size");
                String CR = props.getProperty("CR_needed");
                //String shift_left_string = props.getProperty("shift_left");
                //boolean shiftLeft = shift_left_string == null
                //        || shift_left_string.charAt(0) == '1';
                boolean CR_needed = CR == null || CR.charAt(0) != '0';
                panel.makeTextField(label, value_id, tip, Integer
                        .parseInt(size), saveComponentState, CR_needed,
                        sender);
            }
        } else if (kind.equals("label")) {
            String label = props.getProperty("label", "??label??");
            panel.makeLabel(label,null);
        } else if (kind.equals("button")) {
            String label = props.getProperty("label");
            if (label.equals("double_click")) {
                panel.setDoubleClickAction(value_id);
            } else if (label.equals("column0_click")) {
                panel.setColumn0ClickAction(value_id);
            } else if (label.equals("want_tooltips")) {
                panel.setWantsToolTipAction(value_id);
            } else {
                //                String repeatable = props.getProperty("repeatable");
                //            int repeatMode = repeatable != null ?
                // Integer.parseInt(repeatable)
                //                    : 0;
                //            int repeatDelay = 0;
                //            switch (repeatMode) {
                //                case 1:
                //                    repeatDelay = RepeatingButton.SLOW;
                //                    break;
                //                case 2:
                //                    repeatDelay = RepeatingButton.MEDIUM;
                //                    break;
                //                case 3:
                //                    repeatDelay = RepeatingButton.FAST;
                //                    break;
                //            }
                String enabled = props.getProperty("enabled");
                int E = enabled != null ? Integer.parseInt(enabled) : 1;
                IToolItem b = panel.makeButton(label, value_id,
                        tip);
                if (E == 0) {
                    b.setEnabled(false);
                }
            }
        }else if (kind.equals("LED")) {
            // led: color=string, tip=string
            String color = props.getProperty("color");
            //String orientation = props.getProperty("orientation");
            
            IColor ledColor;
            try {
                ledColor = mWidgetFactory.makeColor(color); 
            } catch(IllegalArgumentException x){
                ledColor = mWidgetFactory.makeColor("black");
            }
            IImageWidget widget = panel.makeLED(ledColor,value_id);
            widget.setToolTipText(tip);
        } else if (kind.equals("image")) {
            String W = props.getProperty("width");
            String H = props.getProperty("height");
            String Label = props.getProperty("label");
            String _32_bit = props.getProperty("32");
            SeeCodeImage si = new SeeCodeImage(panel.getControl(),Integer.parseInt(W),Integer.parseInt(H),
                    _32_bit != null ? Integer
                            .parseInt(_32_bit)!=0 : false,
                            Label,tip,value_id,this.fDebuggerInstallPath,getWidgetFactory());
            panel.registerToolBarItem(si.getImageWidget(),value_id);
            if (mImageWidgetWrapper == null){
                mImageWidgetWrapper = new WeakHashMap<IImageWidget,SeeCodeImage>();
            }
            mImageWidgetWrapper.put(si.getImageWidget(),si);
        } else if (kind.equals("popup_button")) {
            String label = props.getProperty("label");
            makeMenuItem(panel.getStaticMenuDescriptor(), label, value_id, sender);
        } else if (kind.equals("boolean_button")) {
            // bool_button:name0:tio0:name1:tip1:default#:id
            String label0 = props.getProperty("label0");
            String label1 = props.getProperty("label1");
            String tip0 = props.getProperty("tip0");
            String tip1 = props.getProperty("tip1");
            // Default value must be 0 or 1.
            String default_value = props.getProperty("init_state");
            panel.makeBooleanToggle(value_id,"1".equals(default_value),label0,label1,tip0,tip1,
                    saveComponentState);
        } else if (kind.equals("next_line")) {
            // Dummy component that specifies that the GUI components
            // should "shift" to the next line.
            panel.newLine();
        } else if (kind.equals("user_gui")) {
            String ugui = props.getProperty("user_gui");
            addUserGUI(0, ugui, panel.getStaticMenuDescriptor(), callback, sender,cbRecorder);
        } else {
            callback.internalError(null,
                    "Don't recognize GUI component: " + kind,null);
        }
    }



    private void makeMenuItem(MenuDescriptor menu, String label,
            final String value_id, final IValueSender sender) {
        menu.addMenuItem(value_id, label, new MenuDescriptor.IActionObserver() {

            @Override
            public void actionPerformed(String name) {
                sender.sendValueUpdate(value_id, "1");
            }
        });
    }

//    private IButton makeBooleanToggle(IContainer parent, final String valueID,
//            int initValue, final String label1, final String label2,
//            final String tip1, final String tip2, final IValueSender sender) {
//        final IButton b = mWidgetFactory.makeToggleButton(parent);
//        b.setName(valueID);
//        b.setSelected(initValue != 0);
//        setBooleanToggleAttributes(b, label1, label2, tip1, tip2);
//
//        b.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent event) {
//                sender.sendValueUpdate(valueID, b.isSelected() ? "1" : "0");
//                setBooleanToggleAttributes(b, label1, label2, tip1, tip2);
//            }
//        });
//        return b;
//    }

//    private static void setBooleanToggleAttributes(IButton toggle,
//            String label1, String label2, String tip1, String tip2) {
//        if (!toggle.isSelected()) {
//            toggle.setText(label1);
//            toggle.setToolTipText(tip1);
//        } else {
//            toggle.setText(label2);
//            toggle.setToolTipText(tip2);
//        }
//    }

    /**
     * Extract the lists and place them in sequence in a vector.
     * 
     * @param sequence
     *            a nested property list of the form 0=propery list 1=property
     *            list 2=property list
     */
    static List<String> extract_properties_from_property_sequence(String sequence) {
        Properties list = loadProperties(sequence);
        int max_hash = 1024;
        String HC[] = new String[max_hash];
        for (Object keyObject: list.keySet()){
            String key = (String)keyObject;
            if (key.equals("")) {
                // java 1.2 somehow changed the way properties are
                // extracted, and goofed things up. So we have to
                // avoid the empty string as a key.
                continue;
            }
            // Globals.SOP("key is '"+key+"' value is "+list.get(key));
            int hc;
            switch (key.length()) {
                case 1:
                    hc = key.charAt(0) - '0';
                    break;
                case 2:
                    hc = ((key.charAt(0) - '0') << 8) + (key.charAt(1) - '0');
                    break;
                default:
                    hc = key.hashCode();
                    hc = hc % max_hash;
                    break;
            }
            HC[hc] = (String) list.get(key);
            // Get the entries in key order. That's how we make
            // an ordered list. The engine uses single-character
            // keys to ensure hash ordering.
        }
        List<String> v = new ArrayList<String>();
        for (int i = 0; i < max_hash; i++) {
            if (HC[i] != null) {
                v.add(HC[i]);
            }
        }
        return v;
    }

    /**
     * Create <code>Properties</code> map from string representation.
     * 
     * @param props
     *            the string representation.
     * @return the resulting properties.
     */
    static Properties loadProperties(String props) {
        ByteArrayInputStream s = new ByteArrayInputStream(props.getBytes());
        Properties P = new Properties();
        try {
            P.load(s);
        } catch (Exception e) {
        }
        return P;
    }


    @Override
    public String getDebuggerInstallPath () {
        return this.fDebuggerInstallPath;
    }
}
