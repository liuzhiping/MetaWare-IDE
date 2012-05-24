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
package com.arc.seecode.server;

import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;


/**
 * @author David Pickens
 */
interface IPacketSender {
    /**
     * Send command packet, and wait for reply.
     * @param cmd the command to send.
     * @return the reply.
     * @throws TimeoutException usually caused by timeout due to dropped connection.
     */
    ScwpReplyPacket sendPacket(ScwpCommandPacket cmd) throws TimeoutException;
}
