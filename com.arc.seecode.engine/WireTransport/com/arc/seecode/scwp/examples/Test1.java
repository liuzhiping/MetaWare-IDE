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
package com.arc.seecode.scwp.examples;

import java.io.IOException;

import com.arc.seecode.connect.ConnectionFactory;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.scwp.ScwpCommandPacket;


/**
 * A program to test the performance of our packet transport.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version 1.0
 * @lastModified 11/19/04
 * @lastModifiedBy David Pickens
 * @reviewed 0 $Revision:1$
 */
public class Test1 {

    public static void main (String[] args) {
        if (args.length == 0 || args.length > 3){
            System.out.println("Arguments are: <packet-count> <packet-size> <port>");
            return;
        }
        int count;
        int size;
        int port;
        try {
            count = Integer.parseInt(args[0]);
            size = Integer.parseInt(args[1]);
            port = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            System.err.println("Bad integer:"+e.getMessage());
            return;
        }
        byte data[] = new byte[size];
        for (int i = 0; i < size; i++){
            data[i] = (byte)i;
        }
        SocketTransport soc = new SocketTransport();
        try {
            soc.listen(port);
            soc.accept();
        }
        catch (IOException e) {
            System.err.println("Connection failure on port " + port+": "+ e.getMessage());
            return;
        }
        IConnection c = ConnectionFactory.makeConnection(soc);
        
       
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++){
            ScwpCommandPacket packet = new ScwpCommandPacket(1,""+i);
            packet.setData(data);           
            try {
                c.sendCommand(packet);
            }
            catch (TimeoutException e1) {
                System.err.println("Connection timed out");
                break;
            }
            catch (VMDisconnectedException e1) {
                System.err.println("Connection disconnected");
                break;
            }           
        }
        c.shutdown();
        long elapsed = System.currentTimeMillis()-startTime;
        System.out.println("Server: " + count + " packets of " + size + " bytes in " + elapsed + " msec");
        System.out.println("Server: Rate = " + (double)count/elapsed*1000 + " packets/second");
        System.out.println("Server:      = " + (double)(count*size)/1000/elapsed*1000 + " KB/second");
    }
}
