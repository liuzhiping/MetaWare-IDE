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
*** File: mu_test.c
***
*** Comments:      
***   This file contains the function for testing the mutex component.
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
* Function Name    : _mutex_test
* Returned Value   : _mqx_uint MQX_OK or a MQX error code.
* Comments         :
*    This function tests the mutex component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutex_test
   (
      /* [OUT] - the mutex in error */
      pointer _PTR_ mutex_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   MUTEX_COMPONENT_STRUCT_PTR mutex_component_ptr;
   MUTEX_STRUCT_PTR           mutex_ptr;
   _mqx_uint                  result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_mutex_test, mutex_error_ptr);

   *mutex_error_ptr = NULL;

   mutex_component_ptr = (MUTEX_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_MUTEXES];
   if (mutex_component_ptr == NULL) {
      _KLOGX2(KLOG_mutex_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   if (mutex_component_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutex_test, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */

   _int_disable();

   /* Make sure that the queue of mutexes is ok */
   result = _queue_test(&mutex_component_ptr->MUTEXES, mutex_error_ptr);
   if (result != MQX_OK) {
      _int_enable();
      _KLOGX3(KLOG_mutex_test, result, *mutex_error_ptr);
      return(result);
   } /* Endif */

   mutex_ptr = (MUTEX_STRUCT_PTR)((pointer)mutex_component_ptr->MUTEXES.NEXT);
   while (mutex_ptr != (MUTEX_STRUCT_PTR)
      ((pointer)&mutex_component_ptr->MUTEXES))
   {
      if (mutex_ptr->VALID != MUTEX_VALID) {
         _int_enable();
         *mutex_error_ptr = mutex_ptr;
         _KLOGX3(KLOG_mutex_test, MQX_EINVAL, mutex_ptr);
         return(MQX_EINVAL);
      } /* Endif */
      result = _queue_test(&mutex_ptr->WAITING_TASKS, mutex_error_ptr);
      if (result != MQX_OK) {
         _int_enable();
         *mutex_error_ptr = mutex_ptr;
         _KLOGX3(KLOG_mutex_test, result, mutex_ptr);
         return(result);
      } /* Endif */
      mutex_ptr = (MUTEX_STRUCT_PTR)((pointer)mutex_ptr->LINK.NEXT);
   } /* Endif */
   
   _int_enable();

   _KLOGX2(KLOG_mutex_test, MQX_OK);
   return(MQX_OK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
