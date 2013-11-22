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
*** File: mqx_stad.c
***
*** Comments:      
***   This file contains the function for returning TAD_RESERVED field
***                                                               
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_set_tad_data
* Returned Value   : none
* Comments         : 
*   This function sets the value of the TAD_RESERVED field.
*
*END*----------------------------------------------------------------------*/

void _mqx_set_tad_data
   (
      /* [IN] the TD address */
      pointer  td,

      /* [IN] the tad data */
      pointer  tad_data
   )
{ /* Body */
   TD_STRUCT_PTR td_ptr = (TD_STRUCT_PTR)td;

   td_ptr->TAD_RESERVED = tad_data;

} /* Endbody */

/* EOF */
