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
*** File: kl_log.c
***
*** Comments:      
***   This file contains the functions for Kernel Logging Component.
*** This function is called at various parts of the kernel:
***    at function entry, 
***    at function exit,
***    at each interrupt entry
***    at each interrupt exit
***    at each context switch.  
***
*** The application may also write to the kernel log,
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwlog.h"
#include "lwlogprv.h"
#include "klog.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_log
* Returned Value   : none
* Comments         :
*   This function logs information into the kernel log.
*
*END*----------------------------------------------------------------------*/

void _klog_log
   (
      /* [IN] what type of log is this */
      _mqx_uint     type,

      /* [IN] parameter 1 */
      _mqx_max_type p1,

      /* [IN] parameter 2 */
      _mqx_max_type p2,

      /* [IN] parameter 3 */
      _mqx_max_type p3,

      /* [IN] parameter 4 */
      _mqx_max_type p4,

      /* [IN] parameter 5 */
      _mqx_max_type p5
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

   KERNEL_DATA_STRUCT_PTR kernel_data;
   _mqx_max_type          calling_pc;

   _GET_KERNEL_DATA(kernel_data);
   if ( !(kernel_data->LOG_CONTROL & KLOG_ENABLED) ) {
      return;
   } /* Endif */
   if ( kernel_data->LOG_CONTROL & KLOG_TASK_QUALIFIED ) {
      if (! (kernel_data->ACTIVE_PTR->FLAGS & TASK_LOGGING_ENABLED) ) {
         return;
      } /* Endif */
   } /* Endif */

   if ((type == KLOG_FUNCTION_ENTRY) || (type == KLOG_FUNCTION_EXIT))  {
      if ( !(kernel_data->LOG_CONTROL & KLOG_FUNCTIONS_ENABLED) ) {
         return;
      } /* Endif */
      /* Functions enabled, now lets check function group enabled */
      if (((p1 & KLOG_FUNCTION_MASK) & kernel_data->LOG_CONTROL) == 0) {
         return;
      } /* Endif */
   } /* Endif */

    if (type == KLOG_FUNCTION_ENTRY) {
       calling_pc = (_mqx_max_type)_PSP_GET_CALLING_PC();  
    } else {
       calling_pc = 0;
    } /* Endif */

   _INT_DISABLE();
   _lwlog_write(LOG_KERNEL_LOG_NUMBER, (_mqx_max_type)type, p1, p2, p3, p4, p5,
       calling_pc);
   _INT_ENABLE();

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_block_internal
* Returned Value   : none
* Comments         :
*   This function is called from _task_block for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_block_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   if ( kernel_data->LOG_CONTROL & KLOG_TASK_QUALIFIED ) {
      if (! (kernel_data->ACTIVE_PTR->FLAGS & TASK_LOGGING_ENABLED) ) {
         return;
      } /* Endif */
   } /* Endif */

   if (kernel_data->LOG_CONTROL & KLOG_FUNCTIONS_ENABLED) {
      if ((KLOG_task_block & KLOG_FUNCTION_MASK) & kernel_data->LOG_CONTROL){
         if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]) {
            td_ptr = kernel_data->ACTIVE_PTR;
            _lwlog_write_internal(LOG_KERNEL_LOG_NUMBER,  
               (_mqx_max_type)KLOG_FUNCTION_ENTRY,
               (_mqx_max_type)KLOG_task_block,   (_mqx_max_type)td_ptr,
               (_mqx_max_type)td_ptr->TASK_ID,   (_mqx_max_type)td_ptr->STATE,
               (_mqx_max_type)td_ptr->STACK_PTR, (_mqx_max_type)0);
         } /* Endif */
      } /* Endif */
   } /* Endif */

} /* Endbody */
    

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_execute_scheduler_internal
* Returned Value   : none
* Comments         :
*   This function is called from _sched_execute_scheduler_internal for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_execute_scheduler_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   if ( kernel_data->LOG_CONTROL & KLOG_TASK_QUALIFIED ) {
      if (! (kernel_data->ACTIVE_PTR->FLAGS & TASK_LOGGING_ENABLED) ) {
         return;
      } /* Endif */
   } /* Endif */

   if (kernel_data->LOG_CONTROL & KLOG_FUNCTIONS_ENABLED) {
      if ((KLOG_task_execute_scheduler & KLOG_FUNCTION_MASK) & kernel_data->LOG_CONTROL){
         if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]) {
            td_ptr = kernel_data->ACTIVE_PTR;
            _lwlog_write_internal(LOG_KERNEL_LOG_NUMBER, 
               (_mqx_max_type)KLOG_FUNCTION_ENTRY, 
               (_mqx_max_type)KLOG_task_execute_scheduler, (_mqx_max_type)td_ptr,
               (_mqx_max_type)td_ptr->TASK_ID, (_mqx_max_type)td_ptr->STATE,
               (_mqx_max_type)td_ptr->STACK_PTR, (_mqx_max_type)0);
         } /* Endif */
      } /* Endif */
   } /* Endif */

} /* Endbody */
    

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_yield_internal
* Returned Value   : none
* Comments         :
*   This function is called from _sched_yield for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_yield_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   if ( kernel_data->LOG_CONTROL & KLOG_TASK_QUALIFIED ) {
      if (! (kernel_data->ACTIVE_PTR->FLAGS & TASK_LOGGING_ENABLED) ) {
         return;
      } /* Endif */
   } /* Endif */

   if (kernel_data->LOG_CONTROL & KLOG_FUNCTIONS_ENABLED) {
      if ((KLOG_sched_yield & KLOG_FUNCTION_MASK) & kernel_data->LOG_CONTROL){
         if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]) {
            td_ptr = kernel_data->ACTIVE_PTR;
            _lwlog_write_internal(LOG_KERNEL_LOG_NUMBER, 
               (_mqx_max_type)KLOG_FUNCTION_ENTRY,
               (_mqx_max_type)KLOG_sched_yield, (_mqx_max_type)td_ptr,
               (_mqx_max_type)td_ptr->TASK_ID,  (_mqx_max_type)td_ptr->STATE,
               (_mqx_max_type)td_ptr->STACK_PTR, (_mqx_max_type)0);
         } /* Endif */
      } /* Endif */
   } /* Endif */

} /* Endbody */

