#ifndef __psp_prv_h__
#define __psp_prv_h__
/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: psp_prv.h
***            
*** Comments:      
***   This file contains psp private declarations for use when compiling
***   the kernel.
***
**************************************************************************
*END*********************************************************************/

/* This macro modifies the context of a blocked task so that the
** task will execute the provided function when it next runs
*/  
#define _PSP_SET_PC_OF_BLOCKED_TASK(stack_ptr, func) \
   ((PSP_BLOCKED_STACK_STRUCT_PTR)(stack_ptr))->RETURN_ADDRESS = (uint_32)(func)

/* This macro modifies the interrupt priority of a blocked task so that the
** task will execute at the correct interrupt priority when it restarts
*/
#define _PSP_SET_SR_OF_BLOCKED_TASK(stack_ptr, sr_value) \
   ((PSP_BLOCKED_STACK_STRUCT_PTR)(stack_ptr))->FLAGS = \
   (((PSP_BLOCKED_STACK_STRUCT_PTR)(stack_ptr))->FLAGS & 0xF00 ) + (sr_value & 0xFF)

/* This macro modifies the context of a task that has been interrupted 
** so that the task will execute the provided function when the isr returns
*/
#define _PSP_SET_PC_OF_INTERRUPTED_TASK(stack_ptr, func) \
   _PSP_SET_PC_OF_BLOCKED_TASK(stack_ptr, func)

/* Calculate the address of the td extension found on the stack */
#define _PSP_GET_TD_EXTENSION(td_ptr) \
   ((uchar _PTR_)((td_ptr)->STACK_BASE) - sizeof(PSP_STACK_START_STRUCT) + \
    FIELD_OFFSET(PSP_STACK_START_STRUCT, TD_EXTENSION))

/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF PSP FUNCTIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

extern void    _psp_int_install(void);
extern void    _int_kernel_vector_0(void);
extern void    _int_kernel_vector_1(void);
extern void    _int_verbose_unexpected_isr(pointer parameter);
extern void    (_CODE_PTR_ _int_install_verbose_unexpected_isr(void))(pointer);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
