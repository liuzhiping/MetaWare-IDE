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
*** File:  qu_init.c
***
*** Comments :
***  This file contains the function for initializing a queue.
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
* Function Name    : _queue_init
* Returned Value   : none
* Comments         :
*    initialize the provided queue
*
*END*--------------------------------------------------------*/

void _queue_init
   (
      /* [IN] the queue to initialize */
      QUEUE_STRUCT_PTR q_ptr,

      /* [IN] the maximum size of the queue (0 is infinite) */
      uint_16 size
   )
{ /* Body */

   _QUEUE_INIT(q_ptr, size);

} /* Endbody */

/* EOF */
