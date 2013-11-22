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
*** File: responde.c
*** 
*** Comments:
***    This file contains source for the Lightweight MQX demo test.
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <errno.h>
#include <lwevent.h>
#include <lwmsgq.h>
#include "lwdemo.h"

/*   Task Code -  Responder     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  Responder
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void Responder
   (
      uint_32   parameter
   )
{
   uint_32		msg[MSG_SIZE];
   _mqx_uint	i = 100;

   /*
   ** LOOP - 
   */
   while ( TRUE ) {
      /*
      * Service the message queue - Responder_Queue
      */
      _lwmsgq_receive((pointer)responder_queue, msg, LWMSGQ_RECEIVE_BLOCK_ON_EMPTY, 0, 0);
      _lwmsgq_send((pointer)sender_queue, msg, LWMSGQ_SEND_BLOCK_ON_FULL);

      putchar('.');
   } /* endwhile */
} /*end of task*/

/* End of File */
