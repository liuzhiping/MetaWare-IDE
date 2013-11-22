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
*** File: mutexb.c
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

/*   Task Code -  MutexB     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  MutexB
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void MutexB
   (
      uint_32   parameter
   )
{

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
