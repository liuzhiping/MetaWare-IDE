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
*** File: mu_pol.c
***
*** Comments:      
***   This file contains the function for setting the waiting protocol of
*** a mutex attributes structure.
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
* Function Name    : _mutatr_set_wait_protocol
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*    Used by a task to initialize the mutex waiting protocol of 
* a mutex attributes structure.
* 
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutatr_set_wait_protocol
   (
      /* [IN] the mutex attributes address */
      MUTEX_ATTR_STRUCT_PTR attr_ptr,

      /* [IN] the waiting protocol to use */
      _mqx_uint              waiting_protocol
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE3(KLOG_mutatr_set_wait_protocol, attr_ptr, waiting_protocol);

#if MQX_CHECK_ERRORS
   if (attr_ptr == NULL) {
      _KLOGX2(KLOG_mutatr_set_wait_protocol, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (attr_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutatr_set_wait_protocol, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_ERRORS
   /* Validate input value */
   if (!
      (
#if MQX_MUTEX_HAS_POLLING
       (waiting_protocol == MUTEX_SPIN_ONLY) ||
       (waiting_protocol == MUTEX_LIMITED_SPIN) ||
#endif
       (waiting_protocol == MUTEX_QUEUEING) ||
       (waiting_protocol == MUTEX_PRIORITY_QUEUEING)))
   {
      _KLOGX2(KLOG_mutatr_set_wait_protocol, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif

   attr_ptr->WAIT_PROTOCOL = waiting_protocol;

   _KLOGX2(KLOG_mutatr_set_wait_protocol, MQX_EOK);
   return(MQX_EOK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
