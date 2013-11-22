/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: wa_strti.c
***
*** Comments:
***   This file contains the function for starting a watchdog.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "watchdog.h"
#include "wdog_prv.h"

#if MQX_USE_SW_WATCHDOGS
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _watchdog_start_internal
* Returned Value  : boolean TRUE if succeeded
* Comments        : This function starts a software watchdog for the
*  current task.  The timer will expire at the specified number of
*  ticks in the future.
*
*END*------------------------------------------------------------------*/

boolean _watchdog_start_internal
   (
      /*  [IN]  the time in ticks at which to expire the watchdog */
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR          kernel_data;
   TD_STRUCT_PTR                   td_ptr;
   WATCHDOG_COMPONENT_STRUCT_PTR   watchdog_component_ptr;
   WATCHDOG_ALARM_TABLE_STRUCT_PTR table_ptr;
   MQX_TICK_STRUCT                 kd_time;
   _mqx_uint                       i;

   _GET_KERNEL_DATA(kernel_data);

   td_ptr = kernel_data->ACTIVE_PTR;
   
   kd_time = kernel_data->TIME;

   if (td_ptr->FLAGS & TASK_WATCHDOG_STARTED) {
      _INT_DISABLE();
      PSP_ADD_TICKS(tick_ptr, &kd_time, &td_ptr->WATCHDOG_TIME);
      td_ptr->FLAGS |= TASK_WATCHDOG_RUNNING;
      _INT_ENABLE();
      return(TRUE);
   } /* Endif */
   
   watchdog_component_ptr = (WATCHDOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_WATCHDOG];
#if MQX_CHECK_ERRORS
   if (watchdog_component_ptr == NULL) {
      return(FALSE);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (watchdog_component_ptr->VALID != WATCHDOG_VALID) {
      return(FALSE);
   } /* Endif */
#endif

   table_ptr = &watchdog_component_ptr->ALARM_ENTRIES;
   
   _INT_DISABLE();
   while (table_ptr != NULL) {
      for (i = 0; i < WATCHDOG_TABLE_SIZE; ++i) {
         if (table_ptr->TD_PTRS[i] == NULL) {
            table_ptr->TD_PTRS[i] = td_ptr;
            PSP_ADD_TICKS(tick_ptr, &kd_time, &td_ptr->WATCHDOG_TIME);
            td_ptr->FLAGS |= TASK_WATCHDOG_STARTED | TASK_WATCHDOG_RUNNING;
            _INT_ENABLE();
            return(TRUE);
         } /* Endif */
      } /* Endwhile */
      if (table_ptr->NEXT_TABLE_PTR == NULL) {
         table_ptr->NEXT_TABLE_PTR = (WATCHDOG_ALARM_TABLE_STRUCT_PTR)
            _mem_alloc_system_zero((_mem_size)sizeof(WATCHDOG_ALARM_TABLE_STRUCT));
      } /* Endif */
      table_ptr = table_ptr->NEXT_TABLE_PTR;
   } /* Endwhile */

   _int_enable();
   return(FALSE);

} /* Endbody */
#endif /* MQX_USE_SW_WATCHDOGS */

/* EOF */
