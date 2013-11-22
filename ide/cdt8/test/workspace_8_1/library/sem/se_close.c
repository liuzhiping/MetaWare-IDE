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
*** File: se_close.c
***
*** Comments:      
***   This file contains the function for closing a connection to a semaphore.
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
* Function Name    : _sem_close
* Returned Value   : _mqx_uint MQX_OK or SEM_INVALID_SEMAPHORE_HANDLE,
*                       SEM_INVALID_SEMAPHORE_HANDLE
* Comments         :
*   This function closes a connection to an instance of a semaphore
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_close
   (
      /* [IN/OUT] -  The semaphore handle returned by _sem_open. */
      pointer users_sem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   SEM_CONNECTION_STRUCT_PTR sem_connection_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_sem_close, users_sem_ptr);


/* Start CR 1709 */
/* Allow to be called from an ISR */
/*
#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_sem_close, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } */ /* Endif */
/*#endif*/
/* End CR 1709 */

   sem_connection_ptr = (SEM_CONNECTION_STRUCT_PTR)users_sem_ptr;
#if MQX_CHECK_ERRORS
   if (sem_connection_ptr->TD_PTR != kernel_data->ACTIVE_PTR) {
      _KLOGX2(KLOG_sem_close, SEM_INVALID_SEMAPHORE_HANDLE);
      return(SEM_INVALID_SEMAPHORE_HANDLE);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (sem_connection_ptr->VALID != SEM_VALID) {
      _KLOGX2(KLOG_sem_close, SEM_INVALID_SEMAPHORE_HANDLE);
      return(SEM_INVALID_SEMAPHORE_HANDLE); 
   } /* Endif */
#endif

   if (sem_connection_ptr->SEM_PTR->VALID) {
      /* Release all held semaphores */
      while (sem_connection_ptr->POST_STATE > 0) {
         _sem_post(sem_connection_ptr);
      } /* Endwhile */
   } /* Endif */

   sem_connection_ptr->VALID = 0;

   if (_mem_free(sem_connection_ptr) != MQX_OK) {
      _KLOGX2(KLOG_sem_close, kernel_data->ACTIVE_PTR->TASK_ERROR_CODE);
      return(kernel_data->ACTIVE_PTR->TASK_ERROR_CODE);
   } else {
      _KLOGX2(KLOG_sem_close, MQX_OK);
      return(MQX_OK);
   } /* Endif */

} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
