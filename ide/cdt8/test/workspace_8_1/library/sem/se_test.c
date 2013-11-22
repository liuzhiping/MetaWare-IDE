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
*** File: se_test.c
***
*** Comments:      
***   This file contains the function for testing the semaphore component.
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
* Function Name    : _sem_test
* Returned Value   : _mqx_uint MQX_OK, or a MQX error code
* Comments         :
*    This function tests the semaphore component for validity and consistency.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_test
   (
      /* [OUT]  - the semaphore in error */
      pointer _PTR_ sem_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   SEM_COMPONENT_STRUCT_PTR sem_component_ptr;
   SEM_STRUCT_PTR           sem_ptr;
   pointer                  table_ptr;
   _mqx_max_type            data;
   _mqx_uint                result;
   _mqx_uint                i;

   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_sem_test, sem_error_ptr);

   *sem_error_ptr = NULL;

   sem_component_ptr = (SEM_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES];
   if (sem_component_ptr == NULL) {
      _KLOGX3(KLOG_sem_test, MQX_OK, 0);
      return(MQX_OK);
   } /* Endif */
   
   if (sem_component_ptr->VALID != SEM_VALID) {
      _KLOGX3(KLOG_sem_test, MQX_INVALID_COMPONENT_BASE, 0);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
   
   _int_disable();
   /* Test the semaphore name table */
   result = _name_test_internal((NAME_COMPONENT_STRUCT_PTR)
      sem_component_ptr->NAME_TABLE_HANDLE, &table_ptr, sem_error_ptr);
   _int_enable();
   if (result != MQX_OK) {
      _KLOGX3(KLOG_sem_test, MQX_INVALID_COMPONENT_BASE, *sem_error_ptr);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */

   i = 0;
   while (TRUE) {
      _int_disable();
      result =  _name_find_internal_by_index(
         sem_component_ptr->NAME_TABLE_HANDLE, i++, &data);
      if (result != MQX_OK) {
         result = MQX_OK;
         break;
      } /* Endif */

      sem_ptr = (SEM_STRUCT_PTR)data;
      if (sem_ptr->VALID != SEM_VALID) {
         result = SEM_INVALID_SEMAPHORE;
         break;
      } /* Endif */
      result = _queue_test(&sem_ptr->WAITING_TASKS, sem_error_ptr);
      if (result != MQX_OK) {
         break;
      } /* Endif */
      result = _queue_test(&sem_ptr->OWNING_TASKS, sem_error_ptr);
      if (result != MQX_OK) {
         break;
      } /* Endif */
      _int_enable();

   } /* Endwhile */

   _int_enable();

   _KLOGX3(KLOG_sem_test, result, *sem_error_ptr);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
