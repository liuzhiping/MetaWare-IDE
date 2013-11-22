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
*** File: mu_ulock.c
***
*** Comments:      
***   This file contains the function for unlocking a mutex.
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
* Function Name    : _mutex_unlock
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*   This function releases the mutex, so another task may use it.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _mutex_unlock
   (
      /* [IN] - the address of a mutex */
      register MUTEX_STRUCT_PTR mutex_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register TD_STRUCT_PTR          td_ptr;
            _mqx_uint               boosted;
            
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_mutex_unlock, mutex_ptr);

   td_ptr = kernel_data->ACTIVE_PTR;
   
#if MQX_CHECK_ERRORS
   if (mutex_ptr == NULL) {
      _KLOGX2(KLOG_mutex_unlock, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (mutex_ptr->VALID != MUTEX_VALID) {
      _KLOGX2(KLOG_mutex_unlock, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if (mutex_ptr->LOCK != MQX_TEST_AND_SET_VALUE) {
      _KLOGX2(KLOG_mutex_unlock, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */      
/* Start CR 1544 */
#endif
/* End CR 1544 */

   if (mutex_ptr->OWNER_TD != (pointer)td_ptr)  {
      _KLOGX2(KLOG_mutex_unlock, MQX_EINVAL);
      return(MQX_EINVAL);
   } /* Endif */


   _INT_DISABLE();
   boosted = mutex_ptr->BOOSTED;
   mutex_ptr->BOOSTED = 0;   

   if (mutex_ptr->PROTOCOLS & (MUTEX_SPIN_ONLY | MUTEX_LIMITED_SPIN)) {
      mutex_ptr->OWNER_TD = NULL;
      mutex_ptr->LOCK = 0;
   } else {

      if (_QUEUE_GET_SIZE(&mutex_ptr->WAITING_TASKS)) {

         /* Schedule a waiting task to run */
         _QUEUE_DEQUEUE(&mutex_ptr->WAITING_TASKS, td_ptr);
         mutex_ptr->OWNER_TD = td_ptr;
         _TASK_READY(td_ptr, kernel_data);
         if (mutex_ptr->PROTOCOLS & MUTEX_PRIO_PROTECT) {
            /* Switch priority to the higher one if necessary */
            if (mutex_ptr->PRIORITY_CEILING < td_ptr->HOME_QUEUE->PRIORITY) {
               if (mutex_ptr->PRIORITY_CEILING < td_ptr->MY_QUEUE->PRIORITY) {
                  _sched_boost_priority_internal(td_ptr,
                     mutex_ptr->PRIORITY_CEILING);
               } /* Endif */
               mutex_ptr->BOOSTED++;
            } /* Endif */
         } /* Endif */

         _CHECK_RUN_SCHEDULER();/* Let higher priority task run */

      } else {
         mutex_ptr->LOCK = 0;
         mutex_ptr->OWNER_TD = NULL;
      } /* Endif */

   } /* Endif */

   if (boosted) {
      _sched_unboost_priority_internal(kernel_data->ACTIVE_PTR, boosted);
   } /* Endif */
   _INT_ENABLE();      

   _KLOGX2(KLOG_mutex_unlock, MQX_EOK);
   return(MQX_EOK);

} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */
