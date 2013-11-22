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
*** File: lwl_test.c
***
*** Comments:      
***   This file contains the function for testing the log component.
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
* Function Name    : _lwlog_test
* Returned Value   : _mqx_uint MQX_OK, or mqx error code
* Comments         :
*   This function tests the log component for consistency.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_test
   (
      /* [OUT] the log in error */
      _mqx_uint _PTR_ log_error_ptr

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LWLOG_HEADER_STRUCT_PTR    log_header_ptr;
   LWLOG_ENTRY_STRUCT_PTR     entry_ptr;
   uchar_ptr                  min_ptr;
   uchar_ptr                  max_ptr;
   _mqx_uint                  i, j;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if ((pointer)log_error_ptr == NULL) {
      return MQX_INVALID_POINTER;
   } /* Endif */
#endif

   *log_error_ptr = 0;
   log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)
   /* Start CR 2326 */
      kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]; 
   /* End CR 2326 */
   if (log_component_ptr == NULL) {
      return(MQX_OK);
   } /* Endif */

   _int_disable();

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LWLOG_VALID) {
      _int_enable();
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */   
#endif

   for (i = 0; i < LOG_MAXIMUM_NUMBER; i++) {
      log_header_ptr = log_component_ptr->LOGS[i];
      if (log_header_ptr != NULL) {
         /* Verify the log pointers */
         min_ptr = (uchar_ptr)log_header_ptr + sizeof(LWLOG_HEADER_STRUCT_PTR);
         max_ptr = min_ptr + sizeof(LWLOG_ENTRY_STRUCT) * log_header_ptr->MAX_ENTRIES;

         if (((uchar_ptr)log_header_ptr->READ_PTR  <  min_ptr) ||
            ((uchar_ptr)log_header_ptr->READ_PTR   >= max_ptr) ||
            ((uchar_ptr)log_header_ptr->WRITE_PTR  <  min_ptr) ||
            ((uchar_ptr)log_header_ptr->WRITE_PTR  >= max_ptr) ||
            ((uchar_ptr)log_header_ptr->OLDEST_PTR <  min_ptr) ||
            ((uchar_ptr)log_header_ptr->OLDEST_PTR >= max_ptr))

         {
            _int_enable();
            *log_error_ptr = i;
            return(LOG_INVALID);
         } /* Endif */

         /* Check each entry in the log */
         entry_ptr = &log_header_ptr->FIRST_ENTRY;
         j = log_header_ptr->MAX_ENTRIES;
         while (entry_ptr->NEXT_PTR && j) {
            entry_ptr = entry_ptr->NEXT_PTR;
            --j;
            if (((uchar_ptr)entry_ptr < min_ptr) || 
               ((uchar_ptr)entry_ptr >= max_ptr))
            {
               _int_enable();
               *log_error_ptr = i;
               return(LOG_INVALID);
            } /* Endif */
         } /* Endwhile */
      } /* Endif */
   } /* Endfor */

   /* START CR 2065 */
   _int_enable();
   /* END CR 2065 */

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
