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
*** File: lweventa.c
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

/*   Task Code -  LWEventA     */


/*TASK---------------------------------------------------------------
*   
* Task Name   :  LWEventA
* Comments    : 
* 
*END*--------------------------------------------------------------*/

void LWEventA
   (
      uint_32   parameter
   )
{
   _mqx_uint event_result;

   /* 
   ** LOOP - 
   */
   while ( TRUE ) {
      /* set lwevent bit */
      event_result = _lwevent_set(&lwevent, 1);
      if (event_result != MQX_OK) {
         /* setting the event event.Event1 failed */
      } /* endif */
      _time_delay_ticks(1);
   } /* endwhile */ 
} /*end of task*/

/* End of File */
