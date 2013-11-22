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
*** File: sc_spol.c
***
*** Comments:      
***   This file contains the functions for manipulating the 
***   scheduling policy of a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_set_policy
* Returned Value   : old_scheduling policy
* Comments         :
*   This function sets various the scheduling policy for a task or the system
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_set_policy
   (
      /* [IN] the task whose policy is to change:
      ** NULL_TASK_ID => the current task
      ** DEFAULT_TASK_ID => the kernel defaults for task creation
      ** any other    => the specified task
      */
      _task_id task_id,

     /* [IN] the new scheduling policy
     */
      _mqx_uint policy
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   _mqx_uint               old_policy;
   
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_sched_set_policy, (_mqx_uint)task_id, policy);

#if MQX_CHECK_ERRORS
   if (! ((policy == MQX_SCHED_FIFO) || (policy == MQX_SCHED_RR))) {
      _task_set_error(MQX_SCHED_INVALID_POLICY);
      _KLOGX3(KLOG_sched_set_policy, MAX_MQX_UINT, MQX_SCHED_INVALID_POLICY);
      return(MAX_MQX_UINT);
   } /* Endif */
#endif
   
   /* Handle default case */
   if (task_id == MQX_DEFAULT_TASK_ID) {
      old_policy = kernel_data->SCHED_POLICY;
      kernel_data->SCHED_POLICY = policy;
   } else {
      td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
      if (td_ptr == NULL) {
         _task_set_error(MQX_SCHED_INVALID_TASK_ID);
         _KLOGX3(KLOG_sched_set_policy, MAX_MQX_UINT, MQX_SCHED_INVALID_TASK_ID);
         return(MAX_MQX_UINT);
      } /* Endif */
      if (td_ptr->FLAGS & MQX_TIME_SLICE_TASK) {
         old_policy = MQX_SCHED_RR;
      } else {
         old_policy = MQX_SCHED_FIFO;
      } /* Endif */
      _int_disable();
      if (policy == MQX_SCHED_RR) {
         td_ptr->FLAGS |= MQX_TIME_SLICE_TASK;
      } else {
         td_ptr->FLAGS &= ~MQX_TIME_SLICE_TASK;
      } /* Endif */
      _int_enable();
   } /* Endif */

   _KLOGX3(KLOG_sched_set_policy, old_policy, 0L);
   return(old_policy);
      
} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_get_policy
* Returned Value   : scheduling policy
* Comments         :
*   This function gets the scheduling policy for a task or the system
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_get_policy
   (
      /* [IN] the task whose policy is to be obtained
      ** NULL_TASK_ID => the current task
      ** DEFAULT_TASK_ID => the kernel defaults for task creation
      ** any other    => the specified task
      */
      _task_id task_id,

      /* [IN] the location where to write the policy
      */
      _mqx_uint_ptr policy_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   _mqx_uint                old_policy;
   
   _GET_KERNEL_DATA(kernel_data);

   /* Handle default case */
   if (task_id == MQX_DEFAULT_TASK_ID) {
      old_policy = kernel_data->SCHED_POLICY;
   } else {
      td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
      if (td_ptr == NULL) {
         return(MQX_SCHED_INVALID_TASK_ID);
      } /* Endif */
      if (td_ptr->FLAGS & MQX_TIME_SLICE_TASK) {
         old_policy = MQX_SCHED_RR;
      } else {
         old_policy = MQX_SCHED_FIFO;
      } /* Endif */
   } /* Endif */

   *policy_ptr = old_policy;
   return(MQX_OK);
      
} /* Endbody */

/* EOF */