/* Start CR 2396 */
#if MQX_USE_PMU
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_pmu_powerdown_start
* Returned Value   : none
* Comments         :
*   This function is called only for PMU for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_pmu_powerdown_start
(
   void
)
{ /* Body */
   KERNEL_DATA_STRUCT _PTR_ kernel_data; 
   PMU_STRUCT_PTR           pmu_ptr;
   
   _GET_KERNEL_DATA(kernel_data);

   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   _KLOGE4(KLOG_pmu_powerdown_start, pmu_ptr->PMU_MODE, pmu_ptr->PMU_POWER_CONSUMPTION, 0);

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_pmu_powerdown_end
* Returned Value   : none
* Comments         :
*   This function is called only for PMU for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_pmu_powerdown_end
(
   void
)
{ /* Body */
	KERNEL_DATA_STRUCT _PTR_ kernel_data; 
	PMU_STRUCT_PTR           pmu_ptr;

	uint_8    current_dvfs_mode;
	uint_32	  param1;

	_GET_KERNEL_DATA(kernel_data);

	pmu_ptr = kernel_data->PMU_STRUCT_PTR;

	current_dvfs_mode = _psp_get_aux(PSP_AUX_PMU_DVFS_PERFORMANCE)+1;
	param1 = (current_dvfs_mode << 28) | pmu_ptr->POWER_CONSUMPTION[current_dvfs_mode-1];

   _KLOGE4(KLOG_pmu_powerdown_end, pmu_ptr->PMU_MODE, param1, pmu_ptr->TIMER_FREQUNCY[current_dvfs_mode-1]);

} /* Endbody */
#endif
/* End CR 2396 */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_context_switch_internal
* Returned Value   : none
* Comments         :
*   This function is called from _sched_dispatch for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_context_switch_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);

   if (kernel_data->LOG_CONTROL & KLOG_CONTEXT_ENABLED) {
      if (kernel_data->ACTIVE_PTR != kernel_data->LOG_OLD_TD) {
         if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]) {
            kernel_data->LOG_OLD_TD = kernel_data->ACTIVE_PTR;
            if ( kernel_data->LOG_CONTROL & KLOG_TASK_QUALIFIED ) {
               if (! (kernel_data->ACTIVE_PTR->FLAGS & TASK_LOGGING_ENABLED) ) {
                  return;
               } /* Endif */
            } /* Endif */
            td_ptr = kernel_data->ACTIVE_PTR;
            _lwlog_write_internal(LOG_KERNEL_LOG_NUMBER, 
               (_mqx_max_type)KLOG_CONTEXT_SWITCH,
               (_mqx_max_type)td_ptr, (_mqx_max_type)td_ptr->TASK_ID,
               (_mqx_max_type)td_ptr->STATE, (_mqx_max_type)td_ptr->STACK_PTR, 
               (_mqx_max_type)0, (_mqx_max_type)0);
         } /* Endif */
      } /* Endif */
   } /* Endif */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_isr_start_internal
* Returned Value   : none
* Comments         :
*   This function is called from _int_kernel_isr for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_isr_start_internal
   (
      _mqx_uint vector_number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   
   _GET_KERNEL_DATA(kernel_data);
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]) {
      if (kernel_data->LOG_CONTROL & KLOG_INTERRUPTS_ENABLED){
         if (!(kernel_data->LOG_CONTROL & KLOG_SYSTEM_CLOCK_INT_ENABLED)){
            /* Check to see if the vector number is to be ignored */
            if (vector_number == kernel_data->SYSTEM_CLOCK_INT_NUMBER) {
               return;
            } /* Endif */
         } /* Endif */
         _lwlog_write_internal(LOG_KERNEL_LOG_NUMBER, 
            (_mqx_max_type)KLOG_INTERRUPT, (_mqx_max_type)vector_number, 
            (_mqx_max_type)_int_get_isr(vector_number),
            (_mqx_max_type)_int_get_isr_data(vector_number), (_mqx_max_type)0, 
            (_mqx_max_type)0, (_mqx_max_type)0);
      } /* Endif */
   } /* Endif */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_isr_end_internal
* Returned Value   : none
* Comments         :
*   This function is called from _int_kernel_isr for logging purposes
*
*END*----------------------------------------------------------------------*/

void _klog_isr_end_internal
   (
      _mqx_uint vector_number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   
   _GET_KERNEL_DATA(kernel_data);
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG]) {
      if (kernel_data->LOG_CONTROL & KLOG_INTERRUPTS_ENABLED){
         if (!(kernel_data->LOG_CONTROL & KLOG_SYSTEM_CLOCK_INT_ENABLED)){
            /* Check to see if the vector number is to be ignored */
            if (vector_number == kernel_data->SYSTEM_CLOCK_INT_NUMBER) {
               return;
            } /* Endif */
         } /* Endif */
         _lwlog_write_internal(LOG_KERNEL_LOG_NUMBER, 
            (_mqx_max_type)KLOG_INTERRUPT_END, (_mqx_max_type)vector_number, 
            (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0, 
            (_mqx_max_type)0, (_mqx_max_type)0);
      } /* Endif */
   } /* Endif */

/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */

/* EOF */
