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
*** File: lwl_ena.c
***
*** Comments:      
***   This file contains the function for enabling and disabling a log.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "log.h"
#include "lwlog.h"
#include "lwlogprv.h"

#if MQX_USE_LWLOGS
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwlog_disable
* Returned Value   : _mqx_uint MQX_OK, or a MQX error code.
* Comments         :
*   This function stops all further updates to the log
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_disable
   (

      /* [IN] the log number to be used */
      _mqx_uint log_number

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LWLOG_HEADER_STRUCT_PTR    log_header_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (log_number >= LOG_MAXIMUM_NUMBER) {
      return(LOG_INVALID);
   } /* Endif */
#endif

   log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG];

#if MQX_CHECK_ERRORS
   if (log_component_ptr == NULL) {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LWLOG_VALID) {
      return(MQX_INVALID_COMPONENT_HANDLE);
   } /* Endif */   
#endif

   _int_disable();
   log_header_ptr = log_component_ptr->LOGS[log_number];
#if MQX_CHECK_ERRORS
   if (log_header_ptr == NULL) {
      _int_enable();
      return(LOG_DOES_NOT_EXIST);
   } /* Endif */
#endif
   
   log_header_ptr->FLAGS &= ~LWLOG_ENABLED;
   _int_enable();

   return(MQX_OK);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwlog_enable
* Returned Value   : _mqx_uint MQX_OK, or a MQX error code.
* Comments         :
*   This function allows writing to the log again.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_enable
   (

      /* [IN] the log number to be used */
      _mqx_uint log_number

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LWLOG_HEADER_STRUCT_PTR    log_header_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (log_number >= LOG_MAXIMUM_NUMBER) {
      return(LOG_INVALID);
   } /* Endif */
#endif

   log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG];

#if MQX_CHECK_ERRORS
   if (log_component_ptr == NULL) {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LWLOG_VALID) {
      return(MQX_INVALID_COMPONENT_HANDLE);
   } /* Endif */   
#endif

   _int_disable();
   log_header_ptr = log_component_ptr->LOGS[log_number];
#if MQX_CHECK_ERRORS
   if (log_header_ptr == NULL) {
      _int_enable();
      return(LOG_DOES_NOT_EXIST);
   } /* Endif */
#endif

   log_header_ptr->FLAGS |= LWLOG_ENABLED;
   _int_enable();

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
