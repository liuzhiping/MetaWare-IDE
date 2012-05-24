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
package com.arc.seecode.display.icons;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IImage;
import com.arc.widgets.IToolItem;

/**
 * @author David Pickens
 */
public class LabelsAndIcons {
    public static String getButtonLabel(String key){
        String s = BUTTON_LABELS.get(key);
        if (s == null) return key;
        return s;
    }
    
    public static IImage getButtonIcon(String key, IComponentFactory f, String scDir){
        key = key.toLowerCase().replace(' ','_');
        key = key.replace('/','_'); // handle "pc/fp" key: "pc_fp.gif"
        //First extract the icon directly from the debugger installation, if possible.
        // Otherwise, extract at local resource.
        if (scDir != null) {
            String iconPath = scDir + "/mdb/buttons/swing/" + key;
            if (!new File(iconPath).exists()){
                if (!key.endsWith(".gif")){
                    iconPath += ".gif";
                }
            }
            if (new File(iconPath).exists()){
                try {
                    URL url = new URL("file",null,iconPath);
                    IImage icon = f.makeImage(url);
                    if (icon != null)
                        return icon;
                }
                catch (MalformedURLException e) {
                    // do nothing
                }
                catch (IllegalArgumentException x){
                    // Unsupported format...
                }
            }
        }
        String gif = key;
        if (key.indexOf('.') < 0)
            gif += ".gif";
        // At some point, SWT seems to have problems reading a file named "#.gif".
        if (gif.equals("#.gif"))
            gif = "sharp.gif";
        URL url = LabelsAndIcons.class.getResource(gif);
        if (url != null){
            return f.makeImage(url);
        }
      
        return null;
    }
    
    public static void setButtonAttributes(String labelKey, IToolItem button, IComponentFactory f,
        String scDir){

        IImage image = getButtonIcon(labelKey,f,scDir);
        if (image != null) {
            button.setImage(image);
        }
        else {
            String label = getButtonLabel(labelKey);
            button.setText(label);
        }
    }
    
    private static HashMap<String,String> BUTTON_LABELS = new HashMap<String,String>(40);

    static  {
        //Main toolbar
        BUTTON_LABELS.put("isi", "Instr Into");
        BUTTON_LABELS.put("iso", "Instr Over");
        BUTTON_LABELS.put("ssi", "Src Into");
        BUTTON_LABELS.put("sso", "Src Over");
        BUTTON_LABELS.put("ret", "Step Out");
        BUTTON_LABELS.put("run", "Run");
        BUTTON_LABELS.put("stop", "Stop");
        BUTTON_LABELS.put("animate", "Animate");
        BUTTON_LABELS.put("restart", "Restart");

        //Secondary toolbars
        BUTTON_LABELS.put("Break", "Break");
        BUTTON_LABELS.put("stackup", "Stack Up");
        BUTTON_LABELS.put("stackdown", "Stack Dn");
        BUTTON_LABELS.put("PC", "Show PC");
        BUTTON_LABELS.put("Dis", "Disasm");
        BUTTON_LABELS.put("lock_opened", "Lock");
        BUTTON_LABELS.put("lock_closed", "Lock");
        BUTTON_LABELS.put("Snap", "Snapshot");
        BUTTON_LABELS.put("Snaps", "Show Snap");
        BUTTON_LABELS.put("Sdel", "Delete");
        BUTTON_LABELS.put("all", "All");
        BUTTON_LABELS.put("ssi_f", "Src fwd");
        BUTTON_LABELS.put("ssi_b", "Src back");
        BUTTON_LABELS.put("isi_f", "Instr fwd");
        BUTTON_LABELS.put("isi_b", "Instr back");
        BUTTON_LABELS.put("Change", "Change");
        BUTTON_LABELS.put("Watch", "Watch");
        BUTTON_LABELS.put("profarc", "Profile");
        BUTTON_LABELS.put("profac", "Profile");
        BUTTON_LABELS.put("profarm", "Profile");
        BUTTON_LABELS.put("profmips", "Profile");
        BUTTON_LABELS.put("profppc", "Profile");
        BUTTON_LABELS.put("profvc", "Profile");
        BUTTON_LABELS.put("Send", "Send");
        BUTTON_LABELS.put("show", "Show");
        BUTTON_LABELS.put("focus", "Focus");
        BUTTON_LABELS.put("+/- focus", "+/- Focus");
        BUTTON_LABELS.put("next", "Next");
        BUTTON_LABELS.put("prev", "Prev");
        // additional buttons
        BUTTON_LABELS.put("Delete", "Delete");
        BUTTON_LABELS.put("Copy", "Copy");
        BUTTON_LABELS.put("Create", "Create");

        // SHOOT! boolean_buttons don't hold their aliases
        // Future feature: Need to modify boolean_button to hold aliases

        //buttonLabels.put("Mix", "Mix Src");
        //buttonLabels.put("T", "Types");
        //buttonLabels.put("Locals", "Locals");
        //buttonLabels.put("Source", "Source");
        //buttonLabels.put("memdown", "Address Dn");
        //buttonLabels.put("memup", "Address Up");
        //buttonLabels.put("deep", "Depth");
        //buttonLabels.put("shallow", "Depth");
    }

}
