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
*** File: mutexa.c
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

/*   Task Code -  MutexA     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  MutexA
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void MutexA
   (
      uint_32   parameter
   )
{
   MUTEX_ATTR_STRUCT      mutex_init;

   /* initialize a mutex Mutex1 */
   if (_mutatr_init(&mutex_init) == MQX_EOK) {
      _mutatr_set_wait_protocol(&mutex_init,MUTEX_QUEUEING);
      _mutatr_set_sched_protocol(&mutex_init,MUTEX_NO_PRIO_INHERIT);
      _mutex_init(&Mutex1,&mutex_init);
   }

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      if (_mutex_lock(&Mutex1) != MQX_EOK) { 
         /* an error occurred */
      }

      /* access shared resource */

      _time_delay((uint_32)5);
      _mutex_unlock(&Mutex1);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
