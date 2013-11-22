/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ev_creat.c
***
*** Comments:      
***   This file contains the functions for creating an event.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "event.h"
#include "evnt_prv.h"

#if MQX_USE_EVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _event_create
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to create an instance of an event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_create
   (
      /* [IN] the string name for the event */
      char _PTR_ name_ptr
   )
{ /* Body */
   EVENT_STRUCT_PTR           event_ptr;
   _mqx_uint                   result;
   
   result = _event_create_internal(name_ptr, &event_ptr);

#if MQX_CHECK_ERRORS
   if (result != MQX_OK) {
      return(result);
   } /* Endif */
#endif

   event_ptr->AUTO_CLEAR = FALSE;

   return(result);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
