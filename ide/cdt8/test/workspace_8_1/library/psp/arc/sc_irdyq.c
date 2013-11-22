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
*** File: sc_irdyq.c
***
*** Comments:      
***   This file contains psp functions for initializing the scheduler.n
***                                                               
*** $Header:sc_irdyq.c, 6, 2/12/2004 4:42:04 PM, $
***
*** $NoKeywords$
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_set_kernel_disable_level
* Returned Value   : 
* Comments         :
*    This function sets up the kernel disable priority.
*
*END*----------------------------------------------------------------------*/

void _psp_set_kernel_disable_level
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR        kernel_data;
   MQX_INITIALIZATION_STRUCT_PTR init_ptr;
   uint_32                       temp;

   _GET_KERNEL_DATA(kernel_data);

   init_ptr = (MQX_INITIALIZATION_STRUCT_PTR)&kernel_data->INIT;

   /* Compute the enable and disable interrupt values for the kernel. */
   temp = init_ptr->MQX_HARDWARE_INTERRUPT_LEVEL_MAX;
#if MQX_CHECK_ERRORS
   if (init_ptr->MQX_HARDWARE_INTERRUPT_LEVEL_MAX > 2 ) {
      temp = 2;
   } else if (init_ptr->MQX_HARDWARE_INTERRUPT_LEVEL_MAX == 0 ) {
      temp = 1;
   } /* Endif */

   init_ptr->MQX_HARDWARE_INTERRUPT_LEVEL_MAX = temp;
#endif
   if (temp == 2) {
      kernel_data->DISABLE_SR = 0x0000;
   } else {
      kernel_data->DISABLE_SR = 0x0004;
   } /* Endif */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_init_readyqs
* Returned Value   : uint_32 result_code
* Comments         :
*    This function sets up the kernel priority ready queues
*
*END*----------------------------------------------------------------------*/

uint_32 _psp_init_readyqs
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   READY_Q_STRUCT_PTR     q_ptr;
   TD_STRUCT_PTR          td_ptr;
   uint_32                priority_levels;

   _GET_KERNEL_DATA(kernel_data);
   kernel_data->READY_Q_LIST = (READY_Q_STRUCT_PTR) NULL;
   priority_levels = kernel_data->IDLE_TASK_TEMPLATE.TASK_PRIORITY + 1;

   q_ptr = (READY_Q_STRUCT_PTR)
      _mem_alloc_system_zero(sizeof(READY_Q_STRUCT) * priority_levels);
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if ( q_ptr == NULL ) {
      return (MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   while (priority_levels--) {
      td_ptr = (TD_STRUCT_PTR)((pointer)q_ptr);
      q_ptr->HEAD_READY_Q  = td_ptr;
      q_ptr->TAIL_READY_Q  = td_ptr;
      q_ptr->PRIORITY      = (uint_16)priority_levels;
      q_ptr->NEXT_Q        = kernel_data->READY_Q_LIST;
      q_ptr->ENABLE_SR     = PSP_SR32_E2 | PSP_SR32_E1; // All interrupts enabled
      kernel_data->READY_Q_LIST = q_ptr++;
   } /* Endwhile */

   /* Set first priority level */
   q_ptr =  kernel_data->READY_Q_LIST;
   q_ptr->ENABLE_SR = kernel_data->DISABLE_SR;
   if (kernel_data->DISABLE_SR == 0) {
      q_ptr = q_ptr->NEXT_Q;
      q_ptr->ENABLE_SR = PSP_SR32_E2;
   } /* Endif */

   /* 
   ** Set the current ready q (where the ready queue searches start) to
   ** the head of the list of ready queues.
   */
   kernel_data->CURRENT_READY_Q = kernel_data->READY_Q_LIST;

   return MQX_OK;

} /* Endbody */

/* EOF */
