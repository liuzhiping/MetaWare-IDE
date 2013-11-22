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
*** File: qu_util.c
***
*** Comments :
***  This file contains functions for manipulating the mqx queues.
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
* Function Name    : _queue_get_size
* Returned Value   : _mqx_uint - The size of the queue
* Comments         :
*    Returns the size of the queue
*
*END*--------------------------------------------------------*/

_mqx_uint _queue_get_size
   (
      /* [IN] the queue to obtain information about */
      QUEUE_STRUCT_PTR q_ptr
   )
{ /* Body */

   return _QUEUE_GET_SIZE(q_ptr);

} /* Endbody */


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _queue_is_empty
* Returned Value   : boolean - TRUE if queue is empty
* Comments         :
*    Returns whether the queue is empty or not
*
*END*--------------------------------------------------------*/

boolean _queue_is_empty
   (
      /* [IN] the queue to obtain information about */
      QUEUE_STRUCT_PTR q_ptr 
   )
{ /* Body */

   return _QUEUE_IS_EMPTY(q_ptr);

} /* Endbody */

/* EOF */
