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
*** File: dvfs_time.c
***
*** Comments:      
***    This file contains the time calculation used while in DVFS mode.
***    This file is added to address CR 2396.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"
#include "bsp_prv.h"
#include "pmu.h"

#if MQX_USE_PMU

/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _dvfs_get_timer_ratio
* Returned Value   : uint_32
* Comments         : This function returns the ratio between to different 
*                    timer frequencies based on DVFS modes.
*
*END*----------------------------------------------------------------------*/

uint_32 _dvfs_get_timer_ratio 
(
   /* [IN] current DVFS mode */
   uint_8  current_dvfs,

   /* [IN] new DVFS mode */
   uint_8  new_dvfs
)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;
   uint_32                ratio;
   uint_32                new_frqu;
   uint_32                current_frqu;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   new_frqu = pmu_ptr->TIMER_FREQUNCY[new_dvfs - 1] / 1000000;
   current_frqu = pmu_ptr->TIMER_FREQUNCY[current_dvfs - 1] / 1000000;

   /* Calculate the ratio - DVFS modes are from 1 -> 4 */
   ratio = ( (new_frqu * MQX_PMU_DVFS_PRECISION_RATE) / current_frqu);

   return ratio;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_dvfs_adjust_time
* Returned Value   : 
* Comments         : This function adjusts software clock based on the changes 
*                    of source clock frequency. Changing DVFS mode will cause 
*                    the system clock to be changed. Effort made to adjust 
*                    the real time in order to maintain software clock. 
*
*END*----------------------------------------------------------------------*/

