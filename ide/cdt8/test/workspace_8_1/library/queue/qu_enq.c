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
*** File:  qu_enq.c
***
*** Comments :
***  This file contains the function for adding to a queue
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
#define MQX_USE_INLINE_MACROS 1

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _queue_enqueue
* Returned Value   : boolean - TRUE if successful
* Comments         :
*    Queue the given element onto the queue at the end
*
*END*--------------------------------------------------------*/

boolean _queue_enqueue
   (
      /* [IN] the queue to use */
      QUEUE_STRUCT_PTR         q_ptr,

      /* [IN] the element to add to the end of the queue */
      QUEUE_ELEMENT_STRUCT_PTR e_ptr
   )
{ /* Body */

   _int_disable();
   if ((q_ptr->MAX != 0) && (q_ptr->SIZE >= q_ptr->MAX)) {
      _int_enable();
      return(FALSE);
   } /* Endif */
   
   _QUEUE_ENQUEUE(q_ptr, e_ptr);
   _int_enable();
   return(TRUE);
   
} /* Endbody */

/* EOF */
