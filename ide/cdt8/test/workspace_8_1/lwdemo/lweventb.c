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
*** File: lweventb.c
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

/*   Task Code -  LWEventB     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  LWEventB
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void LWEventB
   (
      uint_32   parameter
   )
{
   _mqx_uint  event_result;

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /* wait on event */
      event_result = _lwevent_wait_ticks(&lwevent,1,TRUE,0);
      if (event_result != MQX_OK) { 
         /* waiting on event event.Event1 failed */
      }       
         /* clear the event bits after processing event */
      event_result = _lwevent_clear(&lwevent, 1);
   } /* endwhile */ 
} /*end of task*/

/* End of File */