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
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;


/**
 * Part of two programs that receives packets in order to test speed.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Test2 {

    public static void main (String[] args) {
        if (args.length == 0 || args.length > 2){
            System.err.println("Args are: [<ipaddess>] <port>");
            return;
        }
        int port;
        String ipAddress = args.length == 2?args[0]:"localhost";
        
        try {
            port = Integer.parseInt(args[args.length-1]);
        }
        catch (NumberFormatException e) {
            System.err.println("Bad port number: " + args[0]);
            return;
        }
        SocketTransport soc = new SocketTransport();
        try {
            soc.attach(ipAddress,port);
        }
        catch (IOException e) {
            System.err.println("Failed to attach to " + ipAddress + "@" + port+ ": " + e.getMessage());
            return;
        }
        IConnection c = ConnectionFactory.makeConnection(soc);
        int cnt = 0;
        int byteCount = 0;
        long startTime = System.currentTimeMillis();
        while (!c.isDisconnected()){
            try {
                ScwpCommandPacket cmd = c.readCommand();
                ScwpReplyPacket reply = new ScwpReplyPacket(cmd.getId());
                byte[] data = cmd.getData();
                byteCount += data.length;
                reply.setData(data);
                c.sendReply(reply);
                cnt++;
            }
            catch (Exception e) {
                System.err.println("Failure: " + e.getMessage());
                break;
            }
        }
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("CLIENT: " + cnt + " packets received of " + byteCount + " bytes");
        System.out.println("CLIENT: Rate is " + (double)cnt/elapsed*1000 + " packets/sec");
        System.out.println("              = " + (double)byteCount/1000/elapsed*1000 + " KB/sec");
    }
}
