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
*** File: lwsema.c
*** 
*** Comments:
***    This file contains source for the Lightweight MQX demo test.
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <errno.h>
#include <lwevent.h>
#include "lwdemo.h"

/*   Task Code -  LWSemA     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  LWSemA
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void LWSemA
   (
      uint_32   parameter
   )
{
   _mqx_uint sem_result;

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /* wait for lw semaphore until it is available */      
      sem_result = _lwsem_wait_ticks(&lwsem, NO_TIMEOUT);
      if (sem_result != MQX_OK) { 
         /* waiting on semaphore sem.Sem1 failed */
      }  
      /* semaphore obtained, perform work */
      _time_delay_ticks(1);
      /* semaphore protected work done, release semaphore */
      sem_result = _lwsem_post(&lwsem);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
