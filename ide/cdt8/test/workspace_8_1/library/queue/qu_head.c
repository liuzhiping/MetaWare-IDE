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
*** File:  qu_head.c
***
*** Comments :
***  This file contains the function for returning the head of the queue
***  without actually dequeueing it.
***  Note that the QUEUE macros can be found in mqx_prv.h
***
***
***
**************************************************************************
*END*********************************************************************/

/* Start CR 312 */
#ifdef MQX_USE_INLINE_MACROS
#undef MQX_USE_INLINE_MACROS
#endif
/* End CR 312 */

/* Start CR 313 */
/* #define MQX_USE_INLINE_MACROS 1 */
/* End CR 313 */

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _queue_head
* Returned Value   : QUEUE_ELEMENT_STRUCT_PTR - pointer to 
*                    element at head of queue
* Comments         :
*    Return the first element on the queue, but do not dequeue it
*
*END*--------------------------------------------------------*/

QUEUE_ELEMENT_STRUCT_PTR _queue_head
   (
      /* [IN] the queue to use */
      QUEUE_STRUCT_PTR         q_ptr
   )
{ /* Body */
   QUEUE_ELEMENT_STRUCT_PTR e_ptr;

   _int_disable();
   if (q_ptr->SIZE == 0) {
      _int_enable();
      return(NULL);
   } /* Endif */
   
   e_ptr = q_ptr->NEXT;
   _int_enable();
   return(e_ptr);
   
} /* Endbody */

/* EOF */
