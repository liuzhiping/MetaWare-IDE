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
*** File: na_deli.c
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
#include "mqx_str.h"

#if MQX_USE_NAME
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _name_delete_internal
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function removes a name from the name component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_delete_internal
   (
      /* [IN] the handle returned by _name_internal_create */
      pointer  name_handle,

      /* [IN] the name to be deleted */
      char_ptr name
   )
{ /* Body */
            NAME_COMPONENT_STRUCT_PTR base_name_manager_ptr;
   register NAME_COMPONENT_STRUCT_PTR name_manager_ptr;
   register NAME_STRUCT_PTR           name_ptr;
   register _mqx_uint                 i;

   name_manager_ptr = (NAME_COMPONENT_STRUCT_PTR) name_handle;
#if MQX_CHECK_ERRORS
   if (name_manager_ptr == NULL)  {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

   base_name_manager_ptr = name_manager_ptr;

   /* We are modifying the table, so lets get exclusive access */
   _int_disable();
#if MQX_CHECK_VALIDITY
   if (name_manager_ptr->VALID != NAME_VALID) {
      _int_enable();
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif
#if MQX_CHECK_ERRORS
   if (*name == '\0') {
      /* Cannot delete 0 length string name */
      _int_enable();
      return(NAME_TOO_SHORT);
   } /* Endif */
   if (_strnlen(name, NAME_MAX_NAME_SIZE) >= NAME_MAX_NAME_SIZE) {
      _int_enable();
      return(NAME_TOO_LONG);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (base_name_manager_ptr->VALID != NAME_VALID) {
      _int_enable();
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif
   _lwsem_wait((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));

   _int_enable();

   /* Scan to end of table, and if the name found, delete it */
   while (TRUE) {
      i        = name_manager_ptr->NUMBER_IN_BLOCK + 1;
      name_ptr = &name_manager_ptr->NAMES[0];
      while (--i) {
         if (name_ptr->NAME[0] != '\0') {
            if (strncmp(name_ptr->NAME, name, (_mqx_uint)NAME_MAX_NAME_SIZE-1) 
               == 0) 
            {  /* MATCH */
               name_ptr->NAME[0]  = '\0';
               name_ptr->NUMBER   = 0;
               base_name_manager_ptr->NUMBER--;
               _lwsem_post((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));
               return(MQX_OK);
            } /* Endif */
         } /* Endif */
         name_ptr++;
      } /* Endwhile */
      if (name_manager_ptr->NEXT_TABLE == NULL) {
         _lwsem_post((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));
         return(NAME_NOT_FOUND);
      } else {
         name_manager_ptr = name_manager_ptr->NEXT_TABLE;   
#if MQX_CHECK_VALIDITY
         if (name_manager_ptr->VALID != NAME_VALID) {
            _lwsem_post((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));
            return(MQX_INVALID_COMPONENT_BASE);
         } /* Endif */
#endif
      } /* Endif */
   } /* Endwhile */

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */
