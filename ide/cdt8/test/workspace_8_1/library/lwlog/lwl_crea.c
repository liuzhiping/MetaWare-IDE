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
*** File: lwl_crea.c
***
*** Comments:      
***   This file contains the function for creating a LW log at a specified 
*** location.
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
* Function Name    : _lwlog_create_at
* Returned Value   : _mqx_uint MQX_OK, or mqx error code
* Comments         :
*   This function creates a new log.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_create_at
   (

      /* [IN] the log number to be used */
      _mqx_uint log_number, 

      /* [IN] the maximum number of entries */
      _mqx_uint max_size,

      /* [IN] flags about the properties of the log */
      _mqx_uint flags,

      /* [IN] where the log should be created */
      pointer   where

   )
{ /* Body */
   LWLOG_HEADER_STRUCT_PTR  log_header_ptr = (LWLOG_HEADER_STRUCT_PTR)where;
   _mqx_uint                result;

#if MQX_CHECK_ERRORS
   if (max_size == 0) {
      return(LOG_INVALID_SIZE);
   } /* Endif */

   if (where == NULL) {
      return(MQX_INVALID_POINTER);
   } /* Endif */
#endif

   result = _lwlog_create_internal(log_number, max_size, flags, log_header_ptr);
   if (result == MQX_OK) {
      log_header_ptr->TYPE = LWLOG_STATIC;
   } /* Endif */

   return(result);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
