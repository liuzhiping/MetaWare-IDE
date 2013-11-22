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
*** File: qu_unlnk.c
***
*** Comments :
***  This file contains the function for removing an element from a queue.
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
* Function Name    : _queue_unlink
* Returned Value   : none
* Comments         :
*    remove a specified queue item from the queue
*
*END*--------------------------------------------------------*/

void _queue_unlink
   (
      /* [IN] the queue to use */
      QUEUE_STRUCT_PTR q_ptr,

      /* [IN] the element to remove from the queue */
      QUEUE_ELEMENT_STRUCT_PTR e_ptr
   )
{ /* Body */

   _int_disable();
   _QUEUE_REMOVE(q_ptr, e_ptr);
   _int_enable();

} /* Endbody */

/* EOF */
