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
*** File: se_comp.c
***
*** Comments:      
***   This file contains the function for creating the semaphore component.
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
* Function Name    : _sem_create_component
* Returned Value   : _mqx_uint MQX_OK, a Task Error Code,
* Comments         :
*   This function creates the data structure pointed to by the field
* in the Kernel Data structure.  It contains all basic data structures for
* the entire semaphore component.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_create_component
   (
      /* [IN] the initial number of semaphores */
      _mqx_uint initial_number,

      /* [IN] the number of semaphores to add when
      ** space is no longer available
      */
      _mqx_uint grow_number,

      /* [IN] the maximum number of semaphores allowed */
      _mqx_uint maximum_number
   )
{ /* Body */
   SEM_COMPONENT_STRUCT_PTR sem_component_ptr;
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   _mqx_uint                result;
            
   _GET_KERNEL_DATA(kernel_data);            

   _KLOGE4(KLOG_sem_create_component, initial_number, grow_number, maximum_number);

   sem_component_ptr = (SEM_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES];
   if (sem_component_ptr != NULL) {
      if (maximum_number > sem_component_ptr->MAXIMUM_NUMBER) {
         sem_component_ptr->MAXIMUM_NUMBER = maximum_number;
      } /* Endif */
      _KLOGX2(KLOG_sem_create_component, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   sem_component_ptr = (SEM_COMPONENT_STRUCT_PTR)
      _mem_alloc_system_zero((_mem_size)sizeof(SEM_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (sem_component_ptr == NULL) {
      _KLOGX2(KLOG_sem_create_component, MQX_OUT_OF_MEMORY);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   result = _name_create_handle_internal(&sem_component_ptr->NAME_TABLE_HANDLE,
      initial_number, grow_number, maximum_number, initial_number);
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (result != MQX_OK) {
      _mem_free(sem_component_ptr);
      _KLOGX2(KLOG_sem_create_component, result);
      return(result);
   } /* Endif */
#endif

   sem_component_ptr->GROW_NUMBER       = grow_number;
   if (maximum_number == 0) {
      sem_component_ptr->MAXIMUM_NUMBER = MAX_MQX_UINT;
   } else if (maximum_number < initial_number) {
      sem_component_ptr->MAXIMUM_NUMBER = initial_number;
   } else {
      sem_component_ptr->MAXIMUM_NUMBER = maximum_number;
   } /* Endif */
   sem_component_ptr->VALID = SEM_VALID;

   _int_disable();
#if MQX_CHECK_ERRORS
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES] != NULL) {
      SEM_COMPONENT_STRUCT_PTR new_sem_component_ptr;

      new_sem_component_ptr = (SEM_COMPONENT_STRUCT_PTR)
         kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES];
      if (maximum_number > new_sem_component_ptr->MAXIMUM_NUMBER) {
         new_sem_component_ptr->MAXIMUM_NUMBER = maximum_number;
      } /* Endif */
      _int_enable();
      _name_destroy_handle_internal(sem_component_ptr->NAME_TABLE_HANDLE);
      _mem_free(sem_component_ptr);
      _KLOGX2(KLOG_sem_create_component, MQX_OK);
      return(MQX_OK);
   } /* Endif */
#endif
   kernel_data->KERNEL_COMPONENTS[KERNEL_SEMAPHORES] = sem_component_ptr;

#if MQX_TASK_DESTRUCTION
   kernel_data->COMPONENT_CLEANUP[KERNEL_SEMAPHORES] = _sem_cleanup;
#endif

   _int_enable();
   
   _KLOGX2(KLOG_sem_create_component, MQX_OK);
   return(MQX_OK);
   
} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
