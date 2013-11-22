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
*** File: responde.c
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
   MESSAGE_HEADER_STRUCT_PTR   msg_ptr;

   Responder_Queue_qid = _msgq_open( MSGQ_FREE_QUEUE, SIZE_UNLIMITED);
   if (Responder_Queue_qid == (_queue_id)0){
         /* queue could not be opened */
   }
   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /*
      * Service the message queue - Responder_Queue
      */
      msg_ptr = _msgq_receive(Responder_Queue_qid,(uint_32)NO_TIMEOUT);
      /* process message Respond_msg */
      msg_ptr->SIZE = sizeof(MESSAGE_HEADER_STRUCT);
      msg_ptr->SOURCE_QID = msg_ptr->TARGET_QID;
      msg_ptr->TARGET_QID = Sender_Queue_qid;
      _msgq_send(msg_ptr);

      putchar('.');
   } /* endwhile */ 
} /*end of task*/

/* End of File */
