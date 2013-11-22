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
package com.arc.cdt.debug.seecode.internal.ui.action;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAnimatable;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;


/**
 * A single instance of this class monitors the Debug view. When it detects that a MetaWare debug session is active and
 * animation is supported, then the animate toolbar widgets will materialize.
 * <P>
 * We do this dynamically instead of using the viewAction extension mechanism because there is no visability control on
 * such toolbar buttons (as of Eclipse 3.4M5). We don't want to force the user to have a cluttered toolbar if he doesn't
 * care about animation.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class AnimateToolBarManager implements IDebugContextListener, IDebugEventSetListener {

    // Number of steps before we adjust speed to reflect reality.    
    private static final int DELAY_BEFORE_ADJUSTING_SPEED = 10;
    
    private static final String ANIMATE_GROUP = "animateGroup";

    private ICDITarget currentTarget = null;

    private IViewPart currentViewPart = null;

    private enum ACTION_TYPE {
        ANIMATE_STEP_INTO, ANIMATE_STEP_OVER, ANIMATE_SLOWER, ANIMATE_FASTER, RESET_COUNTER
    }

    private AnimateAction animateStepIntoAction;

    private AnimateAction animateStepOverAction;

    private IAction animateSlower = null;

    private IAction animateFaster = null;

    private ISelection currentSelection = null;

    private IContributionItem groupMarker;

    private IAction counter;

    private LabelContribution speedLabel;
    
    private int stepsRemainingBeforeAdjustingSpeed = 0;

    private Set<IViewPart> fViewsInWhichVisible = new HashSet<IViewPart>(2);

    class AnimateAction extends Action {

        private ACTION_TYPE actionType;

        AnimateAction(String icon, String tooltip, ACTION_TYPE actionType) {
            super(icon==null?"0000":null, icon != null?UISeeCodePlugin.getDefault().getImageDescriptor(icon):null);
            this.setToolTipText(tooltip);
            this.setEnabled(false);
            this.actionType = actionType;
            this.setId(actionType.name());
        }

        @Override
        public void run () {
            switch (actionType) {
                case ANIMATE_STEP_INTO:
                    startAnimate(true);
                    break;
                case ANIMATE_STEP_OVER:
                    startAnimate(false);
                    break;
                case ANIMATE_FASTER:
                    speedUp();
                    break;
                case ANIMATE_SLOWER:
                    slowDown();
                    break;
                case RESET_COUNTER:
                    resetCounter();
                    break;
            }
        }
    }

    public AnimateToolBarManager() {
        DebugUITools.getDebugContextManager().addDebugContextListener(this);
        this.animateStepIntoAction = new AnimateAction("icons/animate_step_into.gif", "Animate step-into",
            ACTION_TYPE.ANIMATE_STEP_INTO);
        this.animateStepOverAction = new AnimateAction("icons/animate_step_over.gif", "Animate step-over",
            ACTION_TYPE.ANIMATE_STEP_OVER);
        this.animateSlower = new AnimateAction("icons/animate_slower.gif", "Decrease animation speed.",
            ACTION_TYPE.ANIMATE_SLOWER);
        this.animateFaster = new AnimateAction("icons/animate_faster.gif", "Increase animation speed.",
            ACTION_TYPE.ANIMATE_FASTER);
        this.counter = //new ButtonContribution("ANIMATOR_COUNTER", "Animation steps executed. Press to reset.");
            new AnimateAction(null,"Animation steps executed; press to reset.",ACTION_TYPE.RESET_COUNTER);
        this.speedLabel = new LabelContribution("ANIMATOR_SPEED", "Animation speed in steps per minute");
        this.groupMarker = new Separator(ANIMATE_GROUP);
//        this.counter.addListener(new ButtonContribution.IListener() {
//
//            public void onSelected () {
//                resetCounter();
//
//            }
//        });
        DebugPlugin.getDefault().addDebugEventListener(this);
    }

    @Override
    public void debugContextChanged (DebugContextEvent event) {
        setSelection(event.getDebugContextProvider().getPart(), event.getContext());
    }

    public void setVisible (IViewPart view, boolean visible) {
        if (fViewsInWhichVisible.contains(view) != visible) {
            if (visible) {
                fViewsInWhichVisible.add(view);
                addAnimateItems(view);
                if (view == currentViewPart)
                    setSelection(view,currentSelection);
            }
            else {
                fViewsInWhichVisible.remove(view);
                removeAnimateItems(view);
            }
        }
    }

    /**
     * @param view the view.
     * @return whether or not the given view has its animation buttons visible in its toolbar.
     */
    protected boolean isVisible (IViewPart view) {
        return fViewsInWhichVisible.contains(view);
    }

    private void startAnimate (boolean stepInto) {
        if (currentSelection != null && currentViewPart != null) {
            ICDIAnimatable animatable = null;
            ICDIThread thread = Utilities.computeCDIThreadFromSelection(currentSelection);
            if (thread instanceof ICDIAnimatable)
                animatable = (ICDIAnimatable) thread;
            IDebugTarget target = Utilities.computeTargetFromSelection(currentSelection);
            if (animatable == null) {
                ICDITarget cdiTarget = (ICDITarget) target.getAdapter(ICDITarget.class);
                if (cdiTarget instanceof ICDIAnimatable) {
                    animatable = (ICDIAnimatable) cdiTarget;
                }
            }
            if (animatable != null) {
                if (target instanceof ISteppingModeTarget) {
                    ISteppingModeTarget t = (ISteppingModeTarget) target;

                    try {
                        animatable
                            .animate(t.isInstructionSteppingEnabled() ? (stepInto ? ICDIAnimatable.ANIMATE_INSTR_STEP_INTO
                                : ICDIAnimatable.ANIMATE_INSTR_STEP_OVER)
                                : (stepInto ? ICDIAnimatable.ANIMATE_STATEMENT_STEP_INTO
                                    : ICDIAnimatable.ANIMATE_STATEMENT_STEP_OVER));
                    }
                    catch (CDIException e) {
                        SeeCodePlugin.log("start animation failure", e);
                    }
                }

            }
        }
    }

    private void speedUp () {
        ICDIAnimatable target = currentTarget instanceof ICDIAnimatable ? (ICDIAnimatable) currentTarget : null;
        if (target != null) {
            int delay = target.getAnimateStepDelay();
            if (delay > 1) {
                delay = delay * 2 / 3;
                target.setAnimateStepDelay(delay);
                fSpeedHistoryCount = 0; // recompute speed for display
                stepsRemainingBeforeAdjustingSpeed = DELAY_BEFORE_ADJUSTING_SPEED;
                setSpeed(delay);
            }
        }
    }

    private void slowDown () {
        ICDIAnimatable target = currentTarget instanceof ICDIAnimatable ? (ICDIAnimatable) currentTarget : null;
        if (target != null) {
            int delay = target.getAnimateStepDelay();
            int actual = target.getActualAnimateStepDelay();
            if (actual > delay)
                delay = actual;
            else {
                // If going super fast, than slow down to something reasonable.
                if (delay <= 1) delay = 5;
                else
                    delay = (delay + 1) * 3 / 2;
            }
            fSpeedHistoryCount = 0; // recompute speed for display
            stepsRemainingBeforeAdjustingSpeed = DELAY_BEFORE_ADJUSTING_SPEED;
            setSpeed(delay);
            target.setAnimateStepDelay(delay);
        }
    }

    private void resetCounter () {
        ICDIAnimatable target = currentTarget instanceof ICDIAnimatable ? (ICDIAnimatable) currentTarget : null;
        if (target != null) {
            target.resetAnimationCounter();
            setCount(target.getAnimationCount());
        }
    }

    private void setSelection (IWorkbenchPart part, ISelection selection) {
        this.currentSelection = selection;
        if (part instanceof IViewPart) {
            IViewPart viewPart = (IViewPart) part;
            if (isVisible((IViewPart) part)) {
                boolean enabled = false;
                boolean animating = false;
                ICDIThread thread = Utilities.computeCDIThreadFromSelection(selection);
                if (thread instanceof ICDIAnimatable) {
                    animating = ((ICDIAnimatable)thread).isAnimating();
                    enabled = thread.isSuspended() || animating;
                }
                if (!enabled) {
                    currentViewPart = viewPart;
                    setEnablement(false, animating);
                }
                else {
                    ICDITarget target = Utilities.computeCDITargetFromSelection(selection);
                    if (target != currentTarget || viewPart != this.currentViewPart) {
                        this.currentTarget = target;
                        this.currentViewPart = viewPart;
                        IToolBarManager toolbarMgr = getToolBar(viewPart);
                        if (!target.isTerminated() /* && engine.supportsAnimation() */) {
                            if (!containsAnimateItems(toolbarMgr)) {
                                addAnimateItems(viewPart);
                            }
                            setCount(((ICDIAnimatable)thread).getAnimationCount());
                            setSpeed(((ICDIAnimatable)thread).getActualAnimateStepDelay());
                        }
                        else
                            removeAnimateItems(viewPart);
                        fSpeedHistoryCount = 0; // recompute speed for display
                    }
                    setEnablement(true, animating);
                }
            }
            else
                currentViewPart = viewPart;
        }

    }

    private static IToolBarManager getToolBar (IViewPart viewpart) {
        return viewpart.getViewSite().getActionBars().getToolBarManager();
    }

    private boolean containsAnimateItems (IToolBarManager mgr) {
        return mgr.find(animateStepIntoAction.getId()) != null;
    }

    private void setEnablement (boolean enable, boolean isAnimating) {
        animateStepIntoAction.setEnabled(enable);
        animateStepOverAction.setEnabled(enable);
        animateSlower.setEnabled(isAnimating);
        animateFaster.setEnabled(isAnimating);
        counter.setEnabled(enable || isAnimating);
        speedLabel.setEnabled(isAnimating);
    }
    
    private void setSpeed(int delay){
        if (delay == 0) delay = 1;
        speedLabel.setText(Integer.toString(60000/delay));
    }
    
    private void setCount(int count){
        String s = Integer.toString(count);
        // If we let the button render to narrow, then layout of the toolbar gets
        // screwed up and the whole animation section gets truncated with a failed wrap.
        if (s.length() < 5){
            s = "     ".substring(0,5-s.length()) + s;
        }
        counter.setText(s);
    }

    private void addAnimateItems (IViewPart view) {
        IToolBarManager toolbarMgr = getToolBar(view);
        toolbarMgr.add(groupMarker);
        toolbarMgr.appendToGroup(ANIMATE_GROUP, animateStepIntoAction);
        toolbarMgr.appendToGroup(ANIMATE_GROUP, animateStepOverAction);
        toolbarMgr.appendToGroup(ANIMATE_GROUP, counter);
        toolbarMgr.appendToGroup(ANIMATE_GROUP, animateFaster);
        toolbarMgr.appendToGroup(ANIMATE_GROUP, animateSlower);
        toolbarMgr.appendToGroup(ANIMATE_GROUP, speedLabel);
        view.getViewSite().getActionBars().updateActionBars();
    }

    private void removeAnimateItems (IViewPart view) {
        IToolBarManager toolbarMgr = getToolBar(view);
        toolbarMgr.remove(groupMarker);
        toolbarMgr.remove(animateStepIntoAction.getId());
        toolbarMgr.remove(animateStepOverAction.getId());
        toolbarMgr.remove(animateFaster.getId());
        toolbarMgr.remove(animateSlower.getId());
        toolbarMgr.remove(counter.getId());
        toolbarMgr.remove(speedLabel.getId());
        view.getViewSite().getActionBars().updateActionBars();
    }

    @Override
    public void handleDebugEvents (DebugEvent[] events) {
        if (isVisible(currentViewPart)) {
            for (DebugEvent event : events) {
                switch (event.getKind()) {
                    case DebugEvent.TERMINATE:
                        setEnablement(false, false);
                        break;
                    case DebugEvent.SUSPEND:
                    case DebugEvent.RESUME: {
                        ICDITarget target = Utilities.computeCDITargetFromSelection(event.getSource());
                        if (target != null && target == this.currentTarget && target instanceof ICDIAnimatable) { 
                            int delay =((ICDIAnimatable) target).getActualAnimateStepDelay();
                            delay = computeMedianDelay(delay);    //adjust speed and remembers history     
                            if (stepsRemainingBeforeAdjustingSpeed == 0) { // adjust speed to represent reality.                                                 
                                setSpeed(delay);
                            }
                            else 
                                stepsRemainingBeforeAdjustingSpeed--;
                            setCount(((ICDIAnimatable) target).getAnimationCount());
                            setEnablement(event.getKind() == DebugEvent.SUSPEND &&
                                !((ICDIAnimatable) target).isAnimating(), ((ICDIAnimatable) target).isAnimating() &&
                                event.getKind() != DebugEvent.TERMINATE);
                        }
                    }
                }
            }
        }
    }

    private int computeMedianDelay (int newSpeed) {
        int insertPoint = fSpeedHistoryCount;
        for (int i = 0; i < fSpeedHistoryCount; i++) {
            if (fPrevSpeeds[i] >= newSpeed) {
                insertPoint = i;
                break;
            }
        }
        if (insertPoint == fPrevSpeeds.length) {
            for (int i = 1; i < fSpeedHistoryCount; i++) {
                fPrevSpeeds[i - 1] = fPrevSpeeds[i];
            }
            insertPoint--;
        }
        else if (fSpeedHistoryCount < fPrevSpeeds.length) {
            // if event insert
            for (int i = fSpeedHistoryCount; i > insertPoint; i--) {
                fPrevSpeeds[i] = fPrevSpeeds[i - 1];
            }
            fSpeedHistoryCount++;
        }
        else if ((insertPoint & 1) == 0 || insertPoint == 0) {
            // If even purge off at the end
            for (int i = fPrevSpeeds.length - 1; i > insertPoint; i--) {
                fPrevSpeeds[i] = fPrevSpeeds[i - 1];
            }
        }
        else {
            // If odd purge off the beginning
            for (int i = 1; i < insertPoint; i++) {
                fPrevSpeeds[i - 1] = fPrevSpeeds[i];
            }
            insertPoint--;
        }
        fPrevSpeeds[insertPoint] = newSpeed;

        return fPrevSpeeds[fSpeedHistoryCount / 2];

    }

    private int fPrevSpeeds[] = new int[30];

    private int fSpeedHistoryCount = 0;
}
