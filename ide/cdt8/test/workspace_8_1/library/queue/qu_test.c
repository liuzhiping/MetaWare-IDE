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
*** File:  qu_test.c
***
*** Comments :
***  This file contains the function for testing a queue.
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
* Function Name    : _queue_test
* Returned Value   : _mqx_uint - MQX_OK or an error code
* Comments         :
*    This function checks the consitstency and validity of a queue.
*    The queue pointers are checked to ensure that they
*    correctly form a circular doubly linked list, with the
*    correct number of elements as specified in the queue header.
*
*END*--------------------------------------------------------*/

_mqx_uint _queue_test
   (
      /* [IN] the queue to test */
      QUEUE_STRUCT_PTR q_ptr,

      /* [OUT] the element where the error was detected */
      pointer _PTR_    element_in_error_ptr
   )
{ /* Body */
   QUEUE_ELEMENT_STRUCT_PTR element_ptr;
   QUEUE_ELEMENT_STRUCT_PTR prev_ptr;
   _mqx_uint                size;

   _int_disable();
   size = _QUEUE_GET_SIZE(q_ptr) + 1;
   element_ptr = q_ptr->NEXT;
   prev_ptr    = (QUEUE_ELEMENT_STRUCT_PTR)((pointer)q_ptr);
   while (--size) {
      if (element_ptr == (pointer)q_ptr) {
         _int_enable();
         /* Size too big for # elements on queue */
         *element_in_error_ptr = element_ptr;
         return(MQX_CORRUPT_QUEUE);
      } /* Endif */
      if (element_ptr->PREV != prev_ptr) {
         _int_enable();
         *element_in_error_ptr = element_ptr;
         return(MQX_CORRUPT_QUEUE);
      } /* Endif */
      prev_ptr    = element_ptr;
      element_ptr = element_ptr->NEXT;
   } /* Endwhile */

   /* Does the last element in the ring point back to the queue head */
   if ((pointer)element_ptr != (pointer)q_ptr) {
      _int_enable();
      *element_in_error_ptr = element_ptr;
      return(MQX_CORRUPT_QUEUE);
   } /* Endif */

   /* Is the last element in ring pointed to by queues PREV field */
   if (q_ptr->PREV != prev_ptr) {
      _int_enable();
      *element_in_error_ptr = element_ptr;
      return(MQX_CORRUPT_QUEUE);
   } /* Endif */

   _int_enable();
   return(MQX_OK);

} /* Endbody */

/* EOF */
