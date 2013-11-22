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
*** File: ta_fp.c
***
*** Comments:      
***   This file contains the functions for modifying the floating point
*** attribute of a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_enable_fp
* Comments         :
*    This function enables floating point context switching for the current
*    task.
*
*END*----------------------------------------------------------------------*/

void _task_enable_fp
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
            TD_STRUCT_PTR td_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE1(KLOG_task_enable_fp);

   td_ptr      = kernel_data->ACTIVE_PTR;
   _int_disable();
   td_ptr->FLAGS |= MQX_FLOATING_POINT_TASK;
   if (kernel_data->FP_ACTIVE_PTR != NULL) {
      if (kernel_data->FP_ACTIVE_PTR != kernel_data->ACTIVE_PTR) {
         _psp_save_fp_context_internal();
      }/* Endif */
   }/* Endif */
   kernel_data->FP_ACTIVE_PTR = kernel_data->ACTIVE_PTR;
   _int_enable();
   _KLOGX1(KLOG_task_enable_fp);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_disable_fp
* Comments         :
*    This function disables floating point context switching for the current
*    task.
*
*END*----------------------------------------------------------------------*/

void _task_disable_fp
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
            TD_STRUCT_PTR td_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE1(KLOG_task_disable_fp);

   td_ptr = kernel_data->ACTIVE_PTR;
   _int_disable();
   if (td_ptr->FLAGS & MQX_FLOATING_POINT_TASK)  {
      td_ptr->FLAGS &= ~MQX_FLOATING_POINT_TASK;
      kernel_data->FP_ACTIVE_PTR = NULL;
   } /* Endif */
   _int_enable();
   _KLOGX1(KLOG_task_disable_fp);

} /* Endbody */

/* EOF */
