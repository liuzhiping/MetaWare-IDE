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
*** File: mu_tdel.c
***
*** Comments:      
***   This file contains the function called when a task is destroyed
*** so that the mutex component can release any owned resources.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "mutex.h"
#include "mutx_prv.h"

#if MQX_USE_MUTEXES
/*FUNCTION****************************************************************
* 
* Function Name    : _mutex_cleanup
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*    Used during task destruction to free up any mutex owned by this task.
*
* 
*END**********************************************************************/

void _mutex_cleanup
   (
      /* [IN] the task being destroyed */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   MUTEX_COMPONENT_STRUCT_PTR mutex_component_ptr;
   MUTEX_STRUCT_PTR           mutex_ptr;

   _GET_KERNEL_DATA(kernel_data);

   mutex_component_ptr = (MUTEX_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_MUTEXES];
   if (mutex_component_ptr == NULL) {
      return; /* No work to do! */
   } /* Endif */

#if MQX_CHECK_VALIDITY
   if (mutex_component_ptr->VALID != MUTEX_VALID) {
      return;
   } /* Endif */
#endif

   _int_disable();
   mutex_ptr = (MUTEX_STRUCT_PTR)((pointer)mutex_component_ptr->MUTEXES.NEXT);
   while (mutex_ptr != (MUTEX_STRUCT_PTR)
      ((pointer)&mutex_component_ptr->MUTEXES))
   {
      if ((mutex_ptr->LOCK) && (mutex_ptr->OWNER_TD == td_ptr)) {
         mutex_ptr->OWNER_TD = kernel_data->ACTIVE_PTR;
         _mutex_unlock(mutex_ptr);
         mutex_ptr = (MUTEX_STRUCT_PTR)
            ((pointer)mutex_component_ptr->MUTEXES.NEXT);
      } else {
         mutex_ptr = (MUTEX_STRUCT_PTR)((pointer)mutex_ptr->LINK.NEXT);
      } /* Endif */
   } /* Endwhile */
   _int_enable();

} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
