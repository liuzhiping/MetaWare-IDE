/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: ti_krnl.c
***
*** Comments:
***   This file contains the function called by the BSP whenever
*** a timer interrupt occurs.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/* Start CR 2396 */
#if MQX_USE_PMU
#include "pmu.h"
#endif
/* End CR 2396 */

/*FUNCTION*-------------------------------------------------------------------
 *
 * Function Name    : _time_notify_kernel
 * Returned Value   : void
 * Comments         :
 *   This function is called by the bsp timer ISR.
 * It manages the timeout queue, the current task's time slice and
 * the system time.
 *
 *END*----------------------------------------------------------------------*/

void _time_notify_kernel
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register TD_STRUCT_PTR          td_ptr;
   register TD_STRUCT_PTR          next_td_ptr;
   register _mqx_uint              count;
   register _mqx_int               result;
/* Start CR 2396 */
#if MQX_USE_PMU
   register _mqx_uint              ready_q_size = 0;
   register _mqx_int               ticks_count = 0;
   register PMU_STRUCT_PTR         pmu_ptr;
#endif
/* End CR 2396 */

   _GET_KERNEL_DATA(kernel_data);

   /*
   ** Update the current time.
   */
/* Start CR 2396 */
#if MQX_USE_PMU
   pmu_ptr = kernel_data->PMU_STRUCT_PTR;
   ticks_count = pmu_ptr->TICKS_COUNT / MQX_PMU_SLOW_DOWN_FACTOR;
   do 
   {
      PSP_INC_TICKS(&kernel_data->TIME);
      ticks_count--;
   } while (ticks_count > 0);
#else
   PSP_INC_TICKS(&kernel_data->TIME);
#endif
/* End CR 2396 */

   _INT_DISABLE();

   if (kernel_data->GET_HWTICKS) {
      // The hardware clock may have counted passed it's reference
      // and have an interrupt pending.  Thus, HW_TICKS may exceed
      // kernel_data->HW_TICKS_PER_TICK and this tick_ptr may need
      // normalizing.  This is done in a moment.
      kernel_data->TIME.HW_TICKS = (*kernel_data->GET_HWTICKS)
         (kernel_data->GET_HWTICKS_PARAM);
   } /* Endif */

   // The tick_ptr->HW_TICKS value might exceed the
   // kernel_data->HW_TICKS_PER_TICK and need to be
   // normalized for the PSP.
   PSP_NORMALIZE_TICKS(&kernel_data->TIME);

   /*
   ** Check for tasks on the timeout queue, and wake the appropriate
   ** ones up.  The timeout queue is a time-priority queue.
   */
   count = _QUEUE_GET_SIZE(&kernel_data->TIMEOUT_QUEUE);
   if (count) {
      td_ptr = (TD_STRUCT_PTR)((pointer)kernel_data->TIMEOUT_QUEUE.NEXT);
      ++count;
      while ( --count ) {
         next_td_ptr = td_ptr->TD_NEXT;
         result = PSP_CMP_TICKS(&kernel_data->TIME, &td_ptr->TIMEOUT);
         if (result >= 0) {
            --kernel_data->TIMEOUT_QUEUE.SIZE;
            _QUEUE_UNLINK(td_ptr);
            td_ptr->STATE &= ~IS_ON_TIMEOUT_Q;
            if (td_ptr->STATE & TD_IS_ON_AUX_QUEUE) {
               td_ptr->STATE &= ~TD_IS_ON_AUX_QUEUE;
               _QUEUE_REMOVE(td_ptr->INFO, &td_ptr->AUX_QUEUE);
            } /* Endif */
            _TASK_READY(td_ptr, kernel_data);
/* Start CR 2396 */
#if MQX_USE_PMU
            ready_q_size++;
#endif
/* End CR 2396 */
         } else {
            break;  /* No more to do */
         } /* Endif */
         td_ptr = next_td_ptr;
      } /* Endwhile */
   } /* Endif */

#if MQX_HAS_TIME_SLICE
   /*
   ** Check if the currently running task is a time slice task
   ** and if its time has expired, put it at the end of its queue
   */
   td_ptr = kernel_data->ACTIVE_PTR;
   if ( td_ptr->FLAGS & MQX_TIME_SLICE_TASK ) {
      PSP_INC_TICKS(&td_ptr->CURRENT_TIME_SLICE);
      if (! (td_ptr->FLAGS & TASK_PREEMPTION_DISABLED) ) {
         result = PSP_CMP_TICKS(&td_ptr->CURRENT_TIME_SLICE, &td_ptr->TIME_SLICE);
         if ( result >= 0 ) {
            _QUEUE_UNLINK(td_ptr);
            _TASK_READY(td_ptr,kernel_data);
/* Start CR 2396 */
#if MQX_USE_PMU
            ready_q_size++;
#endif
/* End CR 2396 */
         } /* Endif */
      } /* Endif */
   } /* Endif */
#endif

   _INT_ENABLE();

   /* If the timer component needs servicing, call its ISR function */
   if (kernel_data->TIMER_COMPONENT_ISR != NULL) {
      (*kernel_data->TIMER_COMPONENT_ISR)();
   }/* Endif */

   /* If the lwtimer needs servicing, call its ISR function */
   if (kernel_data->LWTIMER_ISR != NULL) {
      (*kernel_data->LWTIMER_ISR)();
   }/* Endif */

/* Start CR 2396 */
#if MQX_USE_PMU
   /* Make idle task ready - previously blocked */  
   td_ptr = _task_get_td(_task_get_id_from_name("_mqx_idle_task"));
 
   _INT_DISABLE();;
   if ((td_ptr != NULL) && (td_ptr->STATE == BLOCKED)) {
      _task_ready(td_ptr);
   } /* Endif */
   _INT_ENABLE();

   /* Check to see if there is any task ready to run */
   if(ready_q_size > 0) {
      /*
      ** We should set timer interrupt interval to default (5ms) and restart 
      ** idle task loop counter, there are tasks waiting to run.
      */
      pmu_ptr->SLOW_CLOCK_INTERVAL_FLAG = 0;         
      pmu_ptr->IDLE_LOOP_COUNT = 0;
   } /* Endif */
#endif
/* End CR 2396 */

} /* Endbody */

/* EOF */
