/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: server.c
***

*** Comments: 
***    This passes messages between 3 other tasks. It prints output
***    when each task responds. 
***
*** Expected Output:
*** Client Task 0
*** Client Task 1
*** Client Task 2
*** A
*** B
*** C
***
*** This sequence repeats until terminated
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include "server.h"

/* declaration of a global message pool */
_pool_id   message_pool;

/*TASK*--------------------------------------------------------
*
* Task Name : server_task
* Comments  : This task creates a message queue for itself,
*  creates a message pool, creates three client tasks, and 
*  then waits for a message.  After recieving a message, 
*  the task returns the message to the sender. 
*END*--------------------------------------------------------*/

void server_task 
   (
      uint_32 param
   )
{
   SERVER_MESSAGE_PTR msg_ptr;
   _mqx_uint          i;
   _queue_id          server_qid;

   /* open a message queue */
   server_qid = _msgq_open(SERVER_QUEUE, 0);

   /* create a message pool */   
   message_pool = _msgpool_create(sizeof(SERVER_MESSAGE), 
      NUM_CLIENTS, 0, 0);

   /* create the client tasks */
   for (i = 0; i < NUM_CLIENTS; i++) {
      _task_create(0, CLIENT_TASK, (uint_32)i);
   }
      
   while (TRUE) {
      msg_ptr = _msgq_receive(server_qid, 0);
      printf(" %c \n", msg_ptr->DATA[0]);
  
      /* return the message */   
      msg_ptr->HEADER.TARGET_QID = msg_ptr->HEADER.SOURCE_QID;
      msg_ptr->HEADER.SOURCE_QID = server_qid;
      _msgq_send(msg_ptr);
   }
}
      
/* EOF */
