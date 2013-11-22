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
*** File: arc.c
***
*** Comments:      
***   This file contains utiltity functions for use with an ARC cpu.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/* A zero initialized PSP tick structure */
const PSP_TICK_STRUCT _psp_zero_tick_struct = {0};

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _arc_initialize_support
* Returned Value   : none
* Comments         :
*  Initilize the support functions for the arc cpu.
*
*END*------------------------------------------------------------------------*/

_mqx_uint _arc_initialize_support
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PSP_SUPPORT_STRUCT_PTR psp_support_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   
   psp_support_ptr = _mem_alloc_system_zero((uint_32)
      sizeof(PSP_SUPPORT_STRUCT));

   if (psp_support_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */

   psp_support_ptr->NUM_EXTENSION_REGISTERS = PSP_NUM_EXTENSION_REGISTERS;
   psp_support_ptr->EXTENSION_REGISTERS = PSP_EXTENSION_REGISTERS;

   kernel_data->PSP_SUPPORT_PTR = psp_support_ptr;
   return(MQX_OK);

} /* Endbody */

/* EOF */
