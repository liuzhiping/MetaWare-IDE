/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: client.c
***
*** Comments: 
***    This file contains the code for the client_task
*** 
***************************************************************************
*END**********************************************************************/

#include <string.h>
#include <mqx.h>
#include <bsp.h>
#include <lwmsgq.h>
#include "server.h"


/*TASK*--------------------------------------------------------
*
* Task Name : client_task
* Comments  : This task sends a message to the server_task and
*   then waits for a reply.
*END*--------------------------------------------------------*/

void client_task
   (
      uint_32 index
   )
{
   _mqx_uint		  msg[MSG_SIZE];
  
   while (TRUE) {
      msg[0] = ('A'+ index);
     
      printf("Client Task %ld\n", index);
      _lwmsgq_send((pointer)server_queue, msg, LWMSGQ_SEND_BLOCK_ON_FULL);

      _time_delay_ticks(1);
      
      /* wait for a return message */
      _lwmsgq_receive((pointer)client_queue, msg, LWMSGQ_RECEIVE_BLOCK_ON_EMPTY, 0, 0);
   }

}

/* EOF */
