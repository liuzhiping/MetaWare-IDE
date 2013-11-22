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
*** File: lo_comp.c
***
*** Comments:      
***   This file contains the function for creating the log component
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
* Function Name    : _log_create_component
* Returned Value   : _mqx_uint MQX_OK, MQX_OUT_OF_MEMORY
* Comments         :
*   This function creates a kernel component providing a general log 
* service for all user tasks.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _log_create_component
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   LOG_COMPONENT_STRUCT_PTR log_component_ptr;

   _GET_KERNEL_DATA(kernel_data);

   log_component_ptr = (LOG_COMPONENT_STRUCT_PTR)
      _mem_alloc_system_zero((_mem_size)sizeof(LOG_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (log_component_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   /* We must exclude all ISRs at this point */
   _int_disable();

#if MQX_CHECK_ERRORS
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_LOG] != NULL) {
      _int_enable();
      _mem_free(log_component_ptr);
      return(MQX_OK);
   } /* Endif */
#endif

   kernel_data->KERNEL_COMPONENTS[KERNEL_LOG] = log_component_ptr;
   log_component_ptr->VALID = LOG_VALID;
   _int_enable();

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LOGS */

/* EOF */
