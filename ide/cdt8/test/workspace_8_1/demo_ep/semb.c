/**********************************************************************
*    This file was generated by Design Tool a product of: 
*       ARC International  
*       http://www.ARC.com/         
*                              
*       Mon Mar 12 09:32:22 2007
*   
*       Copyright (c) ARC International  
*       All rights reserved    
*   
*       This software embodies materials and concepts which are
*       confidential to ARC International and is made
*       available solely pursuant to the terms of a written license
*       agreement with ARC International.
*   
*   ********************************************************************/
/* 
*  Resources  -   Number -  Memory Required
*  Kernel       -   N/A       -  0x274c
*  Tasks        - 11       -  0x2530
*  Msg Pools    -  1 of 2  -   0x1a4
*  Msg Queues   -  3 of 12   -  0x1a0
*  Mutexes       -  1    -  0x24
*  Semaphores   -  1 of 10  -  0x280
*  Events       -  1 of 10   -    0x2a0
*  Logs         -  1 of 16    -  0x1344
*  	   Total:  0x6848 or 26696
*/ 
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
      uint_32  parameter
   )
{
   _mqx_uint         sem_result;
   pointer   Sem1_handle;

   /* open connection to semaphore sem.Sem1 */
   sem_result = _sem_open("sem.Sem1", &Sem1_handle);
   if (sem_result != MQX_OK) {
      /* could not open semaphore */
   } /*Endif*/
   
   /*
   ** LOOP - 
   */
   while (TRUE) {
      /* wait for semaphore sem.Sem1 */
#if GDVFS_DELAY
	  Loadfunction(GDVFS_DELAY);
#endif
      sem_result = _sem_wait(Sem1_handle, NO_TIMEOUT);
      if (sem_result != MQX_OK) {
         /* waiting on semaphore sem.Sem1 failed */
      } /*Endif*/
      /* semaphore obtained, perform work */
      _time_delay_ticks((_mqx_uint)1);
      /* semaphore protected work done, release semaphore */
      sem_result = _sem_post(Sem1_handle);
   } /* Endwhile */ 
} /*EndBody*/

/* End of File */
