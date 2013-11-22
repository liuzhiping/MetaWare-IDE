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
*** File: mu_spin.c
***
*** Comments:      
***   This file contains the function for setting the limited
*** spin field of the mutex attributes field.
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
* Function Name    : _mutatr_set_spin_limit
* Returned Value   : _mqx_uint MQX_EOK on success or POSIX error code
* Comments         :
*    Used by a task to set the limited spin a mutex attributes structure.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutatr_set_spin_limit
   (
      /* [IN] the mutex attributes address */
      MUTEX_ATTR_STRUCT_PTR attr_ptr,

      /* [IN] the limited spin count */
      _mqx_uint             spin_count
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_mutatr_set_spin_limit, attr_ptr, spin_count);

#if MQX_MUTEX_HAS_POLLING
#if MQX_CHECK_ERRORS
   if (attr_ptr == NULL) {
      _KLOGX2(KLOG_mutatr_set_spin_limit, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (attr_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutatr_set_spin_limit, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif

   attr_ptr->COUNT = spin_count;

   _KLOGX2(KLOG_mutatr_set_spin_limit, MQX_EOK);
   return(MQX_EOK);
#else
   _KLOGX2(KLOG_mutatr_set_spin_limit, MQX_EINVAL);
   return(MQX_EINVAL);
#endif

   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
