/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File:  qu_next.c
***
*** Comments :
***  This file contains the function for returning the element on the
***  queue next to the input element, without dequeuing it.
***  Note that the QUEUE macros can be found in mqx_prv.h
***
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/* Start CR 313 */
/* #define MQX_USE_INLINE_MACROS 1 */
/* End CR 313 */


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _queue_next
* Returned Value   : QUEUE_ELEMENT_STRUCT_PTR pointer to next
*                    element in queue
* Comments         :
*    return the element on the queue after the provided element,
* but do not dequeue it.
*
*END*--------------------------------------------------------*/

QUEUE_ELEMENT_STRUCT_PTR _queue_next
   (
      /* [IN] the queue to use */
      QUEUE_STRUCT_PTR         q_ptr,

      /* [IN] the element after this element is wanted */
      QUEUE_ELEMENT_STRUCT_PTR e_ptr
   )
{ /* Body */
   QUEUE_ELEMENT_STRUCT_PTR next_ptr;

   if (e_ptr == NULL) {
      return NULL;
   } /* Endif */
   _int_disable();
   next_ptr = e_ptr->NEXT;
   _int_enable();
   if (next_ptr == (QUEUE_ELEMENT_STRUCT_PTR)((pointer)q_ptr)) {
      /* At end of queue */
      next_ptr = NULL;
   } /* Endif */
   return(next_ptr);
   
} /* Endbody */

/* EOF */
