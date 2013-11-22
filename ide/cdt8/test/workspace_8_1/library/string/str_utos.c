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
*** File: str_utos.c
***
*** Comments:      
***   This file contains the function for converting a _mqx_uint to a string.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "mqx_str.h"

const char _str_hex_array_internal[] = "0123456789ABCDEF";

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _str__mqx_uint_to_string
* Returned Value   : none
* Comments         :
*   This function converts a _mqx_uint to a string
*
*END*----------------------------------------------------------------------*/

void _str_mqx_uint_to_hex_string
   (
      /* [IN] the number to convert */
      register _mqx_uint num,

      /* [OUT] the address of the string to write to */
      register char_ptr string
   )
{ /* Body */

#if (MQX_INT_SIZE_IN_BITS == 64)
   string[0]  = _str_hex_array_internal[(num >> 60)&0xF];
   string[1]  = _str_hex_array_internal[(num >> 56)&0xF];
   string[2]  = _str_hex_array_internal[(num >> 52)&0xF];
   string[3]  = _str_hex_array_internal[(num >> 48)&0xF];
   string[4]  = _str_hex_array_internal[(num >> 44)&0xF];
   string[5]  = _str_hex_array_internal[(num >> 40)&0xF];
   string[6]  = _str_hex_array_internal[(num >> 36)&0xF];
   string[7]  = _str_hex_array_internal[(num >> 32)&0xF];
   string[8]  = _str_hex_array_internal[(num >> 28)&0xF];
   string[9]  = _str_hex_array_internal[(num >> 24)&0xF];
   string[10] = _str_hex_array_internal[(num >> 20)&0xF];
   string[11] = _str_hex_array_internal[(num >> 16)&0xF];
   string[12] = _str_hex_array_internal[(num >> 12)&0xF];
   string[13] = _str_hex_array_internal[(num >>  8)&0xF];
   string[14] = _str_hex_array_internal[(num >>  4)&0xF];
   string[16] = _str_hex_array_internal[(num      )&0xF];
   string[17] = 0;
#elif (MQX_INT_SIZE_IN_BITS == 32)
   string[0] = _str_hex_array_internal[(num >> 28)&0xF];
   string[1] = _str_hex_array_internal[(num >> 24)&0xF];
   string[2] = _str_hex_array_internal[(num >> 20)&0xF];
   string[3] = _str_hex_array_internal[(num >> 16)&0xF];
   string[4] = _str_hex_array_internal[(num >> 12)&0xF];
   string[5] = _str_hex_array_internal[(num >>  8)&0xF];
   string[6] = _str_hex_array_internal[(num >>  4)&0xF];
   string[7] = _str_hex_array_internal[(num      )&0xF];
   string[8] = 0;
#elif (MQX_INT_SIZE_IN_BITS == 24)
   string[0] = _str_hex_array_internal[(num >> 20)&0xF];
   string[1] = _str_hex_array_internal[(num >> 16)&0xF];
   string[2] = _str_hex_array_internal[(num >> 12)&0xF];
   string[3] = _str_hex_array_internal[(num >>  8)&0xF];
   string[4] = _str_hex_array_internal[(num >>  4)&0xF];
   string[5] = _str_hex_array_internal[(num      )&0xF];
   string[6] = 0;
#elif (MQX_INT_SIZE_IN_BITS == 16)
   string[0] = _str_hex_array_internal[(num >> 12)&0xF];
   string[1] = _str_hex_array_internal[(num >>  8)&0xF];
   string[2] = _str_hex_array_internal[(num >>  4)&0xF];
   string[3] = _str_hex_array_internal[(num      )&0xF];
   string[4] = 0;
#endif

} /* Endbody */

/* EOF */
