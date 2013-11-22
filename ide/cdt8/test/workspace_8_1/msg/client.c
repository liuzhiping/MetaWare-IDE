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
#include "server.h"


/*TASK*--------------------------------------------------------
*
* Task Name : client_task
* Comments  : This task opens a message queue and
*   allocates a message from the message pool.
*   The message is sent to the server_task and
*   a reply is waited for. The reply message is then freed.
*END*--------------------------------------------------------*/

void client_task 
   (
      uint_32 index
   )
{
   SERVER_MESSAGE_PTR msg_ptr;
   _queue_id          client_qid;
   boolean            result;

   client_qid  = _msgq_open((_queue_number)(CLIENT_QUEUE_BASE +
      index), 0);

   if (client_qid == 0) {
      printf("\nCould not open a client message queue\n");
      _mqx_exit(0);
   }
   
   while (TRUE) {
      /*allocate a message*/
      msg_ptr = (SERVER_MESSAGE_PTR)_msg_alloc(message_pool);

      if (msg_ptr == NULL) {
         printf("\nCould not allocate a message\n");
         _mqx_exit(0);
      }

      msg_ptr->HEADER.SOURCE_QID = client_qid;      
      msg_ptr->HEADER.TARGET_QID = _msgq_get_id(0, SERVER_QUEUE);
      msg_ptr->HEADER.SIZE = sizeof(MESSAGE_HEADER_STRUCT) + 
         strlen((char_ptr)msg_ptr->DATA) + 1;
      msg_ptr->DATA[0] = ('A'+ index);
     
      printf("Client Task %ld\n", index);  
      
      result = _msgq_send(msg_ptr);
      
      if (result != TRUE) {
         printf("\nCould not send a message\n");
         _mqx_exit(0);
      }
   
      /* wait for a return message */
      msg_ptr = _msgq_receive(client_qid, 0);
      
      if (msg_ptr == NULL) {
         printf("\nCould not receive a message\n");
         _mqx_exit(0);
      }
    
       /* free the message */
      _msg_free(msg_ptr);
   }

}

/* EOF */
