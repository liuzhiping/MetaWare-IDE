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
*** File: lo_dest.c
***
*** Comments:      
***   This file contains the function for destroying a log.
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
* Function Name    : _log_destroy
* Returned Value   : _mqx_uint MQX_OK, or a MQX error code.
* Comments         :
*   This function destroys an existing log
*
*END*----------------------------------------------------------------------*/

_mqx_uint _log_destroy
   (

      /* [IN] the log number to be destroyed */
      _mqx_uint log_number

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   LOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LOG_HEADER_STRUCT_PTR    log_header_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (log_number >= LOG_MAXIMUM_NUMBER) {
      return(LOG_INVALID);
   } /* Endif */
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_LOG] == NULL) {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

   log_component_ptr = (LOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LOG];

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LOG_VALID) {
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

   log_component_ptr->LOGS[log_number] = NULL;
   _int_enable();

   _mem_free(log_header_ptr);   

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LOGS */

/* EOF */
