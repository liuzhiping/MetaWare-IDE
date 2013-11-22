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
***
***    This example demonstrates EDS data collection for a single CPU.
***    The example is the same as the demo example except there is no
***    output. Once running, use the EDS Client to view MQX stats.
***
***    The EDS component communicates to the EDS client over the serial 
***    port, so this example only runs on hardware.
***
***  Expected Output: none
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
#include <part.h>
#include "demo.h"

/* Function prototypes */
extern _mqx_uint _name_add(char _PTR_, _mqx_max_type);
extern _mqx_uint _name_create_component(_mqx_uint, _mqx_uint, _mqx_uint);
extern _partition_id _partition_create(_mem_size, _mqx_uint, _mqx_uint, _mqx_uint);

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
   uint_32         log_result;
   _task_id    created_task;
   MESSAGE_HEADER_STRUCT_PTR   msg_ptr;
   uint_32         event_result;
   uint_32         sem_result;
   uint_32         eds_result;

   printf("\nHello from main_task().");

   /* create the EDS component */
   eds_result = _eds_create_component();
   if (eds_result != MQX_OK) { 
      /* eds serial component could not be created */
   } /* endif */

   /* create the log component */
   log_result = _log_create_component();
   if (log_result != MQX_OK) { 
      /* log component could not be created */
   } /* endif */
   
   /* Create the partition component */
   if (_partition_create(0x2000, 5, 2, 20) != MQX_OK) {
      /* an error has been detected */
   } /* Endif */

   /* Create the mutex component */
   if (_mutex_create_component() != MQX_OK) {
      /* an error has been detected */
   } /* Endif */

   /* create the event component */
   event_result = _event_create_component((uint_32)EVENT_INITIAL_NUMBER,
                          (uint_32)EVENT_GROWTH,(uint_32)EVENT_MAXIMUM);
   if (event_result != MQX_OK) { 
      /* event component could not be created */
   } /* endif */
   /* create the semaphore component */
   sem_result = _sem_create_component((uint_32)SEM_INITIAL_NUMBER,
                          (uint_32)SEM_GROWTH, (uint_32)SEM_MAXIMUM);
   if (sem_result != MQX_OK) { 
      /* semaphore component could not be created */
   } /* endif */
      /* Create the name component */
   if (_name_create_component(3, 1, 6) != MQX_OK) {
      /* an error has been detected */
   }/* Endif */
   _name_add("name1", 0x1001);
   _name_add("name2", 0x1002);
   _name_add("name3", 0x1003);

   MsgPool_pool_id = _msgpool_create(sizeof(MESSAGE_HEADER_STRUCT), 10, 0, 0);
   if (MsgPool_pool_id == MSGPOOL_NULL_POOL_ID) { 
      /* _msgpool_create did not succeed */ 
   } 
   Main_Queue_qid = _msgq_open(MSGQ_FREE_QUEUE, SIZE_UNLIMITED);
   if (Main_Queue_qid == (_queue_id)0){
         /* queue could not be opened */
   }
   created_task = _task_create(0, (uint_32)SENDER, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, (uint_32)MUTEXA, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, (uint_32)MUTEXB, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, (uint_32)SEMA, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, (uint_32)SEMB, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, (uint_32)EVENTA, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   created_task = _task_create(0, (uint_32)EVENTB, (uint_32)0);
   if (created_task == MQX_NULL_TASK_ID) {
      /* task creation failed */
   }
   /* create kernel log */
   log_result = _klog_create((uint_32)1200,(uint_32)0);
   if (log_result != MQX_OK) { 
      /* klog could not be created */
   } /* endif */
   /* define kernel logging */
   _klog_control((uint_32)0xffffffff,FALSE);
   _klog_control(
                    KLOG_ENABLED    |
                    KLOG_FUNCTIONS_ENABLED    |
                    KLOG_INTERRUPTS_ENABLED    |
                    KLOG_SYSTEM_CLOCK_INT_ENABLED    |
                    KLOG_CONTEXT_ENABLED    |
                    KLOG_TASKING_FUNCTIONS    |
                    KLOG_ERROR_FUNCTIONS    |
                    KLOG_MESSAGE_FUNCTIONS    |
                    KLOG_INTERRUPT_FUNCTIONS    |
                    KLOG_MEMORY_FUNCTIONS    |
                    KLOG_TIME_FUNCTIONS    |
                    KLOG_EVENT_FUNCTIONS    |
                    KLOG_NAME_FUNCTIONS    |
                    KLOG_MUTEX_FUNCTIONS    |
                    KLOG_SEMAPHORE_FUNCTIONS    |
                    KLOG_WATCHDOG_FUNCTIONS    , TRUE);
   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      msg_ptr = _msg_alloc ((_pool_id) MsgPool_pool_id );
      if (msg_ptr == NULL) { 
         /* No available message buffer */ 
      } 
      msg_ptr->SIZE = sizeof(MESSAGE_HEADER_STRUCT);
      msg_ptr->SOURCE_QID = msg_ptr->TARGET_QID;
      msg_ptr->TARGET_QID = Sender_Queue_qid;
      _msgq_send(msg_ptr);

      /*
      * Service the message queue - Main_Queue
      */
      msg_ptr = _msgq_receive(Main_Queue_qid,(uint_32)NO_TIMEOUT);
      /* process message End_msg */
      _msg_free(msg_ptr);

   } /* endwhile */ 
} /*end of task*/

/* End of File */
