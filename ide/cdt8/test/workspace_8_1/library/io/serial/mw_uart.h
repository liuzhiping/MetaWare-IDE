#ifndef __mw_uart_h__
#define __mw_uart_h__ 1
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
*** File: mw_uart.h
***
*** Comments:      
***   This include file is used to provide information needed by
***   an application program using the simulator I/O for the 
***   Metaware debugger
***                                                               
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                        CONSTANT DEFINTIONS
*/


/*--------------------------------------------------------------------------*/
/*
**                        DATATYPE DEFINTIONS
*/


/*
** MW SIMUART INIT STRUCT
** 
** This structure defines what the UART initialization record contains
*/
typedef struct mw_simuart_init_struct
{
   /* The serial I/O queue size to use to buffer incoming and outgoing
   ** data.
   */
   _mqx_uint QUEUE_SIZE;
   
} MW_SIMUART_INIT_STRUCT, _PTR_ MW_SIMUART_INIT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                        FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_uint _mw_simuart_serial_polled_install(char_ptr, pointer, _mqx_uint);
extern void      _mw_simuart_serial_polled_output_check(void);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
