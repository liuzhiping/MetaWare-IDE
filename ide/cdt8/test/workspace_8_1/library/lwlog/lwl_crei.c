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
*** File: lwl_crei.c
***
*** Comments:      
***   This file contains the function for creating a lw log.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwlog.h"
#include "lwlogprv.h"

#if MQX_USE_LWLOGS
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwlog_create_internal
* Returned Value   : _mqx_uint MQX_OK, or mqx error code
* Comments         :
*   This function creates a new log.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_create_internal
   (

      /* [IN] the log number to be used */
      _mqx_uint               log_number, 

      /* [IN] the maximum number of entries */
      _mqx_uint               max_size,

      /* [IN] flags about the properties of the log */
      _mqx_uint               flags,

      /* [IN] where the log should be created */
      LWLOG_HEADER_STRUCT_PTR log_header_ptr

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LWLOG_ENTRY_STRUCT_PTR     entry_ptr;
   _mqx_uint                  result, i;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (log_number >= LOG_MAXIMUM_NUMBER) {
      return(LOG_INVALID);
   } /* Endif */
#endif

   log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG];
   if (log_component_ptr == NULL) {
      result = _lwlog_create_component();
      log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)
         kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG];
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (log_component_ptr == NULL) {
         return(result);
      } /* Endif */
#endif
#if MQX_CHECK_ERRORS
   } else if (log_component_ptr->LOGS[log_number] != NULL) {
      return(LOG_EXISTS);
#endif
   } /* Endif */

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LWLOG_VALID) {
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */   
#endif
   _mem_zero((pointer)log_header_ptr, (_mem_size)sizeof(LWLOG_HEADER_STRUCT));

   log_header_ptr->FLAGS       = flags;
   log_header_ptr->FLAGS      |= LWLOG_ENABLED;
   log_header_ptr->NUMBER      = 1;
   log_header_ptr->MAX_ENTRIES = max_size;

   entry_ptr = &log_header_ptr->FIRST_ENTRY;
   
   log_header_ptr->READ_PTR   = entry_ptr;
   log_header_ptr->OLDEST_PTR = entry_ptr;

   max_size--;
   for ( i = 0; i < max_size; i++ ) {
      entry_ptr->NEXT_PTR = entry_ptr + 1;
      entry_ptr++;
   } /* Endfor */

   log_header_ptr->WRITE_PTR = entry_ptr;
   entry_ptr->NEXT_PTR = log_header_ptr->READ_PTR;

   _int_disable();
#if MQX_CHECK_ERRORS
   if (log_component_ptr->LOGS[log_number] != NULL) {
      _int_enable();
      return(LOG_EXISTS);
   } /* Endif */
#endif
   log_component_ptr->LOGS[log_number] = log_header_ptr;
   _int_enable();

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
