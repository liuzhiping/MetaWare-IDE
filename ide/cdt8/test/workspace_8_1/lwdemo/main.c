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
*** File: main.c
*** 
*** Comments:
***    This file contains source for the Lightweight MQX demo example.
***
***    The lightweight Demo performs the same tasks as demo, but uses
***    lightweight components. This demonstrates many MQX RTOS components
***    in a single application. It uses message queues, semaphores, mutexs
***    and events. In the case of lwdemo, these are all lightweight components.
***    This demo is discussed in more detail in the MQX RTOS Getting Started Guide.
***
***    Expected Output:
***
***    MQX 2.51
***    Hello from main_task()..............................................
***
***    Dots continue until demo is terminated.
***
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <errno.h>
#include <lwevent.h>
#include <lwmsgq.h>
#if MQX_KERNEL_LOGGING
#include <klog.h>
#endif
#if MQX_USE_LOGS
#include <log.h>
#endif
#include "lwdemo.h"


/*   Task Code -  Main     */

/*TASK---------------------------------------------------------------
*   
* Task Name   :  Main
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void main_task
   (
      uint_32   parameter
   )
{
   _mqx_uint				 msg[MSG_SIZE];
   _task_id                  created_task;
#if MQX_USE_LOGS
   _mqx_uint                 log_result;
#endif
   _mqx_uint                 result;
   _mqx_uint				 i = 0;

   _int_install_unexpected_isr();
   printf("\nMQX %s\n",_mqx_version);
   printf("Hello from main_task().");

#if MQX_USE_LOGS
   /* create the log component */
   log_result = _log_create_component();
   if (log_result != MQX_OK) { 
      /* log component could not be created */
   } /* endif */
#endif
   /* create lwevent group */
   result = _lwevent_create(&lwevent,0);
   if (result != MQX_OK) { 
      /* event component could not be created */
   } /* endif */
   
   /* create a lwsem */
   result = _lwsem_create(&lwsem, 10);
   if (result != MQX_OK) {
      /* semaphore component could not be created */
   } /* endif */

   _lwmsgq_init((pointer)main_queue, NUM_MESSAGES, MSG_SIZE);
   _lwmsgq_init((pointer)sender_queue, NUM_MESSAGES, MSG_SIZE);
   _lwmsgq_init((pointer)responder_queue, NUM_MESSAGES, MSG_SIZE);

   created_task = _task_create(0, SENDER, 0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, LWSEMA, 0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, LWSEMB, 0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, LWEVENTA, 0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, LWEVENTB, 0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }

#if MQX_KERNEL_LOGGING == 1
   /* create log number 0 */
   log_result = _klog_create(1200, 0);
   if (log_result != MQX_OK) { 
      /* log 0 could not be created */
   } /* endif */

   /* define kernel logging */
   _klog_control(0xFFFFFFFF, FALSE);
   _klog_control(
      KLOG_ENABLED                  |
      KLOG_FUNCTIONS_ENABLED        |
      KLOG_INTERRUPTS_ENABLED       |
      KLOG_SYSTEM_CLOCK_INT_ENABLED |
      KLOG_CONTEXT_ENABLED          |
      KLOG_TASKING_FUNCTIONS        |
      KLOG_ERROR_FUNCTIONS          |
      KLOG_MESSAGE_FUNCTIONS        |
      KLOG_INTERRUPT_FUNCTIONS      |
      KLOG_MEMORY_FUNCTIONS         |
      KLOG_TIME_FUNCTIONS           |
      KLOG_EVENT_FUNCTIONS          |
      KLOG_NAME_FUNCTIONS           |
      KLOG_MUTEX_FUNCTIONS          |
      KLOG_SEMAPHORE_FUNCTIONS      |
      KLOG_WATCHDOG_FUNCTIONS, 
      TRUE
      );
#endif

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      _lwmsgq_send((pointer)sender_queue, msg, LWMSGQ_SEND_BLOCK_ON_FULL);
      _lwmsgq_receive((pointer)main_queue, msg, LWMSGQ_RECEIVE_BLOCK_ON_EMPTY, 0, 0);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
