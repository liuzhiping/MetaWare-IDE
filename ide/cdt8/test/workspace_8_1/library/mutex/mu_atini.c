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
*** File: mu_atini.c
***
*** Comments:      
***   This file contains the function for initializing a mutex attributes
*** structure.
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
* Function Name    : _mutatr_init
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*    Used by a task to initialize a mutex attributes structure.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutatr_init
   (
      /* [IN] the address of the mutex attributes structure */
      register MUTEX_ATTR_STRUCT_PTR attr_ptr
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE2(KLOG_mutatr_init, attr_ptr);

#if MQX_CHECK_ERRORS
   if (attr_ptr == NULL) {
      _KLOGX2(KLOG_mutatr_init, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif

   attr_ptr->SCHED_PROTOCOL   = MUTEX_NO_PRIO_INHERIT;
   attr_ptr->PRIORITY_CEILING = 0;
   attr_ptr->VALID            = MUTEX_VALID;
   attr_ptr->COUNT            = 0;
   attr_ptr->WAIT_PROTOCOL    = MUTEX_QUEUEING;

   _KLOGX2(KLOG_mutatr_init, MQX_EOK);
   return(MQX_EOK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
