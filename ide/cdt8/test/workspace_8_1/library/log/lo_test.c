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
*** File: lo_test.c
***
*** Comments:      
***   This file contains the function for testing the log component.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "log.h"
#include "log_prv.h"

#if MQX_USE_LOGS
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _log_test
* Returned Value   : _mqx_uint MQX_OK, or mqx error code
* Comments         :
*   This function tests the log component for consistency.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _log_test
   (

      /* [OUT] the log in error */
      _mqx_uint _PTR_ log_error_ptr

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   LOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LOG_HEADER_STRUCT_PTR    log_ptr;
   _mqx_uint                i;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_log_test, log_error_ptr);

   *log_error_ptr = 0;
   log_component_ptr = (LOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LOG];
   if (log_component_ptr == NULL) {
      _KLOGX2(KLOG_log_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   if (log_component_ptr->VALID != LOG_VALID) {
      _KLOGX2(KLOG_log_test, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */   


   _int_disable();
   for (i = 0; i < LOG_MAXIMUM_NUMBER; i++) {
      log_ptr = log_component_ptr->LOGS[i];
      if (log_ptr != NULL) {
         /* Verify the log pointers */
         if ((log_ptr->LOG_END != &log_ptr->DATA[log_ptr->MAX]) ||
             (log_ptr->LOG_START != &log_ptr->DATA[0]))
         {
            break;
         } /* Endif */
         
         if ((log_ptr->LOG_WRITE > log_ptr->LOG_END) ||
             (log_ptr->LOG_NEXT  > log_ptr->LOG_END) ||
             (log_ptr->LOG_READ  > log_ptr->LOG_END) ||
             (log_ptr->LAST_LOG  > log_ptr->LOG_END))
         {
            break;
         } /* Endif */
         if ((log_ptr->LOG_WRITE < log_ptr->LOG_START) ||
             (log_ptr->LOG_READ  < log_ptr->LOG_START) ||
             (log_ptr->LAST_LOG  < log_ptr->LOG_START))
         {
            break;
         } /* Endif */
         if ((log_ptr->LOG_NEXT != NULL) && 
            (log_ptr->LOG_NEXT < log_ptr->LOG_START))
         {
            break;
         } /* Endif */
      } /* Endif */
   } /* Endfor */
   _int_enable();
   

   if (i == LOG_MAXIMUM_NUMBER) {
      _KLOGX2(KLOG_log_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   *log_error_ptr = i;
   _KLOGX3(KLOG_log_test, LOG_INVALID, i);
   return(LOG_INVALID);

} /* Endbody */
#endif /* MQX_USE_LOGS */

/* EOF */
