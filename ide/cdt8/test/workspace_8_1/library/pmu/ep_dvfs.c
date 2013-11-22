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
*** File: ep_dvfs.c
***
*** Comments:      
***    This file contains the PMU DVFS functions. This file is added to 
***    address CR 2396.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "pmu.h"

#if MQX_USE_PMU

/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_dvfs_get_mode
* Returned Value   : DVFS mode on success. Error code on failer.
* Comments         :
*    This function returns the current voltage and clock frequency mode.
*    If a specific task is specified, the DVFS mode for that task is returned.
*    A 0 is returned if the specified task has no task specific DVFS mode set. 
*    If the task_id is 0, the global DVFS mode is returned.
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_dvfs_get_mode
(
   /* [IN] the task_id to apply DVFS setting */
   _task_id tid 
)
{ /* Body */

   TD_STRUCT_PTR          td_ptr;
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   /* task ID 0, return global DVFS mode */
   if (tid == MQX_NULL_TASK_ID) {
      /* return global DVFS mode */
      return (uint_32)(pmu_ptr->GLOBAL_DVFS_MODE);
   } /* Endif */

   td_ptr = _task_get_td(tid);

   /* check the task ID */
   if (td_ptr == NULL) {
      return MQX_INVALID_TASK_ID;
   } /* Endif */

   /* check DVFS mode the task */
   if (td_ptr->TASK_DVFS_MODE) {
      return (td_ptr->TASK_DVFS_MODE);
   } else {
      return 0;
   } /* Endif */

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_dvfs_set_mode
* Returned Value   : MQX_OK on success. Error code on failer.
* Comments         :
*    This function modifies the current Voltage and Clock frequency settings. 
*    If a specific task is specified, the DVFS mode is applied to the specific 
*    task. If the task_id is 0, it applies the dvfs value globally to any task 
*    that does not have specific DVFS mode setting.
*    Tasks with a specific DVFS mode setting override the global DFVS mode.
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_dvfs_set_mode
(
   /* [IN] the task_id to apply DVFS setting */
   _task_id tid, 
                   
   /* [IN] the DVFS value to use (0-3) */      
   uchar dvfs  
)
{ /* Body */

   TD_STRUCT_PTR          td_ptr;
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   /* check the DVFS mode */
   if (dvfs != MQX_PMU_DVFS_PER_MODE_1) {
      if (dvfs != MQX_PMU_DVFS_PER_MODE_2) {
         if (dvfs != MQX_PMU_DVFS_PER_MODE_3) {
            if (dvfs != MQX_PMU_DVFS_PER_MODE_4) {
               return MQX_INVALID_PARAMETER;
            } /* Endif */
         } /* Endif */
      } /* Endif */
   } /* Endif */

   /* task ID 0 */
   if (tid == MQX_NULL_TASK_ID) {
      /* set the global DVFS mode */
      pmu_ptr->GLOBAL_DVFS_MODE = dvfs;
      /* save a copy of global DVFS mode */
      pmu_ptr->SAVED_GLOBAL_DVFS_MODE = dvfs;
      return MQX_OK;
   } /* Endif */

   td_ptr = _task_get_td(tid);

   /* check the task ID */
   if (td_ptr == NULL) {
      return MQX_INVALID_TASK_ID;
   } /* Endif */

   /* set DVFS mode the task */
   td_ptr->TASK_DVFS_MODE = (_mqx_uint)dvfs;

   return MQX_OK;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_dvfs_set_auto
* Returned Value   : MQX_OK on success. Error code on failer.
* Comments         :
*    This function enables or disables automatic DVFS mode. 
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_dvfs_set_auto
(                   
   /* [IN] automatic DVFS enable/disable */
   uchar   auto_dvfs_enable
)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   if (auto_dvfs_enable == MQX_PMU_DVFS_AUTO_ENABLE) {
      /* 
      ** Enable auto mode save original global DVFS mode GLOBAL_DVFS_MODE
      ** will be holding the auto mode. Save original global DVFS mode 
      */
      pmu_ptr->SAVED_GLOBAL_DVFS_MODE = pmu_ptr->GLOBAL_DVFS_MODE;
      /* Reset the idle count and ticks for DVFS */
      pmu_ptr->AUTO_DVFS_IDLE_CNT = 0;
      pmu_ptr->AUTO_DVFS_TICKS = 0;
      /* Enable auto DVFS mode */
      pmu_ptr->AUTO_DVFS_ENABLE = MQX_PMU_DVFS_AUTO_ENABLE;
      return MQX_OK;
   } /* Endif*/

   if (auto_dvfs_enable == MQX_PMU_DVFS_AUTO_DISABLE) {
      /* Restore original global DVFS mode */
      pmu_ptr->GLOBAL_DVFS_MODE = pmu_ptr->SAVED_GLOBAL_DVFS_MODE;
      /* Disable auto DVFS mode */
      pmu_ptr->AUTO_DVFS_ENABLE = MQX_PMU_DVFS_AUTO_DISABLE;
      return MQX_OK;
   } /* Endif */
  
   return MQX_INVALID_PARAMETER;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_dvfs_set_thrshld
* Returned Value   : MQX_OK on success.
* Comments         :
*    This function specifies the thresholds for Automatic DVFS mode. The 
*    thresholds represent a percentage of idle time. When a specific threshold 
*    is met, the DVFS mode should be changed.
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_dvfs_set_thrshld
(
   /* [IN] threshold 0 to apply DVFS mode 0 */
   uchar dvfs_thrshld0, 

   /* [IN] threshold 1 to apply DVFS mode 1 */
   uchar dvfs_thrshld1, 

   /* [IN] threshold 2 to apply DVFS mode 2 */
   uchar dvfs_thrshld2, 

   /* [IN] time interval used to reset the idle time calculation */
   uint_32 period 

)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   pmu_ptr->AUTO_THRESHOLD_0 = dvfs_thrshld0;
   pmu_ptr->AUTO_THRESHOLD_1 = dvfs_thrshld1;
   pmu_ptr->AUTO_THRESHOLD_2 = dvfs_thrshld2;

   pmu_ptr->AUTO_DVFS_PERIOD = period;

   return MQX_OK;

} /* Endbody */

#endif /* MQX_USE_PMU */

/* EOF */