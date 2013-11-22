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
*** File: se_dest.c
***
*** Comments:      
***   This file contains the function for destroying a semaphore.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "sem.h"
#include "sem_prv.h"

#if MQX_USE_SEMAPHORES
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sem_destroy
* Returned Value   : _mqx_uint MQX_OK, MQX_COMPONENT_DOES_NOT_EXIST,
*                            MQX_INVALID_COMPONENT_BASE,
*                            SEM_SEMAPHORE_DELETED, SEM_SEMAPHORE_NOT_FOUND
* Comments         :
*   This function removes an instance of a semaphore from the system.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_destroy
   (
      /* [IN]  - the string name of the semaphore */
      char_ptr name_ptr,

      /* [IN]  - a boolean flag, if TRUE destroy NOW not when 
      **         waiting tasks all done.
      */
      boolean force_destroy
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR    kernel_data;
   register SEM_COMPONENT_STRUCT_PTR  sem_component_ptr;
            SEM_STRUCT_PTR            sem_ptr;
            SEM_CONNECTION_STRUCT_PTR sem_connection_ptr;
            QUEUE_STRUCT_PTR          queue_ptr;
            TD_STRUCT_PTR             td_ptr;
            _mqx_max_type                 tmp;
            _mqx_uint                  result;
            _mqx_uint                  i;

   _GET_KERNEL_DATA(kernel_data);                                         
   
   _KLOGE3(KLOG_sem_destroy, name_ptr, force_destroy);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_sem_destroy, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES] == NULL) {
      _KLOGX2(KLOG_sem_destroy, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

   sem_component_ptr = (SEM_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES];
#if MQX_CHECK_VALIDITY
   if (sem_component_ptr->VALID != SEM_VALID) {
      _KLOGX2(KLOG_sem_destroy, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif

   result = _name_find_internal(sem_component_ptr->NAME_TABLE_HANDLE, 
      name_ptr, &tmp);
#if MQX_CHECK_ERRORS
   if (result != MQX_OK) {
      if (result == NAME_NOT_FOUND) {
         _KLOGX2(KLOG_sem_destroy, SEM_SEMAPHORE_NOT_FOUND);
         return(SEM_SEMAPHORE_NOT_FOUND);
      } /* Endif */
      _KLOGX2(KLOG_sem_destroy, result);
      return(result);
   } /* Endif */
#endif
   
   sem_ptr = (SEM_STRUCT_PTR)tmp;

   _int_disable();

#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != SEM_VALID) {
      _int_enable();
      _KLOGX2(KLOG_sem_destroy, SEM_INVALID_SEMAPHORE);
      return(SEM_INVALID_SEMAPHORE); 
   } /* Endif */
#endif

   queue_ptr = &sem_ptr->WAITING_TASKS;
   if (force_destroy) {
      /* Get rid of all waiting tasks */
      i = _QUEUE_GET_SIZE(queue_ptr) + 1;
      while (--i) {
         sem_connection_ptr = (SEM_CONNECTION_STRUCT_PTR)
            ((pointer)queue_ptr->NEXT);
         td_ptr = sem_connection_ptr->TD_PTR;
         if ((td_ptr->STATE & STATE_MASK)== SEM_BLOCKED) {
            _TIME_DEQUEUE(td_ptr,kernel_data);
            _task_set_error_td_internal(td_ptr,SEM_SEMAPHORE_DELETED);
            _TASK_READY(td_ptr,kernel_data);
         } /* Endif */
      } /* Endwhile */
   } /* Endif */

   if (
       ( (sem_ptr->POLICY & SEM_STRICT) &&
         (sem_ptr->COUNT == sem_ptr->MAX_COUNT) &&
         (_QUEUE_GET_SIZE(queue_ptr) == 0))
       ||
       ( (!(sem_ptr->POLICY & SEM_STRICT)) &&
         (_QUEUE_GET_SIZE(queue_ptr) == 0)))
   {
      /* Destroy the semaphore */
      sem_ptr->VALID = 0;

      _int_enable();
      
      /* May need to let higher priority task run */
      _CHECK_RUN_SCHEDULER();

      result = _name_delete_internal(sem_component_ptr->NAME_TABLE_HANDLE,
         name_ptr);
      if (result == NAME_NOT_FOUND) {
         result = SEM_SEMAPHORE_NOT_FOUND;
      } /* Endif */
      _mem_free(sem_ptr);
   } else {
      sem_ptr->DELAYED_DESTROY = TRUE;

      _int_enable();

      /* May need to let higher priority task run */
      _CHECK_RUN_SCHEDULER();
   } /* Endif */

   _KLOGX2(KLOG_sem_destroy, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
