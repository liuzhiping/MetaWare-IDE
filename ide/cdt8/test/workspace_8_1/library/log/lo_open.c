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
*** File: lo_open.c
***
*** Comments:      
***   This file contains the function for creating a log.
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
* Function Name    : _log_create
* Returned Value   : _mqx_uint MQX_OK, or mqx error code
* Comments         :
*   This function creates a new log.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _log_create
   (

      /* [IN] the log number to be used */
      _mqx_uint  log_number, 

      /* [IN] the maximum size of the data to be stored (in _mqx_uints) */
      _mqx_uint  max_size,

      /* [IN] flags about the properties of the log */
      uint_32    flags

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   LOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LOG_HEADER_STRUCT_PTR    log_header_ptr;
   _mqx_uint                result;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (log_number >= LOG_MAXIMUM_NUMBER) {
      return(LOG_INVALID);
   } /* Endif */
#endif

   log_component_ptr = (LOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LOG];
   if (log_component_ptr == NULL) {
      result = _log_create_component();
      log_component_ptr = (LOG_COMPONENT_STRUCT_PTR)
         kernel_data->KERNEL_COMPONENTS[KERNEL_LOG];
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (log_component_ptr == NULL) {
         return(result);
      } /* Endif */
#endif
   } /* Endif */

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LOG_VALID) {
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */   
#endif

   log_header_ptr = (LOG_HEADER_STRUCT_PTR)
      _mem_alloc_system((_mem_size)(sizeof(LOG_HEADER_STRUCT) + max_size * 
         sizeof(_mqx_uint)));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (log_header_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif
   
   log_header_ptr->FLAGS     = flags;
   log_header_ptr->FLAGS    |= LOG_ENABLED;
   log_header_ptr->NUMBER    = 1;
   log_header_ptr->MAX       = max_size;
   log_header_ptr->SIZE      = 0;
   log_header_ptr->LOG_WRITE = &log_header_ptr->DATA[0];
   log_header_ptr->LOG_READ  = log_header_ptr->LOG_WRITE;
   log_header_ptr->LAST_LOG  = log_header_ptr->LOG_WRITE;
   log_header_ptr->LOG_START = log_header_ptr->LOG_WRITE;
   log_header_ptr->LOG_END   = &log_header_ptr->DATA[max_size];
   log_header_ptr->LOG_NEXT  = NULL;

   _int_disable();
#if MQX_CHECK_ERRORS
   if (log_component_ptr->LOGS[log_number] != NULL) {
      _int_enable();
      _mem_free(log_header_ptr);
      return(LOG_EXISTS);
   } /* Endif */
#endif
   log_component_ptr->LOGS[log_number] = log_header_ptr;
   _int_enable();

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LOGS */

/* EOF */
