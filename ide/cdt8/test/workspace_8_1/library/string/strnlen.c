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
*** File: strnlen.c
***
*** Comments:      
***   This file contains the function for calculating the length of a 
*** string, length limited.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "mqx_str.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _strnlen
* Returned Value   : unsigned
* Comments         :
*   This function calculates the length of a string, length limited
*
*END*----------------------------------------------------------------------*/

_mqx_uint _strnlen
   (

      /* [IN] the address of the string */
      register char_ptr string_ptr,

      /* [IN] the number to convert */
      register _mqx_uint max_len
   )
{ /* Body */
   register _mqx_uint i = 0;

   
   while (*string_ptr++ && max_len--) {
      ++i;
   } /* Endwhile */

   return(i);
   
} /* Endbody */

/* EOF */
