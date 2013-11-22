/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: int_idef.c
***
*** Comments:
***   This file contains the function for installing an application
*** provided default ISR, called when an unexpected interrupt occurs.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_install_default_isr
* Returned Value   : _CODE_PTR_ address or NULL on error
* Comments         :
*    This routine installs the provided function as the default ISR,
* called whenever an unhandled interrupt occurs.
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_install_default_isr
   ( 
      /* [IN] the new default ISR function */
      void (_CODE_PTR_ default_isr)(pointer)

   ))(pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   void           (_CODE_PTR_ old_default_isr)(pointer);
   INTERRUPT_TABLE_STRUCT_PTR int_table_ptr;
   _mqx_uint                    number;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_int_install_default_isr, default_isr);

   old_default_isr = kernel_data->DEFAULT_ISR;
   kernel_data->DEFAULT_ISR = default_isr;

   int_table_ptr = kernel_data->INTERRUPT_TABLE_PTR;
   if (int_table_ptr != NULL) {
      number = (kernel_data->LAST_USER_ISR_VECTOR -
         kernel_data->FIRST_USER_ISR_VECTOR) + 1 + 1;

      while (--number) {
         if (int_table_ptr->APP_ISR == old_default_isr) {
            int_table_ptr->APP_ISR = default_isr;
         } /* Endif */
         ++int_table_ptr;
      } /* Endwhile */
   } /* Endif */

   _KLOGX2(KLOG_int_install_default_isr, old_default_isr);

   return(old_default_isr);

} /* Endbody */

/* EOF */
