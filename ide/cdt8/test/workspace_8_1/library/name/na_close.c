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
*** File: na_close.c
***
*** Comments:      
***   This file contains the internal function for deleting a name.
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
* Function Name    : _name_destroy_handle_internal
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function completely removes the specified name table.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_destroy_handle_internal
   (
      /* [IN] the name data structure pointer  */
      pointer name_handle
   )
{ /* Body */
   register NAME_COMPONENT_STRUCT_PTR name_manager_ptr;
   register NAME_COMPONENT_STRUCT_PTR next_ptr;

   name_manager_ptr = (NAME_COMPONENT_STRUCT_PTR) name_handle;
#if MQX_CHECK_ERRORS
   if (name_manager_ptr == NULL){
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif

   _int_disable();
#if MQX_CHECK_VALIDITY
   if (name_manager_ptr->VALID != NAME_VALID){
      _int_enable();
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif
   _lwsem_wait((LWSEM_STRUCT_PTR)(&name_manager_ptr->SEM));
   name_manager_ptr->VALID = 0;
   _int_enable();

   _lwsem_destroy((LWSEM_STRUCT_PTR)(&name_manager_ptr->SEM));

   while (name_manager_ptr) {
      next_ptr = name_manager_ptr->NEXT_TABLE;
      name_manager_ptr->VALID = 0;
      _mem_free(name_manager_ptr);
      name_manager_ptr = next_ptr;
   } /* Endwhile */

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */
