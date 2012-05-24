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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import com.arc.seecode.display.icons.LabelsAndIcons;
import com.arc.widgets.IButton;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;
import com.arc.widgets.IImageWidget;
import com.arc.widgets.ILabel;

/**
 * Builder of the container containing a SeeCode image.
 * 
 * @author David Pickens
 */
class SeeCodeImage {
    private ILabel mStatus;
    private IImageWidget mImageWidget;
    private ImageManipulator mImageManipulator;
    private int mUnitIndex = 0;
    private IChoice mChoice ;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private String mData = null;

    SeeCodeImage(IContainer parent, int width, int height, boolean argb,
            String label, String tip,
            String value_id,
            String scDir,
            IComponentFactory factory) {
         IContainer container = factory.makeGridContainer(parent,5);
         //Make scaler
         IChoice choice = factory.makeComboBox(container,false);
         mChoice = choice;
         initScalar(choice);
         setScalar(choice,width,height);
         
         IButton mirror = factory.makeButton(container);
         mirror.getComponent(); // need to fix this!
         mirror.setToolTipText("mirror the image left/right");
         LabelsAndIcons.setButtonAttributes("mirror", mirror, factory,scDir);
         
         IButton rotate = factory.makeButton(container);
         rotate.getComponent();
         rotate.setToolTipText("rotate the image 90 degrees");
         LabelsAndIcons.setButtonAttributes("rotate",rotate,factory,scDir);
         
         factory.makeLabel(container,label);
         
         mStatus = factory.makeLabel(container,"");
         mStatus.setHorizontalAlignment(IComponent.FILL);
         mStatus.setHorizontalWeight(1.0);
         mStatus.getComponent();
         
         IImage image = factory.makeImage(width,height,32);
         IImageWidget iw = factory.makeImageWidget(container,image);
         iw.setGridSpan(1,5);
         iw.setToolTipText(tip);
         iw.getComponent();
         iw.setHorizontalWeight(1.0);
         iw.setHorizontalAlignment(IComponent.BEGINNING);
         iw.setName(value_id);
         mImageWidget = iw;
        
         
         final ImageManipulator im = new ImageManipulator(argb,iw);
         mImageManipulator = im;
         
         mirror.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                im.flip();
                setStatus();                
            }});
         
         rotate.addActionListener(new ActionListener(){

             @Override
            public void actionPerformed(ActionEvent e) {
                 im.rotate(90);
                 setStatus();                
             }});

    }
    
    IImageWidget getImageWidget(){
        return mImageWidget;
    }
    
    void updateData(String data){
        mData = data;
        mImageManipulator.updateData(data);
        setStatus();
    }
    
    /**
     * Update the image from a property description. The recognized properties
     * are as follows:
     * <dl>
     * <dt><code>width</code>
     * <dd><i>(integer) <i>the new width of the image in pixels.
     * <dt><code>height</code>
     * <dd><i>(integer) </i> the new height of the image in pixels.
     * <dt><code>32</code>
     * </dl>
     * <i>(boolean) </i> set whether or not image is 32 bits deep.
     * <dt><code>tip</code>
     * <dd><i>(string) </i> a tooltip to be associated with the image.
     * 
     * @param properties
     */
    public void updateProperties(Properties properties) {
        String widthString = properties.getProperty("width");
        String heightString = properties.getProperty("height");
        String tip = properties.getProperty("tip");
        if (tip != null)
            mImageWidget.setToolTipText(tip);
        String _32String = properties.getProperty("32");
        int width = widthString == null ? mImageWidget.getImageWidth() : Integer
                .parseInt(widthString);
        int height = heightString == null ? mImageWidget.getImageHeight() : Integer
                .parseInt(heightString);
        boolean oldARGB = mImageManipulator.isARGB();
        boolean _32bits = _32String == null ? mImageManipulator.isARGB()
                : (_32String.equals("1") || _32String.equals("true"));
        mImageManipulator.updateProperties(width, height, _32bits);
        setScalar(mChoice,width,height);
        if (widthString != null && width != mImageWidth || heightString != null && height != mImageHeight ||
                _32bits != oldARGB){
            mImageWidth = width;
            mImageHeight = height;
            if (mData != null) {
                mImageManipulator.updateData(mData);
                setStatus();
            }
        }
    }
    
    private void setStatus(){
        StringBuffer buf = new StringBuffer();
        int degrees = mImageManipulator.getRotate();
        if (degrees != 0){
            buf.append("rotated " + degrees + " degrees");
        }
        if (mImageManipulator.isFlipped()){
            buf.append(" flipped");
        }
        mStatus.setText(buf.toString());
        mStatus.revalidate();
    }
    
    private void initScalar(final IChoice choice){
        choice.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = choice.getSelectionIndex();
                if (index < mUnitIndex){
                    mImageManipulator.setScale(1,mUnitIndex-index);
                }
                else
                    mImageManipulator.setScale(index-mUnitIndex+1,1);
                
            }});
        
    }
    
    private void setScalar(IChoice choice, int width, int height){
        int maxUp = Math.min(1024/width,768/height);
        setChoiceItems(choice,4,maxUp);
    }
    
    private void setChoiceItems(IChoice choice, int down, int up){
        int current = choice.getSelectionIndex() - mUnitIndex;
        if (current < 0) current = 0;
        choice.removeAllItems();
        for (int i = 0; i < down; i++){
            choice.addItem("1:" + (down-i));
        }
        mUnitIndex = down-1;
        for (int i = 1; i < up; i++){
            choice.addItem("" + (i+1) + ":1");
        }
        if (mUnitIndex + current >= 0 &&
            mUnitIndex + current < down+up-1)
            choice.setSelectionIndex(mUnitIndex + current);
    }

}
