/*
 * ICDIAnimatableTarget
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package org.eclipse.cdt.debug.core.cdi;



/**
 * 
 * Animation support to a thread.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 * <P>
 * CUSTOMIZATION
 */
public interface ICDIAnimatable {
    /**
     * Passed to {@link #animate} to stop animation. But typically an ordinary stop event
     * does the same thing.
     */
    public static final int ANIMATE_STOP = 0;
    /**
     * Passed to {@link #animate} to animate by invoking "statement-step-into" operations
     * repeatedly. 
     */
    public static final int ANIMATE_STATEMENT_STEP_INTO = 1;
    /**
     * Passed to {@link #animate} to animate by invoking "statement-step-over" operations
     * repeatedly. 
     */
    public static final int ANIMATE_STATEMENT_STEP_OVER = 2;
    /**
     * Passed to {@link #animate} to animate by invoking "instruction-step-into" operations
     * repeatedly. 
     */
    public static final int ANIMATE_INSTR_STEP_INTO = 3;
    /**
     * Passed to {@link #animate} to animate by invoking "instruction-step-over" operations
     * repeatedly. 
     */
    public static final int ANIMATE_INSTR_STEP_OVER = 4;
    /**
     * Do whatever is necessary to animate the target.
     * @param stepType the step type; see <code>IEngineAPI</code>.
     */
    public void animate(int stepType) throws CDIException;
    
    /**
     * Returns whether or not the associated thread is animating.
     * @return whether or not the associated thread is animating.
     */
    public boolean isAnimating();
    
    /**
     * Set the minimum number of milliseconds between animation steps.
     * @param milliseconds the minimum number of millisecond delay betwee animate steps.
     */
    public void setAnimateStepDelay(int milliseconds);
    
    /**
     * Return the specified minimum number of milliseconds to transpire between steps.
     * @return the specified minimum number of milliseconds to transpire between steps.
     */
    public int getAnimateStepDelay();
    
    /**
     * Return the actual delay which will be greater than or equal to {@link #getAnimateStepDelay}.
     * @return the actual delay in milliseconds.
     */
    public int getActualAnimateStepDelay();
    
    /**
     * Return the number of animation steps that have transpired.
     * @return the number of animation steps that have transpired.
     */
    public int getAnimationCount();
    
    /**
     * Reset the animation counter to 0.
     */
    public void resetAnimationCounter();
}
