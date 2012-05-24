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
package com.arc.seecode.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * This class consists of a static method for adding arbitrary command
 * processors to a command parser. It is done by taking an arbitrary
 * object and, by reflection, finding all methods that take a
 * string as an argument and begin with "do_" as a prefix.
 * <P>
 * A parameterless method that has "repeat_" as a prefix will be invoked to repeat
 * a command.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CommandProcessorAugmenter {
    private static String METHOD_PREFIX = "do_";
    private static String REPEAT_PREFIX = "repeat_";
    private static Object[] EMPTY_PARMS = new Class[0];
    /**
     * Given an arbitrary object, search for all methods that take a
     * <code>String</code> argument, and have a name prefixed by "do_".
     * Use the name of the method (that portion after "do_") as the name
     * of the command that the method is to process.
     * <P>
     * Example:
     * <pre>
     *    void do_break(String args)
     * </pre>
     * would be invoked to process the command "break".
     * @param processor
     * @param handler
     */
    public static void augmentProcessor(ICommandProcessor processor, final Object handler) {
        Method methods[] = handler.getClass().getMethods();
        for (final Method m:methods){
            if (isCandidate(m)){
                String[] names = extractNames(m);
                final Method repeatMethod = getRepeatMethodFor(handler,names[0]);
                ICommandExecutor executor = new ICommandExecutor(){
                    @Override
                    public void execute (String arguments) throws Exception {
                        invoke(handler,m,new Object[]{arguments});          
                    }
                    @Override
                    public boolean repeat () throws Exception {
                        if (repeatMethod != null){
                            invoke(handler,repeatMethod,EMPTY_PARMS);
                            return true;
                        }
                        return false;
                    }};
                for (String name: names){
                    processor.addCommandExecutor(name,executor);
                }
            }
        }       
    }
    
    private static void invoke(Object thisPointer, Method method, Object[]parms) throws Exception{
        try {
            method.invoke(thisPointer,parms);
        }
        catch (IllegalArgumentException e) {
            //Shouldn't happen
            throw e;
        }
        catch (IllegalAccessException e) {
            //Shouldn't happen
            throw e;
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Exception)
                throw (Exception)t;
            throw e; // must be Error
        }               
    }
    
    /**
     * Return the command that a method is to process.
     * <P>
     * Example: a method named "do_break" processees the command "break".

     * @param m the method.
     * @return the name of the command that the method is to process.
     */
    private static String[] extractNames(Method m){
        String name = m.getName().substring(METHOD_PREFIX.length());
        // if name has embedded "X", then that's the marker of the 
        // minimum number of characters to specify.
        // E.e. "b_reak" is the break command, which requires that at last
        // the first char be specified.
        int iu = name.indexOf('X');
        if (iu > 0){
            int cnt = name.length() - iu;
            String[] result = new String[cnt];
            String minString = name.substring(0,iu);
            result[0] = minString;
            for (int i = 1; i < cnt; i++){
                result[i] = minString + name.substring(iu+1,iu+1+i);
            }
            return result;
        }
        return new String[]{name};
    }

    /**
     * Return whether or not a method is candidate for being a
     * command processor.
     * <P>
     * <ul>
     * <li>
     * It must have a name that begins with "do_".
     * <li>
     * It must return <code>void</code>
     * <li>
     * It must take a single argument of type <code>String</code>.
     * <li>
     * It must be public.
     * </ul>
     * @param m the method.
     * @return whether or not a method is candidate for being a
     * command processor.
     */
    private static boolean isCandidate(Method m){
        if (!m.getName().startsWith(METHOD_PREFIX))
            return false;
        boolean result = true;
        if (m.getReturnType() != Void.TYPE)
            result = false;       
        Class<?>[] args = m.getParameterTypes();
        if (args.length != 1) result = false;
        if (args[0] != String.class) result = false;
        if (!result)
            System.err.println("WARNING: Method " + m.getName() + " lacks expected signature");
        return result;
    }
    
    /**
     * Given an object and a name, look for a parameterless method named
     * "repeat_<name>" and return it if found.
     * @param thisPointer to be searched for repeat method.
     * @param name the base name of the repeat method to look for.
     * @return method the "repeat_<name>" method, or <code>null</code>
     */
    private static Method getRepeatMethodFor(Object thisPointer, String name){
        Method m;
        try {
            m = thisPointer.getClass().getMethod(REPEAT_PREFIX + name);
        }
        catch (Exception e) {
            return null;
        }
        if (m != null){
            if (m.getReturnType() != Void.TYPE || m.getParameterTypes().length != 0){
                System.err.println("WARNING: " + m.getName() + " lacks expected signature");
                m = null;
            }
        }
        return m;
    }
}
