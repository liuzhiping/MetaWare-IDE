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
*** File: na_addi.c
***
*** Comments:      
***   This file contains the internal function for adding a name.
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
* Function Name    : _name_add_internal
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function adds a name to the name component along with the _mqx_max_type
* number associated with the name.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_add_internal
   (
      /* [IN] the handle returned by _name_internal_create */
      pointer        name_handle,

      /* [IN] the name to be associated with the number */
      char _PTR_     name, 

      /* [IN] the number to be associated with the name */
      _mqx_max_type  number
   )
{ /* Body */
            NAME_COMPONENT_STRUCT_PTR base_name_manager_ptr;
   register NAME_COMPONENT_STRUCT_PTR name_manager_ptr;
   register NAME_STRUCT_PTR           name_ptr;
   register NAME_STRUCT_PTR           saved_name_ptr;
   register _mqx_uint                  i;
            _mqx_uint                  result = MQX_OK;

#if MQX_CHECK_ERRORS
   if (*name == '\0') {
      /* Cannot add 0 length string name */
      return(NAME_TOO_SHORT);
   } /* Endif */
   if (_strnlen(name, NAME_MAX_NAME_SIZE) >= NAME_MAX_NAME_SIZE) {
      return(NAME_TOO_LONG);
   } /* Endif */
#endif

   name_manager_ptr = (NAME_COMPONENT_STRUCT_PTR) name_handle;
#if MQX_CHECK_ERRORS
   if (name_manager_ptr == NULL){
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
   base_name_manager_ptr = name_manager_ptr;

   /* We are modifying the table, so lets get exclusive access */
   _int_disable();
#if MQX_CHECK_VALIDITY
   if ((name_manager_ptr->VALID != NAME_VALID) ||
       (base_name_manager_ptr->VALID != NAME_VALID))
   {
      _int_enable();
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif
   _lwsem_wait((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));
   _int_enable();

   /* 
   ** Scan to end of table, looking for a duplicate name..
   ** simultaneously, remember the first empty slot found
   */
   saved_name_ptr = NULL;
   while (TRUE) {
      i        = name_manager_ptr->NUMBER_IN_BLOCK + 1;
      name_ptr = &name_manager_ptr->NAMES[0];
      while (--i) {
         if (name_ptr->NAME[0] == '\0') {
            if (saved_name_ptr == NULL) {
               saved_name_ptr = name_ptr;
            } /* Endif */
         } else {
            if (strncmp(name_ptr->NAME, name, (_mqx_uint)NAME_MAX_NAME_SIZE-1) 
               == 0) 
            { /* MATCH */
               result = NAME_EXISTS;
               break;
            } /* Endif */
         } /* Endif */
         name_ptr++;
      } /* Endwhile */
      if ((name_manager_ptr->NEXT_TABLE == NULL) || (result != MQX_OK)){
         break;
      } /* Endif */
      name_manager_ptr = name_manager_ptr->NEXT_TABLE;
   } /* Endwhile */

   if (result == MQX_OK) {
      /* We have scanned the whole table, and the original name was not found */

      if (saved_name_ptr == NULL) {
          /* 
          ** An empty slot was not found in the table so we must try to
          ** grow the name table
          */
         if (base_name_manager_ptr->GROW_NUMBER &&
            ((name_manager_ptr->TOTAL_NUMBER + base_name_manager_ptr->GROW_NUMBER) <=
            base_name_manager_ptr->MAX_NUMBER))
         {
            result = _name_create_handle_internal(
               (pointer _PTR_)&name_manager_ptr->NEXT_TABLE,
               base_name_manager_ptr->GROW_NUMBER, 
               base_name_manager_ptr->GROW_NUMBER,
               base_name_manager_ptr->MAX_NUMBER,
               (name_manager_ptr->TOTAL_NUMBER +
                 base_name_manager_ptr->GROW_NUMBER));
            if (result == MQX_OK) {
               name_manager_ptr = name_manager_ptr->NEXT_TABLE;
               saved_name_ptr   = &name_manager_ptr->NAMES[0];
            } /* Endif */
         } else {
            result = NAME_TABLE_FULL;
         } /* Endif */
      } /* Endif */
      
      if (saved_name_ptr != NULL) {
         strncpy(saved_name_ptr->NAME, name, (_mqx_uint)NAME_MAX_NAME_SIZE-1);
         saved_name_ptr->NAME[NAME_MAX_NAME_SIZE-1] = '\0';
         saved_name_ptr->NUMBER = number;
         base_name_manager_ptr->NUMBER++;
      }/* Endif */
   }/* Endif */

   _lwsem_post((LWSEM_STRUCT_PTR)(&base_name_manager_ptr->SEM));

   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */
