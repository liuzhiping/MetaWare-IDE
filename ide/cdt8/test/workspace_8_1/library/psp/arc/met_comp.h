#ifndef __met_comp_h__
#define __met_comp_h__
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
*** File: met_comp.h
***            
*** Comments:      
***   This file defines the Metaware compiler specific macros for MQX
***
**************************************************************************
*END*********************************************************************/

/* This macro obtains the address of the kernel data structure */
#define _GET_KERNEL_DATA(x) x = (KERNEL_DATA_STRUCT_PTR)_mqx_kernel_data

#define _SET_KERNEL_DATA(x) \
   _mqx_kernel_data = (struct kernel_data_struct _PTR_)(x)

#define _PSP_SET_DISABLE_SR _PSP_SET_SR
#define _PSP_SET_ENABLE_SR  _PSP_SET_SR

#define _PSP_SET_SR(x)  _flag((unsigned)(x))

#define _PSP_GET_SR(x)  x = _lr(0xA);

#define _PSP_SET_FP(x) _core_write((unsigned)(x),27);

#define _PSP_SET_SP(x) _core_write((unsigned)(x),28);

#define _PSP_GOTO(x) __PSP_GOTO((void (*)(void))x)
_Asm void __PSP_GOTO(void (*x)(void))  {
   %reg x;
      j [x]
}   

/* START CR 2363 */
#define _PSP_SET_FP_SP_AND_GO(fpreg, spreg, addr) \
   __PSP_SET_FP_SP_AND_GO((void *)(fpreg), (void *)(spreg), (addr))

_Asm void __PSP_SET_FP_SP_AND_GO(void *fpreg, void *spreg, void (*func)(void)) {
/* END CR 2363 */
   %reg fpreg, spreg, func;
      mov %fp,fpreg
      mov %sp,spreg
      j [func]
}

#define _PSP_GET_CALLING_PC() _core_read(31);

#endif
/* EOF */
