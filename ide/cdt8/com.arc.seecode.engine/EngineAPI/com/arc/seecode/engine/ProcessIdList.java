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
package com.arc.seecode.engine;

import java.util.ArrayList;
import java.util.List;

import com.arc.seecode.connect.ICommandReceiverRouter;
import com.arc.seecode.scwp.ScwpCommandPacket;


/**
 * A data structure for holding a list of CMPD process IDs.
 * They are encode as ranges separated by commas. E.g.:
 *    1,3,5:8,9
 * @author dpickens
 *
 */
public class ProcessIdList {
    
    public static final int MAX_PID = ICommandReceiverRouter.MAX_OBJECT_IDS / ScwpCommandPacket.REQUIRED_CHANNELS - 1;
    public ProcessIdList(){}
    
    public void add(int id){
        list.add(new Range(id,id));
    }
    
    public void addRange(int first, int last){
        list.add(new Range(first,last));
    }
    
    public String getEncoding(){
        StringBuilder buf = new StringBuilder();
        for (Range r: list) {
            if (buf.length() > 0) buf.append(",");
            buf.append(r.first);
            if (r.first != r.last) {
                buf.append(":");
                buf.append(r.last);
            }
        }
        return buf.toString();
    }
    
    public boolean doesOverlap(ProcessIdList other){
        for (Range r1: other.list) {
            for (Range r2: this.list){
                if (r1.first <= r2.first && r1.last >= r2.first) return true;
                if (r2.first <= r1.first && r2.last >= r1.first) return true;
            }
        }
        return false;
    }
    public int getCount(){
        int cnt = 0;
        for (Range r: list){
            cnt += r.last-r.first+1;
        }
        return cnt;
    }
    
    public int getHighestID(){
        int max = 0;
        for (Range r: this.list){
            max = Math.max(r.last, max);
        }
        return max;
    }
    public Range[] getRanges() {
        return list.toArray(new Range[list.size()]);
    }
    public static class Range {
        private int first, last;
        public int getFirst() { return first; }
        public int getLast() { return last; }
        Range(int f, int l) { first = f; last = l; }
    }
    private List<Range> list = new ArrayList<Range>();
    
    /**
     * Compute process list from string. E.g. "1,4,5:8,9"
     * @param s
     * @return
     */
    public static ProcessIdList create(String encoding) throws NumberFormatException{
        ProcessIdList list = new ProcessIdList();
        for (String s: encoding.split(",")){
            if (s.indexOf(':') > 0){
                String rr[] = s.split(":");
                if (rr.length != 2) throw new NumberFormatException("Invalid range: " + s);
                int lo = Integer.parseInt(rr[0]);
                int hi = Integer.parseInt(rr[1]);
                if (lo <= 0 || lo > hi) {
                    throw new NumberFormatException("Invalid range: " + s);
                }
                if (hi > MAX_PID){
                    throw new NumberFormatException("Process ID must not exceed " + MAX_PID);
                }
                list.addRange(lo,hi);
            }
            else {
                int i = Integer.parseInt(s);
                if (i <= 0) throw new NumberFormatException("Process ID must be positive integer constant");
                if (i > MAX_PID){
                    throw new NumberFormatException("Process ID must not exceed " + MAX_PID);
                }
                list.add(i);
            }
        }
        return list;
    }
}
