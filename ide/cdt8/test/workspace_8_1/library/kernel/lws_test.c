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
*** File: lws_test.c
***
*** Comments:      
***   This file contains the function for testing all light weight
*** semaphores in the system.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwsem_test
* Returned Value   : MQX_OK or an error code
* Comments         :
*   This function tests all light weight semaphores for consistency and
* validity.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_test
   ( 
      /* [OUT] the light weight semapohre in error */
      pointer _PTR_ lwsem_error_ptr,

      /* [OUT] the td on a light weight semaphore in error */
      pointer _PTR_ td_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWSEM_STRUCT_PTR       sem_ptr;
   _mqx_uint              queue_size;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_lwsem_test, lwsem_error_ptr, td_error_ptr);

   *td_error_ptr = NULL;
   *lwsem_error_ptr = NULL;

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_lwsem_test, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   }/* Endif */
#endif

   _int_disable();

   result = _queue_test((QUEUE_STRUCT_PTR)&kernel_data->LWSEM, lwsem_error_ptr);
   if (result != MQX_OK) {
      /* Start CR 2330 */
      _int_enable(); 
      /* End CR 2330 */
      _KLOGX3(KLOG_lwsem_test, result, *lwsem_error_ptr);
      return(result);
   } /* Endif */

   sem_ptr    = (LWSEM_STRUCT_PTR)((pointer)kernel_data->LWSEM.NEXT);
   queue_size = _QUEUE_GET_SIZE(&kernel_data->LWSEM);
   while (queue_size--) {
      if (sem_ptr->VALID != LWSEM_VALID) {
         result = MQX_INVALID_LWSEM;
         break;
      } /* Endif */

      result = _queue_test(&sem_ptr->TD_QUEUE, td_error_ptr);
      if (result != MQX_OK) {
         break;
      } /* Endif */
      
      sem_ptr = sem_ptr->NEXT;
   } /* Endwhile */

   _int_enable();

   if (result != MQX_OK) {
      *lwsem_error_ptr = (pointer)sem_ptr;
   } /* Endif */
   _KLOGX4(KLOG_lwsem_test, result, *lwsem_error_ptr, *td_error_ptr);

   return(result);
   
} /* Endbody */

/* EOF */
