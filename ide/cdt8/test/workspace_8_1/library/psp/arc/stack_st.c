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
*** File: stack_st.c
***
*** Comments:      
***   This file contains the function for obtaining the address of the
*** stack start structure for the task.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _psp_get_stack_start
* Returned Value   : 
* Comments         :
*   This function returns the pointer to the stack start structure on
* the stack.
*
*END*-------------------------------------------------------------------------*/

PSP_STACK_START_STRUCT_PTR _psp_get_stack_start
   (
      /* [IN] the task descriptor whose stack start struct address is wanted */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */

   return (pointer)((uchar _PTR_)td_ptr->STACK_BASE -
      sizeof(PSP_STACK_START_STRUCT));

} /* Endbody */

/* EOF */
