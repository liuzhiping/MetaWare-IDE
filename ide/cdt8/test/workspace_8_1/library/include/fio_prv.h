#ifndef __fio_prv_h__
#define __fio_prv_h__
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
*** File: fio_prv.h
***
*** Comments:      
***   This file includes the private definitions for the formatted I/O .
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**  Compiler Dependencies
**
**  Most compilers have adequate modf and strtod functions
*/
#ifdef NEED_MODF
#define  modf       _io_modf
#endif
#ifdef NEED_STRTOD
#define  strtod     _io_strtod
#endif

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/

/* 
** Type definitions also used for sizing by doprint 
** They are the maximum string size that a 32 bit number 
** can be displayed as. 
*/

#define PRINT_OCTAL   (11L)
#define PRINT_DECIMAL (10L)
#define PRINT_HEX     (8L)
#define PRINT_ADDRESS (8L)

/* Type definitions use in the control of scanline */

#define SCAN_ERROR    (-1)
#define SCAN_LLONG    (0)
#define SCAN_WLONG    (1)
#define SCAN_BLONG    (2)
#define SCAN_MLONG    (3)


/*--------------------------------------------------------------------------*/
/*
**                            FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_int _io_doprint(FILE_PTR, _mqx_int (_CODE_PTR_)(_mqx_int, FILE_PTR),
   char _PTR_, va_list);
extern _mqx_int _io_sputc(_mqx_int, FILE_PTR);
extern _mqx_int _io_scanline(char _PTR_, char _PTR_, va_list);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
