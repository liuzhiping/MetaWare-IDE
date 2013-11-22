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
*** File: td_util.c
***
*** Comments:      
***   This file contains the functions for checking the stack
*** and returing the processor number of a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_check_stack
* Returned Value   : boolean
* Comments         : 
*    This function returns TRUE if stack is CURRENTLY overflowed.
*
*END*----------------------------------------------------------------------*/

boolean _task_check_stack
   (
      void
   )
{ /* Body */
   register  KERNEL_DATA_STRUCT_PTR kernel_data;
   volatile  char                   loc = (char)0;

   _GET_KERNEL_DATA(kernel_data);

#if PSP_STACK_GROWS_TO_LOWER_MEM
   if ( &loc < ((char _PTR_)kernel_data->ACTIVE_PTR->STACK_LIMIT) ) {
#else
   if ( &loc > ((char _PTR_)kernel_data->ACTIVE_PTR->STACK_LIMIT) ) {
#endif
      return (TRUE);
   } else {
      return (FALSE);
   } /* Endif */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_processor
* Returned Value   : _processor_number
* Comments         :
*    This function returns the processor number upon which the given
*    task id exists. 
*
*END*----------------------------------------------------------------------*/

_processor_number _task_get_processor
   (
      /* [IN] the task id whose processor number is required */
      _task_id task_id
   )
{ /* Body */

   return PROC_NUMBER_FROM_TASKID(task_id);

} /* Endbody */

/* EOF */
