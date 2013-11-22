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


/**
 * A default toolbar builder that is relatiely simple.
 * Under Eclipse, we don't use this one. Instead, we use a more sophisticated version that 
 * creates items
 * in the View's toolbar.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class DefaultToolBarBuilder implements IToolBarBuilder {

    private IComponentFactory mWidgetFactory;
    private IContainer mContainer;
    private IToolBar mToolBar = null;
    private IContainer mCurrentRow = null;

    public DefaultToolBarBuilder(IComponentFactory widgetFactory, IContainer parent){
        mWidgetFactory = widgetFactory;
        mContainer = widgetFactory.makeContainer(parent,
                IComponentFactory.COLUMN_STYLE);
        mContainer.setHorizontalWeight(1.0);
        mContainer.setHorizontalAlignment(IComponent.FILL);
        newLine();
    }
    public IComponent getToolBar() {
        return mContainer;
    }
    
    /**
     * Indicate that subsequent widgets are to be added on the next line.
     *  
     */
    public void newLine() {
        mToolBar = null; // force new toolbar
        mCurrentRow = mWidgetFactory.makeContainer(mContainer,
                IComponentFactory.ROW_STYLE);
        mCurrentRow.setHorizontalAlignment(IComponent.FILL);
        mCurrentRow.setHorizontalWeight(1.0);
    }

    public IContainer getControl() {
        return mCurrentRow;
    }

    @Override
    public IChoice makeComboBox(String label, boolean editable) {
        IChoice choice;
        mToolBar = null;
        if (label != null) {
            IContainer choicePanel = mWidgetFactory.makeContainer(getControl(),
                    IComponentFactory.ROW_STYLE);
            if (editable){
                choicePanel.setHorizontalAlignment(IComponent.FILL);
                choicePanel.setHorizontalWeight(1.0);
            }
            mWidgetFactory.makeLabel(choicePanel, label).getComponent();
            
            choice = mWidgetFactory.makeComboBox(choicePanel, editable);
        } else {
            choice = mWidgetFactory.makeComboBox(getControl(), editable);
        }
        if (editable){
            choice.setHorizontalAlignment(IComponent.FILL);
            choice.setHorizontalWeight(1.0);
        }
        return choice;
    }

    @Override
    public IToolItem makeButton() {
        if (mToolBar == null){
            mToolBar = mWidgetFactory.makeToolBar(getControl());
        }
        return mToolBar.makeItem(IToolItem.PUSH);
    }

    @Override
    public ILabel makeLabel(String label) {
        mToolBar = null;
        return mWidgetFactory.makeLabel(getControl(),label);
    }

    @Override
    public IToolItem makeToggleButton() {
        mToolBar = null;
        return mWidgetFactory.makeToggleButton(getControl());
    }

    @Override
    public void makeSeparator() {
        if (mToolBar != null){
            mToolBar.addSeparator();
        }
        else makeLabel(" | ");

    }

    @Override
    public void makeRowSeparator() {
        newLine();
    }
    
    @Override
    public IImageWidget makeImage (int width, int height, int depth) {
        mToolBar = null;
        IImage image = mWidgetFactory.makeImage(width,height,depth);
        return mWidgetFactory.makeImageWidget(getControl(),image);
    }

}
