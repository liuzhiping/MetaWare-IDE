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
*** File: na_fbi.c
***
*** Comments:      
***   This file contains the internal function for finding the number
*** of a name entry, given the index of the name in the component.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"

#if MQX_USE_NAME
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _name_find_internal_by_index
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function looks up a name in the name component, and returns the
* _mqx_max_type quantity associated with the name.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_find_internal_by_index
   (
      /* [IN] the handle returned by _name_internal_create */
      pointer           name_handle,

      /* [IN] the nth name to locate */
      _mqx_uint         index,

      /* [OUT] the location where the number is to be returned */
      _mqx_max_type_ptr number_ptr
   )
{ /* Body */
   register NAME_COMPONENT_STRUCT_PTR name_manager_ptr;
   register NAME_STRUCT_PTR           name_ptr;

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
   while (index >= name_manager_ptr->NUMBER_IN_BLOCK) {
      index -= name_manager_ptr->NUMBER_IN_BLOCK;
      if (name_manager_ptr->NEXT_TABLE == NULL) {
         return(NAME_NOT_FOUND);
      } /* Endif */
      name_manager_ptr = name_manager_ptr->NEXT_TABLE;
#if MQX_CHECK_VALIDITY
      if (name_manager_ptr->VALID != NAME_VALID) {
         return(MQX_INVALID_COMPONENT_BASE);
      } /* Endif */
#endif
   }/* Endwhile */
   name_ptr = (NAME_STRUCT_PTR)&name_manager_ptr->NAMES[index];
   if (name_ptr->NAME[0] == '\0') {
      return(NAME_NOT_FOUND);
   } /* Endif */
   *number_ptr = name_ptr->NUMBER;

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */
