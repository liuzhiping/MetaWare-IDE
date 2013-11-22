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
*** File: kl_creat.c
***
*** Comments:      
***   This file contains the function for creating a kernel log.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwlog.h"
#include "lwlogprv.h"
#include "klog.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_create
* Returned Value   : MQX_OK or error code
* Comments         :
*   This function creates the kernel log
*
*END*----------------------------------------------------------------------*/

_mqx_uint _klog_create
   (
      /* [IN] the maximum size of the data to be stored in _mqx_max_types */
      _mqx_uint max_size,

      /* [IN] flags about the properties of the log */
      _mqx_uint flags
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

   /* 
   ** Each element in the each log entry is of the type _mqx_max_type.
   ** The light weight log takes the number of entries to create so
   ** adjust size accordingly.
   */
   max_size = max_size * sizeof(_mqx_max_type) / sizeof(LWLOG_ENTRY_STRUCT);

   return _lwlog_create(LOG_KERNEL_LOG_NUMBER, max_size, flags);

/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */


/* EOF */
