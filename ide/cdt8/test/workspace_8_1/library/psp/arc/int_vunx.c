/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: int_vunx.c
***
*** Comments:
***   This file contains the function for the unexpected
*** exception handling function for MQX, which will display on the
*** console what exception has ocurred.  
***  NOTE: the default I/O for the current task is used, since a printf
***  is being done from an ISR.  
***  This default I/O must NOT be an interrupt drive I/O channel.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "fio.h"

/* Start CR 1930 */
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_verbose_unexpected_isr
* Returned Value   : void
* Comments         :
*    A default handler for all unhandled interrupts.
*    It determines the type of interrupt or exception and prints out info.
*    Because printing out strings trashes the scratch registers, 
*    kernel_data->FLAGS must have MQX_FLAGS_EXCEPTION_HANDLER_INSTALLED set 
*    in order to use this ISR safely.
*
*END*----------------------------------------------------------------------*/

void _int_verbose_unexpected_isr
   (
      /* [IN] the parameter passed to the default ISR, the vector */
      pointer parameter
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR         kernel_data;
   PSP_BLOCKED_STACK_STRUCT_PTR   blocked_stack_ptr;
   TD_STRUCT_PTR                  td_ptr;
   uint_32                        vector;

   _GET_KERNEL_DATA(kernel_data);
   td_ptr      = kernel_data->ACTIVE_PTR;

   blocked_stack_ptr = (pointer)td_ptr->STACK_PTR;

   vector = (uint_32)parameter;

   printf( "\n*** UNHANDLED INTERRUPT ***\n"); 
   printf( "Vector #: 0x%02lx Task Id: 0x%0lx Td_ptr 0x%lx Stack Frame: 0x%lx\n\r",
      vector, td_ptr->TASK_ID, td_ptr, blocked_stack_ptr);

   printf( "Interrupt_nesting level: %ld   FLAGS: 0x%04lx\n\r",
      kernel_data->IN_ISR, blocked_stack_ptr->FLAGS);

#if MQX_CPU == 0xACA7
   printf( "PC: %08x   ILINK1: %08x   BTA: %08x\n\r",
      blocked_stack_ptr->RETURN_ADDRESS,
      blocked_stack_ptr->ILINK1,
      blocked_stack_ptr->BTA);
#else
   printf( "PC: %08x   ILINK1: %08x\n\r",
      blocked_stack_ptr->RETURN_ADDRESS,
      blocked_stack_ptr->ILINK1);
#endif

   _int_disable();
   if (td_ptr->STATE != UNHANDLED_INT_BLOCKED) {
      td_ptr->STATE = UNHANDLED_INT_BLOCKED;
      td_ptr->INFO  = (_mqx_uint)parameter;

      _QUEUE_UNLINK(td_ptr);
   } /* Endif */
   _int_enable();

} /* Endbody */

/* End CR 1930 */
/* EOF */
