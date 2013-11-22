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
*** File: na_dbi.c
***
*** Comments:      
***   This file contains the internal function for deleting a name.
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
* Function Name    : _name_delete_internal_by_index
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function removes a name from the name component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_delete_internal_by_index
   (
      /* [IN] the handle returned by _name_internal_create */
      pointer   name_handle,

      /* [IN] the index in the name table */
      _mqx_uint index
   )
{ /* Body */
   register NAME_COMPONENT_STRUCT_PTR base_name_manager_ptr;
   register NAME_COMPONENT_STRUCT_PTR name_manager_ptr;
   register NAME_STRUCT_PTR           name_ptr;
            _mqx_uint                 result = MQX_OK;

   base_name_manager_ptr = (NAME_COMPONENT_STRUCT_PTR) name_handle;
#if MQX_CHECK_ERRORS
   if (base_name_manager_ptr == NULL)  {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

   _int_disable();
#if MQX_CHECK_VALIDITY
   if (base_name_manager_ptr->VALID != NAME_VALID) {
      _int_enable();
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif
   _lwsem_wait((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));
   _int_enable();

   /* Scan to end of table, and if the name found, delete it */
   name_manager_ptr = base_name_manager_ptr;
   /* Scan to end of table, if found, return number */
   while (index >= name_manager_ptr->NUMBER_IN_BLOCK) {
      index -= name_manager_ptr->NUMBER_IN_BLOCK;
      if (name_manager_ptr->NEXT_TABLE == NULL) {
         result = NAME_NOT_FOUND;
         break;
      } /* Endif */
      name_manager_ptr = name_manager_ptr->NEXT_TABLE;
#if MQX_CHECK_VALIDITY
      if (name_manager_ptr->VALID != NAME_VALID) {
         result = MQX_INVALID_COMPONENT_BASE;
         break;
      } /* Endif */
#endif
   }/* Endwhile */

   if (result == MQX_OK) {
      name_ptr = (NAME_STRUCT_PTR)&name_manager_ptr->NAMES[index];
      name_ptr->NAME[0]  = '\0';
      name_ptr->NUMBER   = 0;
      base_name_manager_ptr->NUMBER--;
   } /* Endif */

   _lwsem_post((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */
