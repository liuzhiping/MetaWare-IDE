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
*** File: sc_gprio.c
***
*** Comments:      
***   This file contains the functions for obtaining priority
*** maximum and minimums for the running kernel.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_get_max_priority
* Returned Value   : _mqx_uint 0
* Comments         :
*   This function always returns 0, the highest priority a task may have
* under MQX.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_get_max_priority
   /* ARGS USED */
   (
      /* [IN] - not used, all task priorities same for RR or FIFO */
      _mqx_uint policy
   )
{ /* Body */

   return( 0 );
   
} /* Endbody */
   

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_get_min_priority
* Returned Value   : _mqx_uint lowest_task_priority
* Comments         :
*   This function returns the priority associated with the lowest priority
* task in the system (the priority of the _Idle Task - 1)
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_get_min_priority
   (
      /* [IN] - not used, all task priorities same for RR or FIFO */
      _mqx_uint policy
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   
   return( kernel_data->LOWEST_TASK_PRIORITY );
  
} /* Endbody */

/* EOF */
