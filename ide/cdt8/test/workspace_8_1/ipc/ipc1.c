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
*** File: ipc1.c
***
*** Comments: 
***    This file contains the source for one of the IPC program examples.
***
***    This is a multi-cpu Interprocess Communication Demo.
***    main_task sends and recieves messages to and from a Task on the other
***    CPU or simulated CPU running on a PC. 
***    The demo is currently designed to talk across the first serial port.
***    For more information on how can you run this demo, please refer to
***    "HowTo.txt" document.
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <ipc.h>
#include <ipc_pcb.h>
#include <io_pcb.h>
#include <pcb_mqxa.h>
#include "ipc_ex.h"

extern void main_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
  { IPC_TTN,  _ipc_task, IPC_DEFAULT_STACK_SIZE, 6,
    "_ipc_task", MQX_AUTO_START_TASK, 0, 0},
  { MAIN_TTN, main_task, 2000,                   8,
    "Main",      MQX_AUTO_START_TASK, 0, 0},
  { 0,        0,         0,                      0, 
    0,           0,                   0, 0 }
};

MQX_INITIALIZATION_STRUCT  MQX_init_struct =
{
  TEST_ID,
  BSP_DEFAULT_START_OF_KERNEL_MEMORY,
  BSP_DEFAULT_END_OF_KERNEL_MEMORY,
  BSP_DEFAULT_INTERRUPT_STACK_SIZE,
  (pointer)MQX_template_list,
  BSP_DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX,
  BSP_DEFAULT_MAX_MSGPOOLS,
  BSP_DEFAULT_MAX_MSGQS,
  NULL,
  BSP_DEFAULT_IO_OPEN_MODE
};

IPC_ROUTING_STRUCT _ipc_routing_table[] =
{
   { TEST2_ID, TEST2_ID, QUEUE_TO_TEST2 },
   { 0, 0, 0 }
};

IO_PCB_MQXA_INIT_STRUCT pcb_mqxa_init = 
{
   /* IO_PORT_NAME */          "ittya:",
   /* BAUD_RATE    */          9600,
   /* IS POLLED */             FALSE,
   /* INPUT MAX LENGTH */      sizeof(THE_MESSAGE),
   /* INPUT TASK PRIORITY */   7,
   /* OUPUT TASK PRIORITY */   7
};

IPC_PCB_INIT_STRUCT pcb_init =
{
   /* IO_PORT_NAME */             "pcb_mqxa_ittya:",
   /* DEVICE_INSTALL? */          _io_pcb_mqxa_install,
   /* DEVICE_INSTALL_PARAMETER*/  (pointer)&pcb_mqxa_init,
   /* IN_MESSAGES_MAX_SIZE */     sizeof(THE_MESSAGE),
   /* IN MESSAGES_TO_ALLOCATE */  8,
   /* IN MESSAGES_TO_GROW */      8,
   /* IN_MESSAGES_MAX_ALLOCATE */ 16,
   /* OUT_PCBS_INITIAL */         8,
   /* OUT_PCBS_TO_GROW */         8,
   /* OUT_PCBS_MAX */             16
};

IPC_PROTOCOL_INIT_STRUCT _ipc_init_table[] =
{
   { _ipc_pcb_init, &pcb_init, "Pcb_to_test2", QUEUE_TO_TEST2 },
   { NULL, NULL, NULL, 0}
};

uchar global_endian_swap_def[2] = {4, 0};

/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  : 
*     This task creates a message pool and a message queue then 
*     sends a message to a queue on the second CPU or to the 
*     the simulated CPU.
*     It waits for a return message, validating the message before
*     sending a new message.
*END*-----------------------------------------------------------*/

void main_task
   (
      uint_32 dummy
   ) 
{
   _pool_id        msgpool;
   THE_MESSAGE_PTR msg_ptr;
   _queue_id       qid;
   _queue_id       my_qid;
   uint_32         test_number = 0;

   my_qid  = _msgq_open(MAIN_QUEUE,0);
   qid     = _msgq_get_id(TEST2_ID,RESPONDER_QUEUE);
   msgpool = _msgpool_create(sizeof(THE_MESSAGE), 8, 8, 16);
   while (TRUE) {
      msg_ptr = (THE_MESSAGE_PTR)_msg_alloc(msgpool);
      msg_ptr->HEADER.TARGET_QID = qid;
      msg_ptr->HEADER.SOURCE_QID = my_qid;
      msg_ptr->DATA = test_number;
      putchar('-');
      _msgq_send(msg_ptr);
      msg_ptr = _msgq_receive(MSGQ_ANY_QUEUE, 10000);
      if (msg_ptr == NULL) {
         puts("Receive failed\n");
         _mqx_exit(1);
      } else if (msg_ptr->HEADER.SIZE != sizeof(THE_MESSAGE)) {
         puts("Message wrong size\n");
         _mqx_exit(1);
      } else {
         if (MSG_MUST_CONVERT_DATA_ENDIAN(msg_ptr->HEADER.CONTROL)) {
            _mem_swap_endian(global_endian_swap_def, &msg_ptr->DATA);
         } /* Endif */
         if (msg_ptr->DATA != test_number) {
            puts("Message data incorrect\n");
            _mqx_exit(1);
         } /* Endif */
      } /* Endif */
      _msg_free(msg_ptr);
   } /* Endwhile */
   puts("All complete\n");
   _mqx_exit(0);
} 

/* EOF */
