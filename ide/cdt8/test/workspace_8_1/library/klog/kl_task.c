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
*** File: kl_task.c
***
*** Comments:      
***   This file contains the functions for enabling and disabling
*** kernel logging for a particular task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwlog.h"
#include "klog.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_disable_logging_task
* Returned Value   : none
* Comments         :
*   This function enables logging for the specified task
*
*END*----------------------------------------------------------------------*/

void _klog_disable_logging_task
   (
      /* [IN] the task which is to have kernel logging disabled */
      _task_id tid
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

   TD_STRUCT_PTR          td_ptr;

   _int_disable();
   td_ptr = (TD_STRUCT_PTR)_task_get_td(tid);
   if (td_ptr != NULL) {
      td_ptr->FLAGS &= ~TASK_LOGGING_ENABLED;
   } /* Endif */
   _int_enable();

/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_enable_logging_task
* Returned Value   : none
* Comments         :
*   This function disables logging for the specified task
*
*END*----------------------------------------------------------------------*/

void _klog_enable_logging_task
   (
      /* [IN] the task which is to have kernel logging enabled */
      _task_id tid
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

   TD_STRUCT_PTR          td_ptr;

   _int_disable();
   td_ptr = (TD_STRUCT_PTR)_task_get_td(tid);
   if (td_ptr != NULL) {
      td_ptr->FLAGS |= TASK_LOGGING_ENABLED;
   } /* Endif */
   _int_enable();

/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */

/* EOF */
