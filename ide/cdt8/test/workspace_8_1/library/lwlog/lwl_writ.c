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
*** File: lwl_writ.c
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
* Function Name    : _lwlog_write
* Returned Value   : _mqx_uint MQX_OK, or an MQX error code.
* Comments         :
*   This wrapper function to write data into the log
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_write
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
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   _mqx_uint               result;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (log_number >= LOG_MAXIMUM_NUMBER) {
      return(LOG_INVALID);
   } /* Endif */

   if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG] == NULL) {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

   _INT_DISABLE();

   result = _lwlog_write_internal(log_number, p1, p2, p3, p4, p5, p6, p7);

   _INT_ENABLE();

   return(result);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
