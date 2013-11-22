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
*** File: mqx_gtad.c
***
*** Comments:      
***   This file contains the function for returning the TAD_RESERVED field
***                                                               
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_tad_data
* Returned Value   : pointer value of TAD reserved field
* Comments         : 
*   This function retrieves the value of the TAD_RESERVED field.
*
*END*----------------------------------------------------------------------*/

pointer _mqx_get_tad_data
   (
      /* [IN] the task descriptor to use */
      pointer  td
   )
{ /* Body */
   TD_STRUCT_PTR td_ptr = (TD_STRUCT_PTR)td;

   return td_ptr->TAD_RESERVED;

} /* Endbody */

/* EOF */
