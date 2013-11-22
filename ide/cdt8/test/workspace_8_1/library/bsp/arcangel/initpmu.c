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
*** File: initpmu.c
***
*** Comments:      
***    This file contains the PMU initialization functions. This file is 
***    added to address CR 2396.
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
* Function Name    : _bsp_init_dvfs
* Returned Value   : 
* Comments         : This function sets different timer frequency and HW 
*                    ticks per tick for different DVFS mode
*
*END*----------------------------------------------------------------------*/

uint_32 _bsp_init_dvfs
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;
   
   /* 
   ** Timer frequency 0 is the Max. performance mode. That means this frequency 
   ** is the Max. frequency for the system. As a result no other frequencies should
   ** be greater than this. Same is true for Min. performance mode. No other frequencies
   ** should be smaller than this frequency.
   */
   if( (MQX_PMU_DVFS_TIM_FREQ_1 > MQX_PMU_DVFS_TIM_FREQ_0) || 
       (MQX_PMU_DVFS_TIM_FREQ_2 > MQX_PMU_DVFS_TIM_FREQ_0) ||
       (MQX_PMU_DVFS_TIM_FREQ_3 > MQX_PMU_DVFS_TIM_FREQ_0) )
   {
      return MQX_INVALID_PARAMETER;
   } /* Endif */

   if( (MQX_PMU_DVFS_TIM_FREQ_1 < MQX_PMU_DVFS_TIM_FREQ_3) ||
       (MQX_PMU_DVFS_TIM_FREQ_2 < MQX_PMU_DVFS_TIM_FREQ_3) )
   {
      return MQX_INVALID_PARAMETER;
   } /* Endif */

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   /* Initialize timer frequency for DVFS */
   pmu_ptr->TIMER_FREQUNCY[0] = MQX_PMU_DVFS_TIM_FREQ_0;
   pmu_ptr->TIMER_FREQUNCY[1] = MQX_PMU_DVFS_TIM_FREQ_1;
   pmu_ptr->TIMER_FREQUNCY[2] = MQX_PMU_DVFS_TIM_FREQ_2;
   pmu_ptr->TIMER_FREQUNCY[3] = MQX_PMU_DVFS_TIM_FREQ_3;

   /* Calculate HW ticks per interrupt for different timer frequency */
   pmu_ptr->HW_TICKS_PER_INTERRUPT[0] = BSP_HW_TICKS_PER_INTERRUPT;
   pmu_ptr->HW_TICKS_PER_INTERRUPT[1] = pmu_ptr->TIMER_FREQUNCY[1] / BSP_ALARM_FREQUENCY;
   pmu_ptr->HW_TICKS_PER_INTERRUPT[2] = pmu_ptr->TIMER_FREQUNCY[2] / BSP_ALARM_FREQUENCY;
   pmu_ptr->HW_TICKS_PER_INTERRUPT[3] = pmu_ptr->TIMER_FREQUNCY[3] / BSP_ALARM_FREQUENCY;

   /* Initialize power consumption for DVFS */
   pmu_ptr->POWER_CONSUMPTION[0] = MQX_PMU_DVFS_PWR_CONSUM_0;
   pmu_ptr->POWER_CONSUMPTION[1] = MQX_PMU_DVFS_PWR_CONSUM_1;
   pmu_ptr->POWER_CONSUMPTION[2] = MQX_PMU_DVFS_PWR_CONSUM_2;
   pmu_ptr->POWER_CONSUMPTION[3] = MQX_PMU_DVFS_PWR_CONSUM_3;
   
   /* Initialize auto DVFS default parameters */
   pmu_ptr->AUTO_THRESHOLD_0 = MQX_PMU_AUTO_DVFS_DEFAULT_THRESHOLD_0;
   pmu_ptr->AUTO_THRESHOLD_1 = MQX_PMU_AUTO_DVFS_DEFAULT_THRESHOLD_1;
   pmu_ptr->AUTO_THRESHOLD_2 = MQX_PMU_AUTO_DVFS_DEFAULT_THRESHOLD_2;
   pmu_ptr->AUTO_DVFS_PERIOD = MQX_PMU_AUTO_DVFS_DEFAULT_PERIOD;

   return MQX_OK;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _bsp_init_pmu
* Returned Value   : none
* Comments         :
*    Routine to initialize PMU structure.
*
*END*----------------------------------------------------------------------*/

uint_32 _bsp_init_pmu
   (
      void
   )
{ /* Body */
   
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;
   
   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = (PMU_STRUCT_PTR)_mem_alloc_system_zero((uint_32)sizeof(PMU_STRUCT));

#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (pmu_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   /* Initialize the tick count */
   pmu_ptr->TICKS_COUNT = 1;

   /* Initialize power consumption */
   pmu_ptr->PMU_POWER_CONSUMPTION = MQX_PMU_PWR_CONSUM;

   kernel_data->PMU_STRUCT_PTR = pmu_ptr;

   return MQX_OK;

} /* Endbody */

#endif /* MQX_USE_PMU */

/* EOF */