void _ep_dvfs_adjust_time
 (
   void 
 )
{ /* Body */

   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register TD_STRUCT_PTR          td_ptr;
   register PMU_STRUCT_PTR         pmu_ptr;
   register uint_8                 dvfs_mode = 0;
   register uint_8                 current_dvfs_mode;
   register uint_32                hw_ticks_per_tick = 0;
   register uint_32                elapsed_time = 0;
   register uint_32                time_left = 0;
   register uint_32                ratio;

   
   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   _KLOGE4(KLOG_ep_dvfs_adjust_time, pmu_ptr, NULL, NULL);

   /* Get active task */
   td_ptr = kernel_data->ACTIVE_PTR;

   /* Get kernel current mode */
   current_dvfs_mode = kernel_data->CURRENT_KERNEL_DVFS_MODE;

   /* 
   ** Task specific mode overwrites global mode.
   ** First we need to determine whether we need to change the mode. If task 
   ** specific mode is already set, we change the clock based on that. Other wise,
   ** we look to see whether global mode is set. If global mode is already set    
   ** then we change the clock based on global mode. There is a case that active task 
   ** does not have DVFS mode set and also global mode is not set either. However, we've 
   ** already changed the clock based on the pervious task specific mode. In this case 
   ** we need to set the performance level to maximum for the active task and change the 
   ** clock to 100% performance.
   */
   if (td_ptr->TASK_DVFS_MODE == 0) {

	  /* No task specific DVFS mode is set. Check global mode */
      if (pmu_ptr->GLOBAL_DVFS_MODE) {
         /* Global DVFS mode is set */
         dvfs_mode = pmu_ptr->GLOBAL_DVFS_MODE;
      } else if (current_dvfs_mode > MQX_PMU_DVFS_PER_MODE_1) {
         /* Check if we already change the performance level to something other than default (100%) */  
         dvfs_mode = MQX_PMU_DVFS_PER_MODE_1;
      } /* Endif */
      
   } else {
      /* DVFS mode is set per task specific */
      dvfs_mode = td_ptr->TASK_DVFS_MODE;
   } /* Endif */

   /* Check whether we need to change the clock */
   if (dvfs_mode == current_dvfs_mode) {
      _KLOGX4(KLOG_ep_dvfs_adjust_time, dvfs_mode, pmu_ptr->TIMER_FREQUNCY[dvfs_mode - 1],
              pmu_ptr->POWER_CONSUMPTION[dvfs_mode - 1]);
      /* Nothing to do */
      return;
   } /* Endif */

   /* 
   ** We need to change the clock. DVFS mode is set and we have to calculate the 
   ** time according to performance level. 
   **
   ** NOTE: BSP_ALARM_FREQUENCY (=200) does not change, we still have 200 ticks per sec.
   **       That means BSP_ALARM_RESOLUTION is kept at 5ms.
   */
   if (dvfs_mode) {

      /* check if current dvfs mode is 0 */
      if (current_dvfs_mode == 0)
         current_dvfs_mode = MQX_PMU_DVFS_PER_MODE_1;

      /* 
      ** MQX assumes all crystal frequencies for different DVFS mode are the same. So we don't 
      ** need to change any thing is respect to time scaling. We basically set the DVFS mode 
      ** and return.
      */
      if (pmu_ptr->TIMER_FREQUNCY[current_dvfs_mode - 1] !=  pmu_ptr->TIMER_FREQUNCY[dvfs_mode - 1]) {     

         /* Find the ratio between two different dvfs mode */
         ratio = _dvfs_get_timer_ratio(current_dvfs_mode, dvfs_mode);

         /* Get the elapsed time */
         elapsed_time = _psp_get_aux(BSP_TCOUNT) - kernel_data->TIMER_HW_REFERENCE;

         /* Get how much time is left until next timer interrupt */
         time_left = (kernel_data->HW_TICKS_PER_TICK) - elapsed_time;   

         /* Adjust time left until next interrupt */
         time_left = (time_left * ratio) / MQX_PMU_DVFS_PRECISION_RATE;

         /* Adjust the number of HW ticks per interrupt */
         hw_ticks_per_tick = (pmu_ptr->HW_TICKS_PER_INTERRUPT[current_dvfs_mode - 1] * ratio) /
                              MQX_PMU_DVFS_PRECISION_RATE;

         /* Check if time left is greater than new HW ticks per tick */
         if (time_left >= hw_ticks_per_tick)
            /* Give it enough time */
            time_left = hw_ticks_per_tick - 500;

         _int_disable();

         /* Change the clock settings in kernel data */
         kernel_data->HW_TICKS_PER_TICK  = hw_ticks_per_tick;
         kernel_data->TIMER_HW_REFERENCE = BSP_TIMER_WRAP - hw_ticks_per_tick;

         /* Normalize kernel time */
         PSP_NORMALIZE_TICKS(&kernel_data->TIME);

         /* Check if count register is about to roll over */
         if ( _psp_get_aux(BSP_TCOUNT) <= 0xFFFFFF00 ) {         
            /* 
            ** ONLY FOR ARC600
            ** Check for interrupt pending bit in timer control register.
            */
            if (!(_psp_get_aux(BSP_TCONTROL) & 0x8) ) {
               /* Set the count register to the new value */
               _psp_set_aux(BSP_TCOUNT, (kernel_data->TIMER_HW_REFERENCE + time_left));
            } /* Endif */
         } /* Endif */  

         /* Change HW performance level */
         _psp_set_aux(PSP_AUX_PMU_DVFS_PERFORMANCE, (dvfs_mode - 1));

         _int_enable();

      } else {
         _int_disable();

         /* Change HW performance level */
         _psp_set_aux(PSP_AUX_PMU_DVFS_PERFORMANCE, (dvfs_mode - 1));

         _int_enable();
      } /* Endif */
   } /* Endif */     

   /* Update the current mode */
   kernel_data->CURRENT_KERNEL_DVFS_MODE = dvfs_mode;

   /* Check if dvfs mode is 0 */
   if (dvfs_mode == 0) {
      /* Change the dvfs mode to 1 */
      dvfs_mode = MQX_PMU_DVFS_PER_MODE_1;
   } /* Endif */

   _KLOGX4(KLOG_ep_dvfs_adjust_time, dvfs_mode, pmu_ptr->TIMER_FREQUNCY[dvfs_mode - 1],
           pmu_ptr->POWER_CONSUMPTION[dvfs_mode - 1]);

   return;

} /* Endbody */

#endif /* MQX_USE_PMU */

/* EOF */
