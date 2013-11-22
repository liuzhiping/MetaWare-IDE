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
*** File: ta_rtos.c
***
*** Comments:      
***   This file contains the function which reserves extra memory for
*** each task that gets created.
***                                                               
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

#if MQX_EXTRA_TASK_STACK_ENABLE
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_reserve_space
* Returned Value   : Byte offset from reserved area base address
* Comments         : 
*   This function reserves 'size' bytes that will be added to the size
* of every task's stack.  It can be thought of as a way of allocating
* "task local storage" or perhaps as a way of dynamically adding to the
* TD_STRUCT.  Both "OS Changer" and the MetaWare C/C++ runtime want
* additional per-task variables.  However, this is a general feature that
* can be used for many more purposes.
*
*     NOTE: The space added to each task must be known before ANY
*           tasks are created.
*
* See also ta_gtos.c and td_alloc.c.
*
*END*----------------------------------------------------------------------*/

/* Start CR 1124: C runtime thread local storage */
_mqx_uint _task_reserve_space
   (
      /* [IN} Amount of space to reserve */
      _mqx_uint size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _mqx_uint off, algn;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   /* Can only be called prior to creating any tasks! */
   if (_QUEUE_GET_SIZE(&kernel_data->TD_LIST) != 0) {
      _mqx_fatal_error(MQX_EINVAL);
   } /* Endif */
#endif

   if      (size > 7) algn = 7;
   else if (size > 3) algn = 3;
   else if (size > 1) algn = 1;
   else               algn = 0;

   if (algn > kernel_data->TOS_RESERVED_ALIGN_MASK) {
      kernel_data->TOS_RESERVED_ALIGN_MASK = algn;
   } /* Endif */

   off = (kernel_data->TOS_RESERVED_SIZE + algn) & ~algn;
   kernel_data->TOS_RESERVED_SIZE = off + size;

   return off;

} /* Endbody */
/* End   CR 1124 */
#endif /* MQX_EXTRA_TASK_STACK_ENABLE */

/* EOF */