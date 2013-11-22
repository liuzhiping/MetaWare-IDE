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
*** File: ep_pmu.c
***
*** Comments:      
***    This file contains the PMU functions. This file is added to address
***    CR 2396.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "pmu.h"

#if MQX_USE_PMU

/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_pmu_set_pwrdn_mode
* Returned Value   : MQX_OK on success. Error code on failer.
* Comments         :
*    This function sets the current power down mode. Valid modes are 1 and 2. 
*    A 0 disables the power down feature. 
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_pmu_set_pwrdn_mode
(
   /* [IN] the Mode (0-2) 0 is off */
   uchar pwr_dn_mode
)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);

   if (pwr_dn_mode != MQX_PMU_POWER_DOWN_MODE_0) { 
      if (pwr_dn_mode != MQX_PMU_POWER_DOWN_MODE_1) {
         if (pwr_dn_mode != MQX_PMU_POWER_DOWN_MODE_2) {
            return MQX_INVALID_PARAMETER;
         } /* Endif */
      } /* Endif */
   } /* Endif */
   
   pmu_ptr = kernel_data->PMU_STRUCT_PTR;
   pmu_ptr->PMU_MODE = pwr_dn_mode;

   return MQX_OK;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_pmu_get_pwrdn_mode
* Returned Value   : Current power down mode.
* Comments         :
*    This function returns the current Power down mode. Valid modes are 0, 1 
*    or 2.
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_pmu_get_pwrdn_mode(void)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;
   uint_32 pwrdn_mode;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;
   pwrdn_mode = pmu_ptr->PMU_MODE;

   return pwrdn_mode;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_pmu_set_timer_thrshld
* Returned Value   : MQX_OK on success. Error code on failer.
* Comments         :
*    This function sets the current threshold for increasing the time interval
*    between interrupts. 
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_pmu_set_timer_thrshld
(
   /* [IN] the value of timer interval thrshld */
   uint_32 pwr_dn_thrshld
)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;
   pmu_ptr->SLEEP_THRSHLD = pwr_dn_thrshld;

   return MQX_OK;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------------
* 
* Function Name    : _ep_pmu_set_max_slw_dwn
* Returned Value   : MQX_OK on success. Error code on failer.
* Comments         :
*    This function sets the maximum number of times that the timer interval 
*    can be increased.
*
*END*----------------------------------------------------------------------*/

uint_32 _ep_pmu_set_max_slw_dwn
(
   /* [IN] the value of timer interval thrshld */
   uint_32 max_num_slw_dwn
)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   PMU_STRUCT_PTR         pmu_ptr;

   _GET_KERNEL_DATA(kernel_data);
 
   pmu_ptr = kernel_data->PMU_STRUCT_PTR;
   pmu_ptr->MAX_SLOW_DOWN_NUM = max_num_slw_dwn;

   return MQX_OK;

} /* Endbody */

#endif /* MQX_USE_PMU */

/* EOF */
