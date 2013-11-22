/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2003 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: Sender.c
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <errno.h>
#include <mutex.h>
#include <sem.h>
#include <event.h>
#include <log.h>
#include <klog.h>
#include "demo.h"

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
   MESSAGE_HEADER_STRUCT_PTR   msg_ptr;
   _task_id    created_task;

   Sender_Queue_qid = _msgq_open( MSGQ_FREE_QUEUE, SIZE_UNLIMITED);
   if (Sender_Queue_qid == (_queue_id)0){
         /* queue could not be opened */
   }
   created_task = _task_create(0, (uint_32)RESPONDER, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /*
      * Service the message queue - Sender_Queue
      */
      msg_ptr = _msgq_receive(Sender_Queue_qid,(uint_32)NO_TIMEOUT);
      /* process message Start_msg */
      msg_ptr->SIZE = sizeof(MESSAGE_HEADER_STRUCT);
      msg_ptr->SOURCE_QID = msg_ptr->TARGET_QID;
      msg_ptr->TARGET_QID = Responder_Queue_qid;
      _msgq_send(msg_ptr);

      /* 
      ** LOOP - 
      */
      while ( TRUE ) {
         /*
         * Service the message queue - Sender_Queue
         */
         msg_ptr = _msgq_receive(Sender_Queue_qid,(uint_32)NO_TIMEOUT);
         /* process message Loop_msg */
         _time_delay((uint_32)15);
         msg_ptr->SIZE = sizeof(MESSAGE_HEADER_STRUCT);
         msg_ptr->SOURCE_QID = msg_ptr->TARGET_QID;
         msg_ptr->TARGET_QID = Responder_Queue_qid;
         _msgq_send(msg_ptr);

      } /* endwhile */ 
      /*
      * Service the message queue - Sender_Queue
      */
      msg_ptr = _msgq_receive(Sender_Queue_qid,(uint_32)NO_TIMEOUT);
      /* process message Done_msg */
      msg_ptr->SIZE = sizeof(MESSAGE_HEADER_STRUCT);
      msg_ptr->SOURCE_QID = msg_ptr->TARGET_QID;
      msg_ptr->TARGET_QID = Main_Queue_qid;
      _msgq_send(msg_ptr);

   } /* endwhile */ 
} /*end of task*/

/* End of File */
