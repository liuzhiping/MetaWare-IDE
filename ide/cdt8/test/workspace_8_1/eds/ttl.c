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
*** File: ttl.c
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
#include <eds.h>
#include <ipc.h>
#include <ipc_pcb.h>
#include <io_pcb.h>
#include <pcb_mqxa.h>
#include "demo.h"

/*==========================================================================*/
/* Configuration for the HOST (EDS Client) */

/* Processor number */
#define HOST_PROCESSOR_ID        MQX_MAX_PROCESSOR_NUMBER

/* Message queues */
#define QUEUE_TO_HOST_PROCESSOR  60


/*==========================================================================*/
/* Configuration for the Target 1 PROCESSOR */
/* Processor number */
#define TARGET1_PROCESSOR_ID      1

/* Message queues */
#define QUEUE_TO_TARGET1_PROCESSOR  61


/*==========================================================================*/
/* Configuration for the Target 2 PROCESSOR */
#define TARGET2_PROCESSOR_ID      2

/* Message queues */
#define QUEUE_TO_TARGET2_PROCESSOR  62


/*==========================================================================*/

/*   Task Templates  */ 
TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { 99,   _ipc_task, 1024+IPC_DEFAULT_STACK_SIZE, 6L, "_ipc_task",
      MQX_AUTO_START_TASK},
   { MUTEXB, MutexB, 0x1290L, 9L, "MutexB",
       0L, 0L, 0L },
   { MUTEXA, MutexA, 0x1290L, 9L, "MutexA",
       0L, 0L, 0L },
   { SEMB, SemB, 0x1290L, 10L, "SemB",
       0L, 0L, 0L },
   { SEMA, SemA, 0x1290L, 9L, "SemA",
       0L, 0L, 0L },
   { EVENTB, EventB, 0x1290L, 9L, "EventB",
       0L, 0L, 0L },
   { EVENTA, EventA, 0x1290L, 9L, "EventA",
       0L, 0L, 0L },
   { BTIMESLICETASK, BTimeSliceTask, 0x1290L, 9L, "BTimeSliceTask",
       MQX_AUTO_START_TASK, 0, 0L },
   { ATIMESLICETASK, ATimeSliceTask, 0x1290L, 9L, "ATimeSliceTask",
       MQX_TIME_SLICE_TASK | MQX_AUTO_START_TASK, 0, 0L },
   { SENDER, Sender, 0x1290L, 10L, "Sender",
       0L, 0L, 0L },
   { RESPONDER, Responder, 0x1290L, 9L, "Responder",
       0L, 0L, 0L },
   { MAIN_TASK, main_task, 0x1290L, 11L, "main_task",
       MQX_AUTO_START_TASK, 0, 0L },
   { 0, 0, 0, 0, 0, 0, 0, 0}
};



/*   MQX Initialization  */ 
MQX_INITIALIZATION_STRUCT  MQX_init_struct = 
{ 
   /* PROCESSOR_NUMBER                */  TARGET1_PROCESSOR_ID,
   /* START_OF_KERNEL_MEMORY          */  BSP_DEFAULT_START_OF_KERNEL_MEMORY,
   /* END_OF_KERNEL_MEMORY            */  BSP_DEFAULT_END_OF_KERNEL_MEMORY,
   /* INTERRUPT_STACK_SIZE            */  32*1024,
   /* TASK_TEMPLATE_LIST              */  (pointer)MQX_template_list,
   /* MQX_HARDWARE_INTERRUPT_LEVEL_MAX*/  BSP_DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX,
   /* MAX_BUFFER_POOLS                */  16, 
   /* MAX_QUEUE_NUMBER                */  128, 
   /* IO_CHANNEL                      */  "",
   /* IO_OPEN_MODE                    */  BSP_DEFAULT_IO_OPEN_MODE
};

IPC_ROUTING_STRUCT _ipc_routing_table[] =
{
   { HOST_PROCESSOR_ID,    HOST_PROCESSOR_ID,    QUEUE_TO_HOST_PROCESSOR   },
//marks@3/04/03   { TARGET2_PROCESSOR_ID, TARGET2_PROCESSOR_ID, QUEUE_TO_TARGET2_PROCESSOR},
   { 0, 0, 0 }
};

/*=========================================================================*/
/* Initialization for host communications */

IO_PCB_MQXA_INIT_STRUCT pcb_mqxa_init_host = 
{
   /* IO_PORT_NAME */          "ittya:",
   /* BAUD_RATE    */          9600,
   /* IS POLLED */             FALSE,
   /* INPUT MAX LENGTH */      IPC_MAX_PACKET_SIZE,
   /* INPUT TASK PRIORITY */   7,
   /* OUPUT TASK PRIORITY */   7
};

IPC_PCB_INIT_STRUCT pcb_init_to_host =
{
   /* IO_PORT_NAME */             "pcb_mqxa_ittya:",
   /* DEVICE_INSTALL? */          _io_pcb_mqxa_install,
   /* DEVICE_INSTALL_PARAMETER*/  (pointer)&pcb_mqxa_init_host,
   /* IN_MESSAGES_MAX_SIZE */     IPC_MAX_PACKET_SIZE,
   /* IN MESSAGES_TO_ALLOCATE */  8,
   /* IN MESSAGES_TO_GROW */      8,
   /* IN_MESSAGES_MAX_ALLOCATE */ 16,
   /* OUT_PCBS_INITIAL */         8,
   /* OUT_PCBS_TO_GROW */         8,
   /* OUT_PCBS_MAX */             16
};

/*=========================================================================*/
/* Initialization for second CPU communications */

//IO_PCB_MQXA_INIT_STRUCT pcb_mqxa_init_target2 = 
//{
//   /* IO_PORT_NAME */          "ittyb:",
//   /* BAUD_RATE    */          9600,
//   /* IS POLLED */             FALSE,
//   /* INPUT MAX LENGTH */      IPC_MAX_PACKET_SIZE,
//   /* INPUT TASK PRIORITY */   7,
//   /* OUPUT TASK PRIORITY */   7
//};


/*=========================================================================*/

IPC_PROTOCOL_INIT_STRUCT _ipc_init_table[] =
{
   { _ipc_pcb_init, &pcb_init_to_host,    "Pcb_to_host",    QUEUE_TO_HOST_PROCESSOR },
//marks@3/04/03   { _ipc_pcb_init, &pcb_init_to_target2, "Pcb_to_target2", QUEUE_TO_TARGET2_PROCESSOR },
   { NULL, NULL, NULL, 0}
};

/* End of File */
