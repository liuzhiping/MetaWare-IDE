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
*** File: atimesli.c
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

/*   Task Code -  ATimeSliceTask     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  ATimeSliceTask
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void ATimeSliceTask
   (
      uint_32   parameter
   )
{

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      _time_delay((uint_32)15);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
