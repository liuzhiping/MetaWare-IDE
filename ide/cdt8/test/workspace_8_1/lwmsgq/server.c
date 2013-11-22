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
***    when each task responds. Uses lightweight messages.
***
*** Expected Output:
*** Client Task 0
*** Client Task 1
*** Client Task 2
*** A
*** B
*** C
*** Client Task 2
*** Client Task 1
*** Client Task 0
*** C
*** B
*** A
***
*** Pattern Repeats until terminated
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <lwmsgq.h>
#include "server.h"

/*TASK*--------------------------------------------------------
*
* Task Name : server_task
* Comments  : This task initializes the message queues,
*  creates three client tasks, and then waits for a message.
*  After recieving a message, the task returns the message to 
*  the sender.
*END*--------------------------------------------------------*/

void server_task 
   (
      uint_32 param
   )
{
   _mqx_uint		  msg[MSG_SIZE];
   _mqx_uint          i;
   _mqx_uint          result;

   result = _lwmsgq_init((pointer)server_queue, NUM_MESSAGES, MSG_SIZE);
   if (result != MQX_OK) {
      // lwmsgq_init failed
   } /* Endif */
   result = _lwmsgq_init((pointer)client_queue, NUM_MESSAGES, MSG_SIZE);
   if (result != MQX_OK) {
      // lwmsgq_init failed
   } /* Endif */
   
   /* create the client tasks */
   for (i = 0; i < NUM_CLIENTS; i++) {
      _task_create(0, CLIENT_TASK, (uint_32)i);
   }
      
   while (TRUE) {
      _lwmsgq_receive((pointer)server_queue, msg, LWMSGQ_RECEIVE_BLOCK_ON_EMPTY, 0, 0);
      printf(" %c \n", msg[0]);
      
      _lwmsgq_send((pointer)client_queue, msg, LWMSGQ_SEND_BLOCK_ON_FULL);
   }

}
      
/* EOF */
