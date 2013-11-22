#ifndef __pmu_h__
#define __pmu_h__ 1
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
*** File: pmu.h
***
*** Comments:      
***   This file contains the definitions of constants and structures
***   required for Power Management Unit (PMU) and Dynamic Voltage and 
***   Frequency Scaling (DVFS). This File has been added to address CR
***   2396.
***
**************************************************************************
*END*********************************************************************/

/* __DVFS_SYSCLK_X is defined in the linker command file */
extern unsigned int __DVFS_SYSCLK_1[];
extern unsigned int __DVFS_SYSCLK_2[];
extern unsigned int __DVFS_SYSCLK_3[];
extern unsigned int __DVFS_PWR_CONSUMPTION_0[];
extern unsigned int __DVFS_PWR_CONSUMPTION_1[];
extern unsigned int __DVFS_PWR_CONSUMPTION_2[];
extern unsigned int __DVFS_PWR_CONSUMPTION_3[];
extern unsigned int __PMU_PWR_CONSUMPTION[];

/*-----------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/* 
** Power down off: 0
*/
#define MQX_PMU_POWER_DOWN_MODE_0   0

/* 
** Power down 1: Core and architecturally invisible 
** memories powered down 
*/
#define MQX_PMU_POWER_DOWN_MODE_1   1

/*
** Power down 2: Core and memories all power down
*/
#define MQX_PMU_POWER_DOWN_MODE_2   2

/*
** PMU status register
*/
#define MQX_PMU_STATUS_REG         0x450
#define MQX_PMU_STATUS_MASK        0x1C

/*
** Wake up cause
*/
#define MQX_PMU_WAKE_UP_EXT_REST   0x0 /* External reset */
#define MQX_PMU_WAKE_UP_DEBUG_ACCS 0x1 /* Debug access */
#define MQX_PMU_WAKE_UP_TIMER_0    0x2 /* Watchdog timer (timer 0) */
#define MQX_PMU_WAKE_UP_TIMER_1    0x3 /* Watchdog timer (timer 1) */
#define MQX_PMU_WAKE_UP_EXT_INT    0x4 /* External interrupt */

/*
** PMU slow down factor. This is the factor which we slow down timer interval with
*/
#define MQX_PMU_SLOW_DOWN_FACTOR   2

/* 
** DVFS performance mode
*/
#define MQX_PMU_DVFS_PER_MODE_1   1 /* DVFS mode 1 */
#define MQX_PMU_DVFS_PER_MODE_2   2 /* DVFS mode 2 */
#define MQX_PMU_DVFS_PER_MODE_3   3 /* DVFS mode 3 */
#define MQX_PMU_DVFS_PER_MODE_4   4 /* DVFS mode 4 */

/*
** Automatic DVFS flag 
*/
#define MQX_PMU_DVFS_AUTO_ENABLE  1
#define MQX_PMU_DVFS_AUTO_DISABLE 0

/*
** Different timer frequency for different DVFS mode 
*/
#define MQX_PMU_DVFS_TIM_FREQ_0   BSP_TIMER_FREQUENCY /* Max. frequency */
#define MQX_PMU_DVFS_TIM_FREQ_1   ((unsigned int)__DVFS_SYSCLK_1)
#define MQX_PMU_DVFS_TIM_FREQ_2   ((unsigned int)__DVFS_SYSCLK_2)
#define MQX_PMU_DVFS_TIM_FREQ_3   ((unsigned int)__DVFS_SYSCLK_3)

/*
** Power consunmption for different DVFS mode 
*/
#define MQX_PMU_DVFS_PWR_CONSUM_0   ((unsigned int)__DVFS_PWR_CONSUMPTION_0)
#define MQX_PMU_DVFS_PWR_CONSUM_1   ((unsigned int)__DVFS_PWR_CONSUMPTION_1)
#define MQX_PMU_DVFS_PWR_CONSUM_2   ((unsigned int)__DVFS_PWR_CONSUMPTION_2)
#define MQX_PMU_DVFS_PWR_CONSUM_3   ((unsigned int)__DVFS_PWR_CONSUMPTION_3)

/*
** Power consumption for power down
*/
#define MQX_PMU_PWR_CONSUM           ((unsigned int)__PMU_PWR_CONSUMPTION)

/*
** Automatic DVFS default thresholds
*/
#define MQX_PMU_AUTO_DVFS_DEFAULT_THRESHOLD_0   25
#define MQX_PMU_AUTO_DVFS_DEFAULT_THRESHOLD_1   50
#define MQX_PMU_AUTO_DVFS_DEFAULT_THRESHOLD_2   75

/*
** Automatic DVFS default period
*/
#define MQX_PMU_AUTO_DVFS_DEFAULT_PERIOD        200

/*
** timer tick precision
*/
#define MQX_PMU_DVFS_PRECISION_RATE   10000


/*-----------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/



/*-----------------------------------------------------------------------*/
/*
**                          PROTOTYPE DECLARATIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

/* Interface functions */
extern uint_32 _ep_pmu_set_pwrdn_mode(uchar);
extern uint_32 _ep_pmu_get_pwrdn_mode(void);
extern uint_32 _ep_pmu_set_timer_thrshld(uint_32);
extern uint_32 _ep_pmu_set_max_slw_dwn(uint_32);
extern uint_32 _ep_dvfs_get_mode(_task_id);
extern uint_32 _ep_dvfs_set_mode(_task_id, uchar);
extern uint_32 _ep_dvfs_set_auto(uchar);
extern uint_32 _ep_dvfs_set_thrshld(uchar,uchar,uchar,uint_32);
extern uint_32 _ep_dvfs_set_freq_for_mode(uint_32,uint_32,uint_32,uint_32);

#ifdef __cplusplus
}
#endif

#endif