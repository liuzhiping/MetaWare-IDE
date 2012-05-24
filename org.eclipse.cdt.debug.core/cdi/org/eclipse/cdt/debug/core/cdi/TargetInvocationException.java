/*
 * Created on Aug 25, 2004 by David Pickens
 */
package org.eclipse.cdt.debug.core.cdi;

/**
 * An exception to denote a failure in invoking a target
 * program. We provide this to distinguish between an ordinary
 * "CDIException" because the latter causes a CoreException.
 * @author David Pickens
 */
public class TargetInvocationException extends CDIException {

    /**
     * 
     */
    public TargetInvocationException() {
        super();
 
    }

    /**
     * @param t
     */
    public TargetInvocationException(Throwable t) {
        super(t);
 
    }

    /**
     * @param s
     */
    public TargetInvocationException(String s) {
        super(s);
  
    }

    /**
     * @param s
     * @param t
     */
    public TargetInvocationException(String s, Throwable t) {
        super(s, t);
  
    }

    /**
     * @param s
     * @param d
     */
    public TargetInvocationException(String s, String d) {
        super(s, d); 
    }

}
