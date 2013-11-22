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
*** File:  qu_deq.c
***
*** Comments :
***  This file contains the function for removing an element from a queue.
***  Note that the QUEUE macros can be found in mqx_prv.h
***
***
**************************************************************************
*END*********************************************************************/

/* Start CR 312 */
#ifdef MQX_USE_INLINE_MACROS
#undef MQX_USE_INLINE_MACROS
#endif
/* End CR 312 */
#define MQX_USE_INLINE_MACROS 1

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _queue_dequeue
* Returned Value   : QUEUE_ELEMENT_STRUCT_PTR
* Comments         :
*    Dequeue an element from the head of the queue
*
*END*--------------------------------------------------------*/

QUEUE_ELEMENT_STRUCT_PTR  _queue_dequeue
   ( 
      /* [IN] the queue to use */
      QUEUE_STRUCT_PTR q_ptr 
   )
{ /* Body */
   QUEUE_ELEMENT_STRUCT_PTR e_ptr;
   
   _int_disable();
   if (q_ptr->SIZE == 0) {
      _int_enable();
      return(NULL);
   } /* Endif */
   
   _QUEUE_DEQUEUE(q_ptr, e_ptr);
   _int_enable();
   return(e_ptr);

} /* Endbody */

/* EOF */
