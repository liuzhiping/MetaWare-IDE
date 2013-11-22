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
*** File: ttl.c
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
#include "lwdemo.h"

/*   Task Templates  */ 
TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { LWSEMB, LWSemB, 0x400, 10, "LWSemB",
       0, 0, 0 },
   { LWSEMA, LWSemA, 0x400, 9, "LWSemA",
       0, 0, 0 },
   { LWEVENTB, LWEventB, 0x400, 9, "LWEventB",
       0, 0, 0 },
   { LWEVENTA, LWEventA, 0x400, 9, "LWEventA",
       0, 0, 0 },
   { SENDER, Sender, 0x400, 10, "Sender",
       0, 0, 0 },
   { RESPONDER, Responder, 0x400, 9, "Responder",
       0, 0, 0 },
   { MAIN_TASK, main_task, 0xC00, 11, "main_task",
       MQX_AUTO_START_TASK, 0, 0 },
   { 0, 0, 0, 0, 0, 0, 0, 0}
};

/*   MQX Initialization  */ 
MQX_INITIALIZATION_STRUCT  MQX_init_struct = 
{ 
   /* PROCESSOR_NUMBER                */  BSP_DEFAULT_PROCESSOR_NUMBER,
   /* START_OF_KERNEL_MEMORY          */  BSP_DEFAULT_START_OF_KERNEL_MEMORY,
   /* END_OF_KERNEL_MEMORY            */  BSP_DEFAULT_END_OF_KERNEL_MEMORY,
   /* INTERRUPT_STACK_SIZE            */  BSP_DEFAULT_INTERRUPT_STACK_SIZE,
   /* TASK_TEMPLATE_LIST              */  (pointer)MQX_template_list,
   /* MQX_HARDWARE_INTERRUPT_LEVEL_MAX*/  BSP_DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX,
   /* MAX_BUFFER_POOLS                */  2, 
   /* MAX_QUEUE_NUMBER                */  12, 
   /* IO_CHANNEL                      */  BSP_DEFAULT_IO_CHANNEL,
   /* IO_OPEN_MODE                    */  BSP_DEFAULT_IO_OPEN_MODE
};


/* End of File */
