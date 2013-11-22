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
*** File: lwl_wrii.c
***
*** Comments:      
***   This file contains the function for writing to a log.
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
* Function Name    : _lwlog_write_internal
* Returned Value   : _mqx_uint MQX_OK, or an MQX error code.
* Comments         :
*   This function writes data into the log
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_write_internal
   (

      /* [IN] the log number to be used */
      _mqx_uint      log_number,

      /* [IN] The data to be written into the log entry */
      _mqx_max_type  p1,
      _mqx_max_type  p2,
      _mqx_max_type  p3,
      _mqx_max_type  p4,
      _mqx_max_type  p5,
      _mqx_max_type  p6,
      _mqx_max_type  p7
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;
   LWLOG_HEADER_STRUCT_PTR    log_header_ptr;
   _mqx_max_type _PTR_        data_ptr;
   LWLOG_ENTRY_STRUCT_PTR     log_ptr;
#if MQX_LWLOG_TIME_STAMP_IN_TICKS == 0
   TIME_STRUCT                time;
   MQX_TICK_STRUCT            ticks;
#endif

   _GET_KERNEL_DATA(kernel_data);

   log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG];
   log_header_ptr = log_component_ptr->LOGS[log_number];

#if MQX_CHECK_VALIDITY
   if (log_component_ptr->VALID != LWLOG_VALID) {
      return(MQX_INVALID_COMPONENT_HANDLE);
   } /* Endif */   
#endif

#if MQX_CHECK_ERRORS
   if (log_header_ptr == NULL) {
      return(LOG_DOES_NOT_EXIST);
   } /* Endif */
#endif

   if (! (log_header_ptr->FLAGS & LWLOG_ENABLED)) {
      return(LOG_DISABLED);
   } /* Endif */

   log_ptr  = log_header_ptr->WRITE_PTR->NEXT_PTR;

   if (log_header_ptr->CURRENT_ENTRIES >= log_header_ptr->MAX_ENTRIES) {
      if (log_header_ptr->FLAGS & LOG_OVERWRITE) {
         if (log_ptr == log_header_ptr->READ_PTR) {
            log_header_ptr->READ_PTR = log_ptr->NEXT_PTR;
         } /* Endif */
         log_header_ptr->OLDEST_PTR = log_ptr->NEXT_PTR;
      } else {
         return(LOG_FULL);
      } /* Endif */
   } else {
      log_header_ptr->CURRENT_ENTRIES++;
   } /* Endif */

#if MQX_LWLOG_TIME_STAMP_IN_TICKS == 0
   log_ptr->MICROSECONDS    = (uint_32)_time_get_microseconds();
   PSP_ADD_TICKS(&kernel_data->TIME, &kernel_data->TIME_OFFSET, &ticks);
   PSP_TICKS_TO_TIME(&ticks, &time);
   log_ptr->SECONDS         = time.SECONDS;
   log_ptr->MILLISECONDS    = time.MILLISECONDS;
#else
/* Start CR 2097 */   
   log_ptr->TIMESTAMP = kernel_data->TIME;
   log_ptr->TIMESTAMP.HW_TICKS = _time_get_hwticks();
   PSP_ADD_TICKS(&log_ptr->TIMESTAMP, &kernel_data->TIME_OFFSET, 
      &log_ptr->TIMESTAMP);
/* End CR 2097 */      
#endif

   log_ptr->SEQUENCE_NUMBER = log_header_ptr->NUMBER++;

   data_ptr = &log_ptr->DATA[0];
   *data_ptr++ = p1;
   *data_ptr++ = p2;
   *data_ptr++ = p3;
   *data_ptr++ = p4;
   *data_ptr++ = p5;
   *data_ptr++ = p6;
   *data_ptr   = p7;

   log_header_ptr->WRITE_PTR = log_ptr;

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
