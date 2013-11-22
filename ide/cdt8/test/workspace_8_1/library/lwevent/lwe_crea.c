/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: lwe_crea.c
***
*** Comments:      
***   This file contains the functions for creating a light weight event.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_create
* Returned Value   : 
*   Returns MQX_OK upon success, or an error code
* Comments         :
*    Used by a task to create an instance of an light weight event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_create
   (
      /* [IN] the location of the event */
      LWEVENT_STRUCT_PTR event_ptr,
      
      /* [IN] flags for the light weight event */
      _mqx_uint          flags
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

/* START CR 2365 */
#if MQX_CHECK_ERRORS
   LWEVENT_STRUCT_PTR     event_chk_ptr;
#endif
/* END CR 2365 */
   
   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_lwevent_create, event_ptr);

   _QUEUE_INIT(&event_ptr->WAITING_TASKS, 0);
   event_ptr->VALUE = 0;
   event_ptr->FLAGS = flags;
   _int_disable();
   if (kernel_data->LWEVENTS.NEXT == NULL) {
      /* Initialize the light weight event queue */
      _QUEUE_INIT(&kernel_data->LWEVENTS, 0);
   } /* Endif */
   event_ptr->VALID = LWEVENT_VALID;

#if MQX_CHECK_ERRORS
   /* Check if lwevent is already initialized */
   event_chk_ptr = (LWEVENT_STRUCT_PTR)((pointer)kernel_data->LWEVENTS.NEXT);
   while (event_chk_ptr != (LWEVENT_STRUCT_PTR)((pointer)&kernel_data->LWEVENTS)) {
      if (event_chk_ptr == event_ptr) {
         _int_enable();
         _KLOGX2(KLOG_lwevent_create, MQX_EINVAL);
         return(MQX_EINVAL);
      } /* Endif */
      event_chk_ptr = (LWEVENT_STRUCT_PTR)((pointer)event_chk_ptr->LINK.NEXT);
   } /* Endwhile */
#endif

   _QUEUE_ENQUEUE(&kernel_data->LWEVENTS, &event_ptr->LINK);
   _int_enable();

   _KLOGX2(KLOG_lwevent_create, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWEVENTS */

/* EOF */
