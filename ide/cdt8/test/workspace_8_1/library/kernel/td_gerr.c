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
*** File: td_gerr.c
***
*** Comments:      
***   This file contains the function for retuning the task error
*** code for the current task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_error
* Returned Value   : _mqx_uint
* Comments         :
*    Returns the current task error code of the calling (active) task.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _task_get_error
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   if (kernel_data->IN_ISR) {
      return( kernel_data->INTERRUPT_CONTEXT_PTR->ERROR_CODE );
   } else {
      return( kernel_data->ACTIVE_PTR->TASK_ERROR_CODE );
   } /* Endif */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_error_ptr
* Returned Value   : _mqx_uint _PTR_
* Comments         :
*    Returns the address of the task error code
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _PTR_ _task_get_error_ptr
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   if (kernel_data->IN_ISR) {
      return( &kernel_data->INTERRUPT_CONTEXT_PTR->ERROR_CODE );
   } else {
      return( &kernel_data->ACTIVE_PTR->TASK_ERROR_CODE );
   } /* Endif */      

} /* Endbody */

/* EOF */
