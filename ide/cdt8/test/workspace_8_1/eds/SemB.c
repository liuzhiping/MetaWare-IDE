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
*** File: Semb.c
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

/*   Task Code -  SemB     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  SemB
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void SemB
   (
      uint_32   parameter
   )
{
   uint_32         sem_result;
   pointer   Sem1_handle;

   /* open connection to semaphore sem.Sem1 */
   sem_result = _sem_open("sem.Sem1",&Sem1_handle);
   if (sem_result != MQX_OK) { 
      /* could not open sem.Sem1  */
   } /* endif */
   
   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /* wait for semaphore sem.Sem1 */
      sem_result = _sem_wait(Sem1_handle,(uint_32)NO_TIMEOUT);
      if (sem_result != MQX_OK) { 
         /* waiting on semaphore sem.Sem1 failed */
      }  
      /* semaphore obtained, perform work */
      _time_delay((uint_32)5);
      /* semaphore protected work done, release semaphore */
      sem_result = _sem_post(Sem1_handle);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
