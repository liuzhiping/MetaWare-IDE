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
*** File: mu_init.c
***
*** Comments:      
***   This file contains the function for initializing a mutex.
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
* Function Name    : _mutex_init
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*    This function creates an instance of a mutex.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutex_init
   (
      /* [IN] - the address where the mutex is to be initialized */
      register MUTEX_STRUCT_PTR      mutex_ptr,

      /* [IN]  - Initialization parameters for the mutex  */
      register MUTEX_ATTR_STRUCT_PTR attr_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   MUTEX_COMPONENT_STRUCT_PTR mutex_component_ptr;
   MUTEX_ATTR_STRUCT          default_attr;
#if MQX_CHECK_ERRORS
   MUTEX_STRUCT_PTR           mutex_chk_ptr;
#endif
   _mqx_uint                   result;

   _GET_KERNEL_DATA(kernel_data);
   if (attr_ptr == NULL) {
      attr_ptr = &default_attr;
      _mutatr_init(attr_ptr);
      _KLOGE3(KLOG_mutex_init, mutex_ptr, NULL);
   } else {
      _KLOGE3(KLOG_mutex_init, mutex_ptr, attr_ptr);
   } /* Endif */
   
#if MQX_CHECK_ERRORS
   if (mutex_ptr == NULL) {
      _KLOGX2(KLOG_mutex_init, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (attr_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutex_init, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif

   mutex_component_ptr = (MUTEX_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_MUTEXES];
   if (mutex_component_ptr == NULL) {
      result = _mutex_create_component();
      mutex_component_ptr = (MUTEX_COMPONENT_STRUCT_PTR)
         kernel_data->KERNEL_COMPONENTS[KERNEL_MUTEXES];
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (mutex_component_ptr == NULL){
         _KLOGX2(KLOG_mutex_init, result);
         return(result);
      } /* Endif */
#endif
   } /* Endif */

#if MQX_CHECK_VALIDITY
   if (mutex_component_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutex_init, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif

   _int_disable();
#if MQX_CHECK_ERRORS
   /* Check if mutex is already initialized */
   mutex_chk_ptr = (MUTEX_STRUCT_PTR)
      ((pointer)mutex_component_ptr->MUTEXES.NEXT);
   while (mutex_chk_ptr != (MUTEX_STRUCT_PTR)
      ((pointer)&mutex_component_ptr->MUTEXES))
   {
      if (mutex_chk_ptr == mutex_ptr) {
         _int_enable();
         _KLOGX2(KLOG_mutex_init, MQX_EINVAL);
         return(MQX_EINVAL);
      } /* Endif */
      mutex_chk_ptr = (MUTEX_STRUCT_PTR)((pointer)mutex_chk_ptr->LINK.NEXT);
   } /* Endif */
#endif
   
   mutex_ptr->PROTOCOLS        = 
      attr_ptr->SCHED_PROTOCOL | attr_ptr->WAIT_PROTOCOL;
   mutex_ptr->VALID            = MUTEX_VALID;
   mutex_ptr->COUNT            = attr_ptr->COUNT;
   mutex_ptr->PRIORITY_CEILING = attr_ptr->PRIORITY_CEILING;
   mutex_ptr->LOCK             = 0;
   mutex_ptr->BOOSTED          = 0;
   mutex_ptr->OWNER_TD         = NULL;
   _QUEUE_INIT(&mutex_ptr->WAITING_TASKS, 0);

   _QUEUE_ENQUEUE(&mutex_component_ptr->MUTEXES, mutex_ptr);
   _int_enable();

   _KLOGX2(KLOG_mutex_init, MQX_EOK);
   return(MQX_EOK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
