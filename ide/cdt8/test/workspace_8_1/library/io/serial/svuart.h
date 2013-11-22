#ifndef __svuart_h__
#define __svuart_h__ 1
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
*** File: svuart.h
***
*** Comments:      
***   This include file is used to provide information needed by
***   an application program using a VAutomation UART.
***
**************************************************************************
*END*********************************************************************/



/*-----------------------------------------------------------------------*/
/*
**                      CONSTANT DEFINITIONS
*/

/*----------------------------------------------------------------------*/
/* Some CPUs are capable of reordered execution. It is important that
** this doesn't happen with the 16554 code. A macro maybe defined in 
** the BSP that prevent reordering. If one isn't present define the 
** macro to do nothing
*/
#ifndef _BSP_IO_EIEIO
#define _BSP_IO_EIEIO
#endif

#ifndef _BSP_IO_ISYNC
#define _BSP_IO_ISYNC
#endif

#ifndef _BSP_IO_SYNC
#define _BSP_IO_SYNC
#endif


/*----------------------------------------------------------------------*/
/* 
** Macros to read and write registers
*/

#ifndef _BSP_READ_VUART
#define _BSP_READ_VUART(addr,y) (y) = *(addr) & 0xFF
#endif

#ifndef _BSP_WRITE_VUART
#define _BSP_WRITE_VUART(addr,y) *(addr) = (y) & 0xFF
#endif

/*----------------------------------------------------------------------*/
/*
** VUART SERIAL INIT STRUCT
** 
** This structure defines what the UART initialization record contains
*/
typedef struct vuart_serial_init_struct
{
   /* The device address */
   pointer   DEVICE_ADDRESS;

   /* 
   ** The serial I/O queue size to use to buffer incoming and outgoing
   ** data.
   */
   _mqx_uint QUEUE_SIZE;

   /* The baud rate for the channel */
   uint_32   BAUD_RATE;

   /* The vector number */
   _mqx_uint VECTOR;

   /* The vector level */
   _mqx_uint LEVEL;

} VUART_SERIAL_INIT_STRUCT, _PTR_ VUART_SERIAL_INIT_STRUCT_PTR;


/*
** VUART_SERIAL_INFO_STRUCT
** Run time state information for the serial channel
*/
typedef struct vuart_serial_info_struct
{
   /* The address of the 16652 uart registers */
   VUART_DEVICE_STRUCT_PTR            UART_PTR;
   
   /* The vector number which the uart will interrupt on */
   _mqx_uint                          VECTOR;

   /* Current initialized values */
   VUART_SERIAL_INIT_STRUCT           INIT;

   /* Frequency of the clock for the VUART */
   uint_32                            CLOCK_SPEED;
      
   /* The previous interrupt handler and data for the UART */
   void                 (_CODE_PTR_   OLD_ISR)(pointer);
   void                 (_CODE_PTR_   OLD_ISR_EXCEPTION_HANDLER)(_mqx_uint, _mqx_uint,
      pointer, pointer);
   pointer                            OLD_ISR_DATA;

   /* Statistical information */
   _mqx_uint                          INTERRUPTS;
   _mqx_uint                          RX_DROPPED_INPUT;
   _mqx_uint                          RX_GOOD_CHARS;
   _mqx_uint                          RX_CHARS;
   _mqx_uint                          TX_CHARS;
   _mqx_uint                          RX_PARITY_ERRORS;
   _mqx_uint                          RX_FRAMING_ERRORS;
   _mqx_uint                          RX_OVERRUNS;
   _mqx_uint                          RX_BREAKS;

} VUART_SERIAL_INFO_STRUCT, _PTR_ VUART_SERIAL_INFO_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                        FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

/* Polled driver functions */
extern _mqx_uint _vuart_serial_polled_install(char_ptr, pointer, _mqx_uint);

/* Interrupt driver functions */
extern _mqx_uint _vuart_serial_int_install(char_ptr, pointer, _mqx_uint);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
