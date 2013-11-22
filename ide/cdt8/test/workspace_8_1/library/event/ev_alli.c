/**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ev_alli.c
***
*** Comments:      
***   This file contains the function for waiting for all requested bits
*** to be set in the event.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "event.h"
#include "evnt_prv.h"

#if MQX_USE_EVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _event_wait_all_internal
* Returned Value   : Returns MQX_OK upon success an error code:
* Comments         : Used by a task to wait for all or any specified events
*   according to bit mask
* 
*END*------------------------------------------------------------------*/
/* START CR 1053 */ 
//_mqx_uint _event_wait_internal
_mqx_uint _event_wait_all_internal
   (
      /* 
      ** [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer              users_event_ptr, 

      /* [IN] - bit mask - each bit of which represents an event. */
      _mqx_uint            bit_mask, 

      /* [IN] - The maximum number of ticks to wait for or time to until */
      MQX_TICK_STRUCT_PTR  tick_ptr,

      /* [IN] - if wait is specified in ticks is it absolute or relative time */
      boolean              ticks_are_absolute,

      /* [IN] - if wait is for all bits */
      boolean              all

   )
{ /* Body */
            
   return(_event_wait_internal(users_event_ptr, bit_mask, tick_ptr, 
                               ticks_are_absolute, all));
            
} /* Endbody */
/* END CR 1053 */
#endif /* MQX_USE_EVENTS */

/* EOF */