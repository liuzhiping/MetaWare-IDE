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
*** File: mu_prio.c
***
*** Comments:      
***   This file contains the function for setting the the priority
*** ceiling of a mutex attributes structure.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "mutex.h"
#include "mutx_prv.h"

#if MQX_USE_MUTEXES
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mutatr_set_priority_ceiling
* Returned Value   : _mqx_uint MQX_EOK on success or POSIX error code
* Comments         :
*    Used by a task to initialize the priority ceiling of a mutex attributes 
* structure.
* 
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutatr_set_priority_ceiling
   (
      /* [IN] the mutex attributes address */
      MUTEX_ATTR_STRUCT_PTR attr_ptr,

      /* [IN] the ceiling to use */
      _mqx_uint             ceiling
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_mutatr_set_priority_ceiling, attr_ptr, ceiling);

#if MQX_CHECK_ERRORS
   if (attr_ptr == NULL) {
      _KLOGX2(KLOG_mutatr_set_priority_ceiling, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (attr_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutatr_set_priority_ceiling, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_ERRORS
   if (ceiling > kernel_data->LOWEST_TASK_PRIORITY) {
      _KLOGX2(KLOG_mutatr_set_priority_ceiling, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif

   attr_ptr->PRIORITY_CEILING = ceiling;

   _KLOGX2(KLOG_mutatr_set_priority_ceiling, MQX_EOK);
   return(MQX_EOK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
