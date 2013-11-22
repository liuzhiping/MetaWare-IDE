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
*** File: td_get.c
***
*** Comments:      
***   This file contains the function that returns the task descriptor
*** address for a given task id.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_td
* Returned Value   : pointer to task descriptor
* Comments         :
*   This function takes a task-id, and converts it to the address
*   of the associated task descriptor.  It returns NULL if an invalid
*   task_id is presented.
*
*END*----------------------------------------------------------------------*/

pointer _task_get_td
   (
      /* [IN] the task id whose task descriptor address is required */
      _task_id task_id
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   TD_STRUCT_PTR           td_ptr;
   _mqx_uint               size;

   _GET_KERNEL_DATA(kernel_data);
   if (task_id == MQX_NULL_TASK_ID) {
      return((pointer)kernel_data->ACTIVE_PTR);
   } /* Endif */

   /* SPR P171-0022-01 Use int disable, not a semaphore */
   _INT_DISABLE();
   /* END SPR */

   td_ptr = (TD_STRUCT_PTR)((uchar_ptr)kernel_data->TD_LIST.NEXT -
      FIELD_OFFSET(TD_STRUCT,TD_LIST_INFO));
   /* SPR P171-0023-01 use pre-decrement on while loop */
   size   = _QUEUE_GET_SIZE(&kernel_data->TD_LIST) + 1;
   while (--size) {
   /* END SPR */
      if (td_ptr->TASK_ID == task_id) {
         /* SPR P171-0022-01 Use int disable, not a semaphore */
         _INT_ENABLE();
         /* END SPR */
         return (pointer)td_ptr;
      } /* Endif */
      td_ptr = (TD_STRUCT_PTR)((uchar_ptr)td_ptr->TD_LIST_INFO.NEXT -
         FIELD_OFFSET(TD_STRUCT,TD_LIST_INFO));
   } /* Endwhile */

   /* SPR P171-0022-01 Use int disable, not a semaphore */
   _int_enable();
   /* END SPR */

   return NULL;

} /* Endbody */

/* EOF */
