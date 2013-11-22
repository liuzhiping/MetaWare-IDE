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
package com.arc.mw.util;

/**
 * A global class by which trace messages may be logged.
 */
public class Log {
    private static ILogger sLogger;
    
    /**
     * A prefix to each log. This is necessary if we're running
     * more than one program and we need to know to which the
     * log applies.
     */
    private static String sPrefix = null;
    
    /**
     * Return the logger object. Creates a default if one hasn't
     * been set.
     * @return ILogger
     */
    public static ILogger getLogger() {
        if (sLogger == null)
            setLogger(new ILogger() {
            @Override
            public void log(String fromWhere, String message) {
                if (sPrefix != null) fromWhere = sPrefix + ":" + fromWhere;
                System.out.println("[" + fromWhere + "] " + message);
                System.out.flush();
            }
        });
        return sLogger;
    }
    
    /**
     * Set a prefix to be applied to the log message (assuming our
     * default implementation is being used).
     * <P>
     * This is necessary to distinguish messages if more than one
     * process is running simultaneously, and they are each emitting
     * log message.
     * @param prefix a prefix string to be prepended to each log message.
     */
    public static void setPrefix(String prefix){
        sPrefix = prefix;       
    }

    /**
     * Set the logger object.
     * 
     * @param log
     */
    public static void setLogger(ILogger log) {
        assert log != null;
        sLogger = log;
    }
 
    /**
     * Write a log message.
     * @param fromWhere
     * @param message
     */   
    public static void log(String fromWhere, String message){
        getLogger().log(fromWhere,message);
    }

}
