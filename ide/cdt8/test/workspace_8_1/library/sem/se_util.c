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
*** File: se_util.c
***
*** Comments:      
***   This file contains utility functions for the Semaphore component.
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
* Function Name    : _sem_get_value
* Returned Value   : _mqx_uint current sem value or MAX_MQX_UINT on error
*                    If error the task error code is set to one of:
*                       SEM_INVALID_SEMAPHORE_HANDLE
* Comments         :
*   this function returns the current value of the semaphore.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_get_value
   (
      /* [IN] -  The semaphore handle returned by _sem_open. */
      pointer users_sem_ptr
   )
{ /* Body */
   register SEM_CONNECTION_STRUCT_PTR sem_connection_ptr;
   register SEM_STRUCT_PTR            sem_ptr;
   
   sem_connection_ptr = (SEM_CONNECTION_STRUCT_PTR)users_sem_ptr;
#if MQX_CHECK_VALIDITY
   if (sem_connection_ptr->VALID != SEM_VALID) {
      _task_set_error(SEM_INVALID_SEMAPHORE_HANDLE);
      return(MAX_MQX_UINT); 
   } /* Endif */
#endif

   sem_ptr = sem_connection_ptr->SEM_PTR;
#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != SEM_VALID) {
      _task_set_error(SEM_INVALID_SEMAPHORE);
      return(MAX_MQX_UINT); 
   } /* Endif */
#endif

   return(sem_ptr->COUNT);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sem_get_wait_count
* Returned Value   : _mqx_uint current num tasks on waiting list or MAX_MQX_UINT
*                    on error.
*                    If error the task error code is set to one of:
*                       SEM_INVALID_SEMAPHORE_HANDLE
* Comments         :
*   This function returns the number of tasks waiting for the specified
* semaphore
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_get_wait_count
   (
      /* [IN] -  The semaphore handle returned by _sem_open. */
      pointer users_sem_ptr
   )
{ /* Body */
   register SEM_CONNECTION_STRUCT_PTR sem_connection_ptr;
   register SEM_STRUCT_PTR            sem_ptr;
   
   sem_connection_ptr = (SEM_CONNECTION_STRUCT_PTR)users_sem_ptr;
#if MQX_CHECK_VALIDITY
   if (sem_connection_ptr->VALID != SEM_VALID) {
      _task_set_error(SEM_INVALID_SEMAPHORE_HANDLE);
      return(MAX_MQX_UINT); 
   } /* Endif */
#endif

   sem_ptr = sem_connection_ptr->SEM_PTR;
#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != SEM_VALID) {
      _task_set_error(SEM_INVALID_SEMAPHORE);
      return(MAX_MQX_UINT); 
   } /* Endif */
#endif

   return(_QUEUE_GET_SIZE(&(sem_ptr->WAITING_TASKS)));

} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
