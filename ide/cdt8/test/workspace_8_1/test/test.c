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
*** File: test.c
***
*** Comments: 
***    This example calls the MQX test functions checking for integrity.
***    The following calls are made:
***     
***    _event_test()
***    _log_test()
***    _lwevent_test()
***    _lwlog_test()
***    _lwsem_test()
***    _lwmem_test()
***    _lwtimer_test()
***    _mem_test_all()
***
*** Expected Results:
*** All tests passed.
***
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>
#include <event.h>
#include <log.h>
#include <lwevent.h>
#include <lwlog.h>
#include <lwmem.h>
#include <lwtimer.h>
#include <message.h>
#include <mutex.h>
#include <name.h>
#include <part.h>
#include <sem.h>
#include <timer.h>
#include <watchdog.h>

extern void background_test_task(uint_32);

TASK_TEMPLATE_STRUCT MQX_template_list[] = 
{
   { 10, background_test_task, 800, 8, "Main",
      MQX_AUTO_START_TASK, 0, 0},
   { 0,  0,               0,   0, 0,
      0,                   0, 0}
};

/*TASK*----------------------------------------------------------
*
* Task Name : background_test_task
* Comments  :
*   This task is meant to run in the background testing for
* integrity of MQX component datastructures.
*END*-----------------------------------------------------------*/

void background_test_task
   (
      uint_32  parameter
   )
{
   _partition_id  partition;
   _lwmem_pool_id lwmem_pool_id;
   pointer        error_ptr;
   pointer        error2_ptr;
   _mqx_uint      error;
   _mqx_uint      result;

   while (TRUE) {
      result = _event_test(&error_ptr);
      if (result != MQX_OK){
         printf("\nFailed _event_test: 0x%X.", result);
         _mqx_exit(1);
      } 
      result = _log_test(&error);
      if (result != MQX_OK){
         printf("\nFailed _log_test: 0x%X.", result);
         _mqx_exit(2);
      }
      result = _lwevent_test(&error_ptr, &error2_ptr);
      if (result != MQX_OK){
         printf("\nFailed _lwevent_test: 0x%X.", result);
         _mqx_exit(3);
      }
      result = _lwlog_test(&error);
      if (result != MQX_OK){
         printf("\nFailed _lwlog_test: 0x%X.", result);
         _mqx_exit(4);
      }
      result = _lwsem_test(&error_ptr, &error2_ptr);
      if (result != MQX_OK){
         printf("\nFailed _lwsem_test: 0x%X.", result);
         _mqx_exit(5);
      }
      result = _lwmem_test(&lwmem_pool_id, &error_ptr);
      if (result != MQX_OK){
         printf("\nFailed _lwmem_test: 0x%X.", result);
         _mqx_exit(6);
      }
      result = _lwtimer_test(&error_ptr, &error2_ptr);
      if (result != MQX_OK){
         printf("\nFailed _lwtimer_test: 0x%X.", result);
         _mqx_exit(7);
      }
      result = _mem_test_all(&error_ptr);
      if (result != MQX_OK){
         printf("\nFailed _mem_test_all,");
         printf("\nError = 0x%X, pool = 0x%X.", result,
            (_mqx_uint)error_ptr);
         _mqx_exit(8);
      }
      /*
      ** Create the message component.
      ** Verify the integrity of message pools and message queues.
      */
      if (_msg_create_component() != MQX_OK){
         printf("\nError creating the message component.");
         _mqx_exit(9);
      }
      if (_msgpool_test(&error_ptr, &error2_ptr) != MQX_OK){
         printf("\nFailed _msgpool_test.");
         _mqx_exit(10);
      }
      if (_msgq_test(&error_ptr, &error2_ptr) != MQX_OK){
         printf("\nFailed _msgq_test.");
         _mqx_exit(11);
      }
      if (_mutex_test(&error_ptr) != MQX_OK){
         printf("\nFailed _mutex_test.");
         _mqx_exit(12);
      }
      if (_name_test(&error_ptr, &error2_ptr) != MQX_OK){
         printf("\nFailed _name_test.");
         _mqx_exit(13);
      }
      if (_partition_test(&partition, &error_ptr, &error2_ptr) 
         != MQX_OK)
      {
         printf("\nFailed _partition_test.");
         _mqx_exit(14);
      }
      if (_sem_test(&error_ptr) != MQX_OK){
         printf("\nFailed _sem_test.");
         _mqx_exit(15);
      }
      if (_taskq_test(&error_ptr, &error2_ptr) != MQX_OK){
         printf("\nFailed _takq_test.");
         _mqx_exit(16);
      }
      if (_timer_test(&error_ptr) != MQX_OK){
         printf("\nFailed _timer_test.");
         _mqx_exit(17);
      }
      if (_watchdog_test(&error_ptr, &error2_ptr) != MQX_OK){
         printf("\nFailed _watchlog_test.");
         _mqx_exit(18);
      }
      printf("All tests passed.");
      _mqx_exit(0);
   }

}

/* EOF */
