/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: na_fni.c
***
*** Comments:      
***   This file contains the internal function for finding a name
*** in the name component, given the number.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"

#if MQX_USE_NAME
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _name_find_name_internal
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function looks up a number in the name component, and returns the
* name associated with the number.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_find_name_internal
   (
      /* [IN] the handle returned by _name_internal_create */
      pointer       name_handle,

      /* [IN] the number to be looked up */
      _mqx_max_type number,

      /* [OUT] the location where the name is to be written to */
      char_ptr      name_string_ptr
   )
{ /* Body */
   register NAME_COMPONENT_STRUCT_PTR name_manager_ptr;
   register NAME_STRUCT_PTR           name_ptr;
   register _mqx_uint                 i;

   name_manager_ptr = (NAME_COMPONENT_STRUCT_PTR) name_handle;
#if MQX_CHECK_ERRORS
   if (name_manager_ptr == NULL)  {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (name_manager_ptr->VALID != NAME_VALID) {
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif

   /* Scan to end of table, if found, return number */
   while (TRUE) {
      i        = name_manager_ptr->NUMBER_IN_BLOCK + 1;
      name_ptr = (NAME_STRUCT_PTR)&name_manager_ptr->NAMES[0];
      while (--i) {
         if (name_ptr->NAME[0] != '\0') {
            if (number == name_ptr->NUMBER) {  /* MATCH */
               strncpy(name_string_ptr, name_ptr->NAME, 
                  (_mqx_uint)NAME_MAX_NAME_SIZE-1);
               name_string_ptr[NAME_MAX_NAME_SIZE-1] = '\0';
               return(MQX_OK);
            } /* Endif */
         } /* Endif */
         name_ptr++;
      } /* Endwhile */

      if (name_manager_ptr->NEXT_TABLE == NULL) {
         return(NAME_NOT_FOUND);
      } else {
         name_manager_ptr = name_manager_ptr->NEXT_TABLE;
      } /* Endif */
#if MQX_CHECK_VALIDITY
      if (name_manager_ptr->VALID != NAME_VALID) {
         return(MQX_INVALID_COMPONENT_BASE);
      } /* Endif */
#endif
   } /* Endwhile */
   
} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */
