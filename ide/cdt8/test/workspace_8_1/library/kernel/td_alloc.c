/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: td_alloc.c
***
*** Comments:      
***   This file contains the function for obtaining a free task descriptor.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_alloc_td_internal
* Returned Value   : TD_STRUCT_PTR
* Comments         :
*    This function returns a pointer to an initialized task descriptor.
*    Note that the task descriptor is initialized to zero.
*
*END*----------------------------------------------------------------------*/

TD_STRUCT_PTR _task_alloc_td_internal
   (
      /* [IN] size of the stack for the newly created task */
      _mem_size     stack_size,

      /* [OUT] size of TD_STRUCT and other overhead */
      _mem_size_ptr overhead,

      /* [IN] if not NULL, the location of the stack is provided */
      pointer    input_stack_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   TD_STRUCT _PTR_          insert_td_ptr;
   TD_STRUCT _PTR_          next_td_ptr;
   TD_STRUCT _PTR_          new_td_ptr;
   TD_STRUCT _PTR_          prev_td_ptr;
   QUEUE_ELEMENT_STRUCT_PTR q_ptr;
   _mem_size                extra = sizeof(TD_STRUCT);
#if MQX_EXTRA_TASK_STACK_ENABLE
   _mem_size                tos, tos_offset = 0; /* CR 2366 */
#endif
   uint_16                  task_num;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (_QUEUE_GET_SIZE(&kernel_data->TD_LIST) >= (MAX_UINT_16-1)) {
      /* We already have maximum number of tasks in system */
      return NULL;
   } /* Endif */
#endif
     
   /*
   ** Space can be added to each task to provide for
   ** "task local storage".  This is a way to provide each task
   ** with its own copy of a var.  Both OS Changer and the
   ** MetaWare C/C++ runtime want additional per-task variables.
   */
#if MQX_EXTRA_TASK_STACK_ENABLE
   tos = kernel_data->TOS_RESERVED_SIZE;
   if (tos != 0) {
      uint_32 align_mask = kernel_data->TOS_RESERVED_ALIGN_MASK;
      extra = (extra + align_mask) & ~align_mask;
      tos_offset = extra;
      /* must be word multiple */
      extra = (extra + tos + 3) & ~3;
   } /* Endif */
#endif
   *overhead = extra;


   if (input_stack_ptr) {
      new_td_ptr = (TD_STRUCT_PTR)input_stack_ptr;
   } else {
      new_td_ptr = (TD_STRUCT_PTR)_mem_alloc(extra + stack_size);
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if ( new_td_ptr == NULL ) {
         return ((TD_STRUCT_PTR)new_td_ptr);
      } /* Endif */
#endif
   } /* Endif */

   _mem_zero((pointer)new_td_ptr, extra);

#if MQX_EXTRA_TASK_STACK_ENABLE
   if (tos != 0) {
      new_td_ptr->TOS_RESERVED = ((uchar_ptr) new_td_ptr) + tos_offset;
   } /* Endif */
#endif

   /* Add new task to list */
   task_num = kernel_data->TASK_NUMBER;
   insert_td_ptr = kernel_data->INSERT_TD_PTR;
   new_td_ptr->TASK_ID = BUILD_TASKID(kernel_data->PROCESSOR_NUMBER, 
      task_num);

   if (!insert_td_ptr) {
      q_ptr = (QUEUE_ELEMENT_STRUCT_PTR)((pointer)&kernel_data->TD_LIST);
   } else {
      q_ptr = &insert_td_ptr->TD_LIST_INFO;
   } /* Endif */

   _QUEUE_INSERT(&kernel_data->TD_LIST, q_ptr, &new_td_ptr->TD_LIST_INFO);

   /* 
   ** New task may not have been inserted at the end of the list. Search for a 
   ** spot to insert the next td and also calculate its task number
   */
   prev_td_ptr = new_td_ptr;
   do {
      insert_td_ptr = prev_td_ptr;
      next_td_ptr   = (TD_STRUCT_PTR)((uchar_ptr)insert_td_ptr->TD_LIST_INFO.NEXT -
         FIELD_OFFSET(TD_STRUCT,TD_LIST_INFO));
      prev_td_ptr   = next_td_ptr;
      INC_TASK_NUMBER(task_num);
   } while( ((pointer)next_td_ptr != ((pointer)&kernel_data->TD_LIST)) &&
      (TASK_NUMBER_FROM_TASKID(next_td_ptr->TASK_ID) == task_num) );

   kernel_data->INSERT_TD_PTR = insert_td_ptr;
   kernel_data->TASK_NUMBER   = task_num;

   return ((TD_STRUCT_PTR)new_td_ptr);

} /* Endbody */

/* EOF */
