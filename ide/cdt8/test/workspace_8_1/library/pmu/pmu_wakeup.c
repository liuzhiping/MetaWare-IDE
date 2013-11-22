/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: pmu_wakeup.c
***
*** Comments:      
***   This file contains the wake up routin for MQX after CPU wakes up.
***   This file has been added to address CR 2396.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"
#include "bsp_prv.h"
#include "log.h"
#include "lwlog.h"
#include "lwlogprv.h"
#include "pmu.h"

#if MQX_USE_PMU

/*FUNCTION*-------------------------------------------------------------------
 *
 * Function Name    : _pmu_wakeup
 * Returned Value   : void
 * Comments         :
 *   This function is a recovery function after we wake up.
 * In case, wake up casued by external interrupt or debugger access, this 
 * function readys tasks that are in timeout queue and normalized the system
 * time. If timer interrupt causes the wake up, it will call timer ISR.
 *
 *END*----------------------------------------------------------------------*/

void _pmu_wakeup()
{ /* Body */

   register KERNEL_DATA_STRUCT_PTR     kernel_data;
   register TD_STRUCT_PTR              td_ptr;
   register TD_STRUCT_PTR              next_td_ptr;
   register LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;
   register LWLOG_HEADER_STRUCT_PTR    log_header_ptr;
   register LWLOG_ENTRY_STRUCT_PTR     log_ptr;
   register PMU_STRUCT_PTR             pmu_ptr;
   register _mqx_uint                  count;
   register _mqx_int                   result;
   register _mqx_uint                  ready_q_size = 0;
   register _mqx_uint                  pmu_status;
   register _mqx_uint                  idle_task_id;
   register _mqx_uint                  proc_num;

   pmu_status = (_psp_get_aux(MQX_PMU_STATUS_REG) & MQX_PMU_STATUS_MASK);

   _GET_KERNEL_DATA(kernel_data);
   pmu_ptr = kernel_data->PMU_STRUCT_PTR;
 
   /* Check for debugger access and external interrupt */
   if ( (pmu_status == (MQX_PMU_WAKE_UP_DEBUG_ACCS << 2)) || 
        (pmu_status == (MQX_PMU_WAKE_UP_EXT_INT << 2)) ) {

      _INT_DISABLE();

      /* 
      ** Since wake up resets the timer we need to re-initialize the timer. 
      ** That causes MQX to loss some time.
      */
      /* Initialize the timer */
#ifdef BSP_TIMER
      _psp_set_int_level(BSP_TIMER_INTERRUPT_VECTOR, BSP_TIMER_INTERRUPT_LEVEL);
      _psp_set_aux(BSP_TCONTROL,0);
#ifndef BSP_LEGACY_TIMER
      _psp_set_aux(BSP_TLIMIT,BSP_TIMER_WRAP);
      _psp_set_aux(PSP_AUX_TCONTROL0,0);
      _psp_set_aux(PSP_AUX_TCONTROL1,0);
#endif
      _psp_set_aux(BSP_TCOUNT,(kernel_data->TIMER_HW_REFERENCE));
      _psp_set_aux(BSP_TCONTROL,3);
#endif

      /* 
      ** when we wake up by external interrupt or debugger access, time is unknown.
      ** we try to not to lose too much time when we wake up.
      ** when we get here last log entry is wake up. when lwlog logs an entry it 
      ** will up date the time stamp on the log based on HW ticks. we normalize
      ** kernel time based on the last kernel log time stamp.
      */
      log_component_ptr = (LWLOG_COMPONENT_STRUCT_PTR)kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG];
      log_header_ptr = log_component_ptr->LOGS[LOG_KERNEL_LOG_NUMBER];
      log_ptr = log_header_ptr->WRITE_PTR;
      kernel_data->TIME = log_ptr->TIMESTAMP;

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
               ready_q_size++;
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
               ready_q_size++;
            } /* Endif */
         } /* Endif */
      } /* Endif */
#endif

      /* Check to see if there is any task ready to run */
      if(ready_q_size > 0) {
         /*
         ** We should set timer interrupt interval to default (5ms) and restart 
         ** idle task loop counter, there are tasks waiting to run.
         */
         pmu_ptr->SLOW_CLOCK_INTERVAL_FLAG = 0;         
         pmu_ptr->IDLE_LOOP_COUNT = 0;
      } /* Endif */

      _INT_ENABLE();

      /* If the timer component needs servicing, call its ISR function */
      if (kernel_data->TIMER_COMPONENT_ISR != NULL) {
         (*kernel_data->TIMER_COMPONENT_ISR)();
      } /* Endif */

      /* If the lwtimer needs servicing, call its ISR function */
      if (kernel_data->LWTIMER_ISR != NULL) {
         (*kernel_data->LWTIMER_ISR)();
      } /* Endif */
      
      /* get the processor number */
      proc_num = kernel_data->PROCESSOR_NUMBER;
      /* calculate idle task ID - idle task is always the first task (1) */
      idle_task_id = (proc_num << 0x10) + 1;
      /* get pointer to the idle task descriptor */
      td_ptr = _task_get_td(idle_task_id);
 
      /* Make idle task ready - previously blocked */
      _INT_DISABLE();
      if ((td_ptr != NULL) && (td_ptr->STATE == BLOCKED)) {
         _TASK_READY(td_ptr,kernel_data);
      } /* Endif */
      _INT_ENABLE();

      /* Let higher priority task run */
      _CHECK_RUN_SCHEDULER(); 

   } else if (pmu_status == (MQX_PMU_WAKE_UP_TIMER_0 << 2)) {
      /* Update the auto DVFS idle count */
      pmu_ptr->AUTO_DVFS_IDLE_CNT++;
      /* 
      ** wake up caused by timer 0. we assume timer 0 vector number has not 
      ** changed from default location (default vector number for timer 0 = 3)
      */
      asm (" mov_s %r1,3");
      asm (" jeq   _int_kernel_isr");
   } else if (pmu_status == (MQX_PMU_WAKE_UP_TIMER_1 << 2)) {
      /* 
      ** wake up caused by timer 1. we assume timer 1 vector number has not 
      ** changed from default location (default vector number for timer 1 = 7)
      */
      asm (" mov_s %r1,7");
      asm (" jeq   _int_kernel_isr");
   } /* Endif */
   
} /* Endbody */

#endif /* MQX_USE_PMU */

/* EOF */
