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
*** File: eventa.c
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

/*   Task Code -  EventA     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  EventA
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void EventA
   (
      uint_32   parameter
   )
{
   uint_32         event_result;
   pointer   Event1_handle;

   /* create event - event.Event1 */
   event_result = _event_create("event.Event1");
   if (event_result != MQX_OK) { 
      /* event event.Event1 not be created */
   } /* endif */
   /* open connection to event event.Event1 */
   event_result = _event_open("event.Event1",&Event1_handle);
   if (event_result != MQX_OK) { 
      /* could not open event.Event1  */
   } /* endif */
   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /* set event event.Event1 */
      event_result = _event_set(Event1_handle,(uint_32)0x1);
      if (event_result != MQX_OK) { 
         /* setting the event event.Event1 failed */
      } /* endif */
      _time_delay((uint_32)5);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
