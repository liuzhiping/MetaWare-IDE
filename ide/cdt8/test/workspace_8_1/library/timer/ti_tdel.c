/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: ti_tdel.c
***
*** Comments:
***   This file contains the function called when a task is destroyed
*** in order to cleanup any connections for the timer component
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "timer.h"
#include "tim_prv.h"

#if MQX_USE_TIMER
/*FUNCTION*-----------------------------------------------------------
*
* Function Name   : _timer_cleanup
* Return Value    : none
* Comments        : This is the function that is called by a task during
*   task destruction, in order to cleanup any timers
*
*END*------------------------------------------------------------------*/

void _timer_cleanup
   (
      /* [IN] the task being destroyed */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   TIMER_COMPONENT_STRUCT_PTR timer_component_ptr;
   QUEUE_STRUCT_PTR           queue_ptr;
   TIMER_ENTRY_STRUCT_PTR     element_ptr;
   TIMER_ENTRY_STRUCT_PTR     next_element_ptr;

   _GET_KERNEL_DATA(kernel_data);
   
   timer_component_ptr = kernel_data->KERNEL_COMPONENTS[KERNEL_TIMER];
   if (timer_component_ptr == NULL) {
      return;
   } /* Endif */

   /* Gain exclusive access to the timer queues */
   _lwsem_wait(&timer_component_ptr->TIMER_ENTRIES_LWSEM);

   queue_ptr   = (QUEUE_STRUCT_PTR)&timer_component_ptr->ELAPSED_TIMER_ENTRIES;
   element_ptr = (TIMER_ENTRY_STRUCT_PTR)((pointer)queue_ptr->NEXT);
   while ((pointer)element_ptr != (pointer)queue_ptr) {
      next_element_ptr = (TIMER_ENTRY_STRUCT_PTR)
         ((pointer)element_ptr->QUEUE_ELEMENT.NEXT);
      if (element_ptr->TD_PTR == td_ptr) {
         _QUEUE_REMOVE(queue_ptr, element_ptr);
         element_ptr->VALID = 0;
         _mem_free(element_ptr);
      } /* Endif */
      element_ptr = next_element_ptr;
   } /* Endwhile */

   queue_ptr   = (QUEUE_STRUCT_PTR)
      ((pointer)&timer_component_ptr->KERNEL_TIMER_ENTRIES);
   element_ptr = (TIMER_ENTRY_STRUCT_PTR)((pointer)queue_ptr->NEXT);
   while ((pointer)element_ptr != (pointer)queue_ptr) {
      next_element_ptr = (TIMER_ENTRY_STRUCT_PTR)
         ((pointer)element_ptr->QUEUE_ELEMENT.NEXT);
      if (element_ptr->TD_PTR == td_ptr) {
         _QUEUE_REMOVE(queue_ptr, element_ptr);
         element_ptr->VALID = 0;
         _mem_free(element_ptr);
      } /* Endif */
      element_ptr = next_element_ptr;
   } /* Endwhile */

   _lwsem_post(&timer_component_ptr->TIMER_ENTRIES_LWSEM);

} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */