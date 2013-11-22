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
*** File: se_waiti.c
***
*** Comments:      
***   This file contains the function for waiting on a semaphore.
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
* Function Name    : _sem_wait_internal
* Returned Value   : _mqx_uint MQX_OK, or SEM_INVALID_SEMAPHORE_HANDLE,
*                       SEM_INVALID_SEMAPHORE_COUNT, SEM_WAIT_TIMEOUT
* Comments         :
*   This function waits for a semaphore to become available.  If one is not
* the task is queued according to the queueing policy for this semaphore.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_wait_internal
   (
      /* [IN] -  The semaphore handle returned by _sem_open. */
      pointer              users_sem_ptr,

      /* [IN] - The maximum number of ticks to wait for or time to until */
      MQX_TICK_STRUCT_PTR  tick_ptr,

      /* [IN] - if wait is specified in ticks is it absolute or relative time */
      boolean              ticks_are_absolute

   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR    kernel_data;
            TD_STRUCT_PTR             td_ptr;
            SEM_STRUCT_PTR            sem_ptr;
   register SEM_CONNECTION_STRUCT_PTR sem_connection_ptr;
   register SEM_CONNECTION_STRUCT_PTR list_connection_ptr;
            SEM_CONNECTION_STRUCT_PTR saved_connection_ptr;
            _mqx_uint                 priority;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   sem_connection_ptr = (SEM_CONNECTION_STRUCT_PTR)users_sem_ptr;

#if MQX_CHECK_VALIDITY
   if (sem_connection_ptr->VALID != SEM_VALID) {
      return(SEM_INVALID_SEMAPHORE_HANDLE); 
   } /* Endif */
#endif

   sem_ptr = sem_connection_ptr->SEM_PTR;
   td_ptr  = kernel_data->ACTIVE_PTR;
      
#if MQX_CHECK_ERRORS
   if (sem_connection_ptr->TD_PTR != td_ptr) {
      /* Only OPENING task can wait on the semaphore */
      return(SEM_INVALID_SEMAPHORE_HANDLE); 
   } /* Endif */
#endif

   _INT_DISABLE();

#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != SEM_VALID) {
      _int_enable();
      return(SEM_INVALID_SEMAPHORE); 
   } /* Endif */
#endif

   if (sem_ptr->COUNT == 0) {

      /* Must wait for a semaphore to become available */
      if (sem_ptr->POLICY & SEM_STRICT) {

         /* 
         ** First check for deadlock... See if this task already
         ** owns all the semaphores, thus cannot post to wake itself up
         */
         if (sem_connection_ptr->POST_STATE == sem_ptr->MAX_COUNT) {
            _int_enable();
            return(MQX_EDEADLK); 
         } /* Endif */

         /* 
         ** May need to raise priorities of one task with semaphore,
         ** If the current task has a higher priority.
         */
         if ((sem_ptr->POLICY & SEM_PRIORITY_INHERITANCE) &&
            (_QUEUE_GET_SIZE(&sem_ptr->OWNING_TASKS) > 0))
         {
      
            /* Find owning task with highest priority */
            list_connection_ptr  = (SEM_CONNECTION_STRUCT_PTR)
               ((pointer)sem_ptr->OWNING_TASKS.NEXT);
            saved_connection_ptr = list_connection_ptr;
            priority = kernel_data->LOWEST_TASK_PRIORITY;
            while ((pointer)list_connection_ptr != 
               (pointer)&sem_ptr->OWNING_TASKS)
            {
               if (list_connection_ptr->TD_PTR->MY_QUEUE->PRIORITY < priority) {
                  priority = list_connection_ptr->TD_PTR->MY_QUEUE->PRIORITY;
                  saved_connection_ptr = list_connection_ptr;
               } /* Endif */
               list_connection_ptr = (SEM_CONNECTION_STRUCT_PTR)
                  list_connection_ptr->NEXT;
            } /* Endwhile */

            if (priority > td_ptr->HOME_QUEUE->PRIORITY) {
               if (priority > td_ptr->MY_QUEUE->PRIORITY) {
                  /* Not high enough, boost the tasks priority */
                  _sched_boost_priority_internal(saved_connection_ptr->TD_PTR, 
                     (_mqx_uint)td_ptr->MY_QUEUE->PRIORITY);
               } /* Endif */
               saved_connection_ptr->BOOSTED++;
            } /* Endif */
         } /* Endif */
      } /* Endif */

      if (sem_ptr->POLICY & SEM_PRIORITY_QUEUEING) {
         /* 
         ** We must insert the waiting task into the waiting list in order
         ** of the priority of the waiting task.
         */
         _sem_insert_priority_internal(&sem_ptr->WAITING_TASKS, sem_connection_ptr);
      } else {
         /* Enqueue at end */   
         _QUEUE_ENQUEUE(&sem_ptr->WAITING_TASKS, sem_connection_ptr);
      } /* Endif */

      /* Now put the task to sleep */
      td_ptr->STATE = SEM_BLOCKED;
      td_ptr->INFO = SEM_WANT_SEMAPHORE;  
      /* Indicate wanting semaphore */

      if (tick_ptr) {

         if (ticks_are_absolute) {
            _time_delay_until(tick_ptr);
         } else {
            _time_delay_for(tick_ptr);
         } /* Endif */


#if MQX_COMPONENT_DESTRUCTION
         if (sem_ptr->VALID != SEM_VALID) {
            _int_enable();
            return(SEM_SEMAPHORE_DELETED);
         } /* Endif */
#endif

         _QUEUE_REMOVE(&sem_ptr->WAITING_TASKS, sem_connection_ptr);

         if (td_ptr->INFO != SEM_AVAILABLE) {
            td_ptr->INFO = 0;
            _INT_ENABLE();
            return(SEM_WAIT_TIMEOUT);
         } /* Endif */

      } else {

         _task_block();

#if MQX_COMPONENT_DESTRUCTION
         if (sem_ptr->VALID != SEM_VALID) {
            _int_enable();
            return(SEM_SEMAPHORE_DELETED);
         } /* Endif */
#endif
         _QUEUE_REMOVE(&sem_ptr->WAITING_TASKS, sem_connection_ptr);

#if MQX_COMPONENT_DESTRUCTION
         if (td_ptr->INFO != SEM_AVAILABLE) {
            td_ptr->INFO = 0;
            _INT_ENABLE();
            return(SEM_SEMAPHORE_DELETED);
         } /* Endif */
#endif

      } /* Endif */

   } else {
      --sem_ptr->COUNT;
   } /* Endif */

   if (sem_ptr->POLICY & SEM_STRICT) {
      if (sem_connection_ptr->POST_STATE == 0){
         _QUEUE_ENQUEUE(&sem_ptr->OWNING_TASKS, sem_connection_ptr);
      } /* Endif */
      sem_connection_ptr->POST_STATE++;  /* # of posts allowed to do */
   } /* Endif */

   _INT_ENABLE();

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
