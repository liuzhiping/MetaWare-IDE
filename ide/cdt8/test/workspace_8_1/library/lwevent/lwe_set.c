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
*** File: lwe_set.c
***
*** Comments:      
***   This file contains the function for setting the event.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_set
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to set the specified event bits in an event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_set
   (
      /* [IN] - The address of the light weight event */
      LWEVENT_STRUCT_PTR   event_ptr, 

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint bit_mask
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR      kernel_data;
   QUEUE_ELEMENT_STRUCT_PTR    q_ptr;
   QUEUE_ELEMENT_STRUCT_PTR    next_q_ptr;
   TD_STRUCT_PTR               td_ptr;
   _mqx_uint                   set_bits;
            
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_lwevent_set, event_ptr, bit_mask);

   _INT_DISABLE();
#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != LWEVENT_VALID) {
      _int_enable();
      _KLOGX2(KLOG_lwevent_set, MQX_LWEVENT_INVALID);
      return(MQX_LWEVENT_INVALID);
   } /* Endif */
#endif

   set_bits = event_ptr->VALUE | bit_mask;

   if (_QUEUE_GET_SIZE(&event_ptr->WAITING_TASKS)) {
      /* Schedule waiting task(s) to run if bits ok */

      q_ptr = event_ptr->WAITING_TASKS.NEXT;
      while (q_ptr != (QUEUE_ELEMENT_STRUCT_PTR)
         ((pointer)&event_ptr->WAITING_TASKS))
      {
         td_ptr = (pointer)q_ptr;
         _BACKUP_POINTER(td_ptr, TD_STRUCT, AUX_QUEUE);
         next_q_ptr = q_ptr->NEXT;
         if (((td_ptr->FLAGS & TASK_LWEVENT_ALL_BITS_WANTED) && 
            ((td_ptr->LWEVENT_BITS & set_bits) == td_ptr->LWEVENT_BITS)) ||
            ((!(td_ptr->FLAGS & TASK_LWEVENT_ALL_BITS_WANTED)) && 
            (td_ptr->LWEVENT_BITS & set_bits)))
         {  
/* Start CR 406 */
#if 0
            _QUEUE_DEQUEUE(&event_ptr->WAITING_TASKS, q_ptr);
#endif
            _QUEUE_REMOVE(&event_ptr->WAITING_TASKS, q_ptr);
/* End CR 406 */
            _TIME_DEQUEUE(td_ptr, kernel_data);
            td_ptr->INFO = 0;
            _TASK_READY(td_ptr, kernel_data);
            /* Only ready one task if event is an auto clear event */
            if (event_ptr->FLAGS & LWEVENT_AUTO_CLEAR) {
/* Start CR 303 */
#if 0
               set_bits &= td_ptr->LWEVENT_BITS;
#endif
               set_bits &= ~td_ptr->LWEVENT_BITS;
/* End CR 303 */
               break;
            } /* Endif */
         } /* Endif */
         q_ptr = next_q_ptr;
      } /* Endwhile */
   } /* Endif */

   event_ptr->VALUE = set_bits;
   _INT_ENABLE();

   /* May need to let higher priority task run */
   _CHECK_RUN_SCHEDULER();

   _KLOGX2(KLOG_lwevent_set, MQX_OK);
   return(MQX_OK);
         
} /* Endbody */
#endif /* MQX_USE_LWEVENTS */

/* EOF */
