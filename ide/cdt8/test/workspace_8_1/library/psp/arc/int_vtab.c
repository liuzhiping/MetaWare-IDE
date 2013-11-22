/*HEADER*******************************************************************
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
*** File: int_vtab.c
***
*** Comments:      
***   This file contains the functions used to access the vector
*** table locations.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_set_vector_table
* Returned Value   : pointer old_vector table address
* Comments         :
*    This function changes the VBR to point to the new address
*  and returns the old interuupt vector table pointer (VBR) value.
*
************************************************************************/

_mqx_max_type _int_set_vector_table
   (
      /* [IN] the new address for the vector table */
      _mqx_max_type new
   )
{ /* Body */
   _mqx_max_type vbr;

   vbr = _int_get_vector_table();
   
   _psp_set_aux(PSP_AUX_BCR_VECBASE, new & 0xFFFFFC00);

   return vbr;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_vector_table
* Returned Value   : pointer address of vector table
* Comments         :
*    This function returns the current vector table pointer
*
************************************************************************/

_mqx_max_type _int_get_vector_table
   (
      void
   )
{ /* Body */
   _mqx_max_type vbr;

   vbr = (_mqx_max_type)(_psp_get_aux(PSP_AUX_BCR_VECBASE) & 0xFFFFFC00);

   return vbr;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_previous_vector_table
* Returned Value   : pointer address of vector table before MQX ran
* Comments         :
*    This function returns the saved vector table pointer
*
************************************************************************/

_mqx_max_type _int_get_previous_vector_table
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   return((_mqx_max_type)kernel_data->USERS_VBR);

} /* Endbody */

/* EOF */
