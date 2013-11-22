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
*** File: idletask.c
***
*** Comments:      
***   This file contains the idle task.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

#if MQX_USE_IDLE_TASK

/* Start CR 2396 */
#if MQX_USE_PMU
#include "pmu.h"


/*TASK*---------------------------------------------------------------------
* 
* Function Name    : _mqx_idle_task
* Returned Value   : none
* Comments         :
*    This function is the code for the idle task.
* If PMU is enabled this task will count the idle task loop count and if this 
* counter exceeds the user defined threshold then it blocks itself and system 
* eventually will go to power down mode (PMU mode is set by application). If 
* PMU is disabled it will execute sleep instruction (no power down) and exits.
*
*END*----------------------------------------------------------------------*/

void _mqx_idle_task
   ( 
      /* [IN] parameter passed to the task when created */
      uint_32 parameter
   )
{ /* Body */

   volatile KERNEL_DATA_STRUCT _PTR_ kernel_data;
   PMU_STRUCT _PTR_                  pmu_ptr;          
      
   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   /* Check to see power down mode is enabled */
   if (pmu_ptr->PMU_MODE > 0) {
	  
      while (1) {
       
         /* 
         ** check to see if we are sleeping/waking too many times in the idle loop.
         ** we loop until thrshld reached, then set the slow flag for timer ISR to be 
         ** picked up and block. when we wake up we need to ready the task again.
         ** we do not reset our sleep count, so next time we come back we don't loop 
         ** again, set the flag and block. 
         */
         if (pmu_ptr->IDLE_LOOP_COUNT >= pmu_ptr->SLEEP_THRSHLD) {
            /* set flag for timer isr to change the timer interrupt interval */
            pmu_ptr->SLOW_CLOCK_INTERVAL_FLAG = 1;
            /* block idle task and let MQX to save the task's state */
            _task_block(); 
         } else {
            /* update the idle loop count */
            pmu_ptr->IDLE_LOOP_COUNT++;
         } /* Endif */

      } /* Endwhile */

   } else {
      while (1) {
         /* Update the auto DVFS idle count */
         pmu_ptr->AUTO_DVFS_IDLE_CNT++;
         /* Go to sleep (not power down) */
         asm (" sleep");
      } /* Endwhile */
   } /* Endif */

} /* Endbody */

#else

/*TASK*---------------------------------------------------------------------
* 
* Function Name    : _mqx_idle_task
* Returned Value   : none
* Comments         :
*    This function is the code for the idle task.
* It implements a simple 128 counter.  This can be read from the debugger,
* and calibrated so that the idle CPU time usage can be calculated.
*
*END*----------------------------------------------------------------------*/

void _mqx_idle_task
   ( 
      /* [IN] parameter passed to the task when created */
      uint_32 parameter
   )
{ /* Body */
   
   volatile KERNEL_DATA_STRUCT _PTR_ kernel_data;
      
   _GET_KERNEL_DATA(kernel_data);

   while (1) {

      if (++kernel_data->IDLE_LOOP1 == 0) {
         if (++kernel_data->IDLE_LOOP2 == 0) {
            if (++kernel_data->IDLE_LOOP3 == 0) {
               ++kernel_data->IDLE_LOOP4;
			} /* Endif */
		 } /* Endif */
	  } /* Endif */

   } /* Endwhile */
} /* Endbody */

#endif /* MQX_USE_PMU */
/* End CR 2396 */

#endif /* MQX_USE_IDLE_TASK */

/* EOF */

