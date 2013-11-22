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
*** File: sender.c
*** 
*** Comments:
***    This file contains source for the Lightweight MQX demo test.
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <errno.h>
#include <lwevent.h>
#include <lwmsgq.h>
#include "lwdemo.h"

/*   Task Code -  Sender     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  Sender
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void Sender
   (
      uint_32   parameter
   )
{
   _mqx_uint				   msg[MSG_SIZE];
   _task_id                    created_task;
   _mqx_uint				   i = 500;
   
   created_task = _task_create(0, RESPONDER, 0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }

   /*
   * Service the message queue - Sender_Queue
   */
   _lwmsgq_receive((pointer)sender_queue, msg, LWMSGQ_RECEIVE_BLOCK_ON_EMPTY, 0, 0);
   _lwmsgq_send((pointer)responder_queue, msg, LWMSGQ_SEND_BLOCK_ON_FULL);   

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /*
      * Service the message queue - Sender_Queue
      */
      _lwmsgq_receive((pointer)sender_queue, msg, 0, LWMSGQ_RECEIVE_BLOCK_ON_EMPTY, 0);
      _time_delay_ticks(3);
      _lwmsgq_send((pointer)responder_queue, msg, LWMSGQ_SEND_BLOCK_ON_FULL);
   } /* endwhile */ 

} /*end of task*/

/* End of File */
