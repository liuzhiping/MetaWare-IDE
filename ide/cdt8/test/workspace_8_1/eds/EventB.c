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
*** File: eventb.c
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

/*   Task Code -  EventB     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  EventB
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void EventB
   (
      uint_32   parameter
   )
{
   uint_32         event_result;
   pointer   Event1_handle;

   /* open connection to event event.Event1 */
   event_result = _event_open("event.Event1",&Event1_handle);
   if (event_result != MQX_OK) { 
      /* could not open event.Event1  */
   } /* endif */
   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /* wait on event event.Event1 */
      event_result = _event_wait_all(Event1_handle,(uint_32)0x1,(uint_32)NO_TIMEOUT);
      if (event_result != MQX_OK) { 
         /* waiting on event event.Event1 failed */
      } 
         /* clear the event bits after processing event */
      event_result = _event_clear(Event1_handle,(uint_32)0x1);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
