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
*** File: td_serr.c
***
*** Comments:      
***   This file contains the function for setting the task error code
*** for the current task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_set_error
* Returned Value   : _mqx_uint
*                  :   The old value of the error code
* Comments         :
*    This function sets the task error code.  If the task error code does
*    not equal MQX_OK, then the value is not changed.  However, the value
*    of the task error code may be reset to MQX_OK by setting the task
*    error code to MQX_OK.  The old value of the task error code is 
*    returned.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _task_set_error
   (
      /* [IN] the new error code */
      _mqx_uint new_error_code
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register _mqx_uint                old_error_code;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_task_set_error, new_error_code);

   /* get the old value */
   if (kernel_data->IN_ISR) {
      old_error_code = kernel_data->INTERRUPT_CONTEXT_PTR->ERROR_CODE;
   } else {
      old_error_code = kernel_data->ACTIVE_PTR->TASK_ERROR_CODE;
   } /* Endif */

   if (( new_error_code == MQX_OK ) || ( old_error_code == MQX_OK )) {
      if (kernel_data->IN_ISR) {
         kernel_data->INTERRUPT_CONTEXT_PTR->ERROR_CODE = new_error_code;
      } else {
         kernel_data->ACTIVE_PTR->TASK_ERROR_CODE = new_error_code;
      } /* Endif */
   } /* Endif */

   _KLOGX2(KLOG_task_set_error, old_error_code);
   return( old_error_code );

} /* Endbody */

/* EOF */
