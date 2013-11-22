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
*** File: sc_grrt.c
***
*** Comments:      
***   This file contains the function that returns the time slice
***   interval for a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_get_rr_interval_ticks
* Returned Value   : _mqx_uint MQX_OK error code
* Comments         :
*   This function returns the current time slice in ticks for the specified 
* task or the kernel default if the default task id is passed
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_get_rr_interval_ticks
   (
      /* 
      ** [IN] the task id to apply this to:
      ** NULL_TASK_ID => the current task
      ** DEFAULT_TASK_ID => the kernel default for task creation
      ** any other    => the specified task
      */
      _task_id            task_id,

      /* 
      ** [IN/OUT] the address where the current time slice time
      **   in ticks (_mqx_uint) is to be written
      */
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
            TD_STRUCT_PTR           td_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   /* Validate parameters */
   if ( tick_ptr == NULL ) {
      _task_set_error( MQX_SCHED_INVALID_PARAMETER_PTR );
      return( MQX_SCHED_INVALID_PARAMETER_PTR );
   } /* Endif */
#endif
   
   /* Handle default case */
   if ( task_id == MQX_DEFAULT_TASK_ID ) {
                
      *tick_ptr = kernel_data->SCHED_TIME_SLICE;

   } else {

      td_ptr = (TD_STRUCT_PTR)_task_get_td( task_id );
      if ( td_ptr == NULL ) {
         _task_set_error( MQX_SCHED_INVALID_TASK_ID );
         return( MQX_SCHED_INVALID_TASK_ID );
      } /* Endif */

      *tick_ptr = td_ptr->TIME_SLICE;

   } /* Endif */

   return( MQX_OK );
   
   
} /* Endbody */

/* EOF */
