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
*** File: stack_de.c
***
*** Comments:      
***   This file contains the functions for manipulating the user
*** context on the stack.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_destroy_stack_frame
* Returned Value   : none
* Comments         :
*
*   This function performs any PSP specific destruction for a task
* context
*
*END*----------------------------------------------------------------------*/

void _psp_destroy_stack_frame
   (
      /* [IN] the task descriptor whose stack needs to be destroyed */
      TD_STRUCT_PTR td_ptr
   )
{/* Body */

   /* Nothing to do for this CPU */
      
}/* Endbody */

/* EOF */
