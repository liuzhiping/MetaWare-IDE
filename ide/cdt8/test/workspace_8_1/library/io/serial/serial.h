#ifndef __serial_h__
#define __serial_h__ 1
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
*** File: serial.h
***
*** Comments:      
***   This include file is used to provide information needed by
***   applications using the serial I/O functions.
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/

/* Incoming and outgoing data not processed */
#define IO_SERIAL_RAW_IO             (0)

/* Perform xon/xoff processing */
#define IO_SERIAL_XON_XOFF           (1)

/*
** Perform translation :
**    outgoing \n to CR\LF
**    incoming CR to \n
**    incoming backspace erases previous character
*/
#define IO_SERIAL_TRANSLATION        (2)

/* echo incoming characters */
#define IO_SERIAL_ECHO               (4)

/* Perform hardware flow control processing */
#define IO_SERIAL_HW_FLOW_CONTROL    (8)

/* Serial I/O IOCTL commands */
#define IO_IOCTL_SERIAL_GET_FLAGS        (0x0101)
#define IO_IOCTL_SERIAL_SET_FLAGS        (0x0102)
#define IO_IOCTL_SERIAL_GET_BAUD         (0x0103)
#define IO_IOCTL_SERIAL_SET_BAUD         (0x0104)
#define IO_IOCTL_SERIAL_GET_STATS        (0x0105)
#define IO_IOCTL_SERIAL_CLEAR_STATS      (0x0106)
#define IO_IOCTL_SERIAL_TRANSMIT_DONE    (0x0107)
#define IO_IOCTL_SERIAL_GET_CONFIG       (0x0108)

#define IO_IOCTL_SERIAL_GET_HW_SIGNAL    (0x0109)
#define IO_IOCTL_SERIAL_SET_HW_SIGNAL    (0x010A)
#define IO_IOCTL_SERIAL_CLEAR_HW_SIGNAL  (0x010B)
/* Standard HW signal names used with GET/SET/CLEAR HW SIGNAL */
#define IO_SERIAL_CTS                    (1)
#define IO_SERIAL_RTS                    (2)
#define IO_SERIAL_DTR                    (4)
#define IO_SERIAL_DSR                    (8)
#define IO_SERIAL_DCD                    (0x10)
#define IO_SERIAL_RI                     (0x20)

#define IO_IOCTL_SERIAL_SET_DATA_BITS    (0x010C)
#define IO_IOCTL_SERIAL_GET_DATA_BITS    (0x010D)
/* Value used with SET DATA BITS is just the integer number of bits */

#define IO_IOCTL_SERIAL_SET_STOP_BITS    (0x010E)
#define IO_IOCTL_SERIAL_GET_STOP_BITS    (0x010F)
/* Standard names used with SET STOP BITS */
#define IO_SERIAL_STOP_BITS_1            (1)
#define IO_SERIAL_STOP_BITS_1_5          (2)
#define IO_SERIAL_STOP_BITS_2            (3)

#define IO_IOCTL_SERIAL_SET_PARITY       (0x0110)
#define IO_IOCTL_SERIAL_GET_PARITY       (0x0111)
/* Standard parity names used with SET PARITY */
#define IO_SERIAL_PARITY_NONE            (1)
#define IO_SERIAL_PARITY_ODD             (2)
#define IO_SERIAL_PARITY_EVEN            (3)
#define IO_SERIAL_PARITY_FORCE           (4)
#define IO_SERIAL_PARITY_MARK            (5)
#define IO_SERIAL_PARITY_SPACE           (6)

#define IO_IOCTL_SERIAL_START_BREAK      (0x0112)
#define IO_IOCTL_SERIAL_STOP_BREAK       (0x0113)
#define IO_IOCTL_SERIAL_TX_DRAINED       (0x0114)


/*--------------------------------------------------------------------------*/
/*
**                      FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

extern void    _io_serial_default_init(void);

extern _mqx_uint _io_serial_polled_install(
      char_ptr, 
      _mqx_uint (_CODE_PTR_)(pointer, pointer _PTR_, char _PTR_),
      _mqx_uint (_CODE_PTR_)(pointer, pointer),
      char    (_CODE_PTR_)(pointer),
      void    (_CODE_PTR_)(pointer, char),
      boolean (_CODE_PTR_)(pointer),
      _mqx_uint (_CODE_PTR_)(pointer, _mqx_uint, pointer),
      pointer, _mqx_uint);

extern _mqx_uint _io_serial_int_install(
      char_ptr, 
      _mqx_uint (_CODE_PTR_)(pointer, char _PTR_),
      _mqx_uint (_CODE_PTR_)(pointer),
      _mqx_uint (_CODE_PTR_)(pointer, pointer),
      void     (_CODE_PTR_)(pointer, char),
      _mqx_uint (_CODE_PTR_)(pointer, _mqx_uint, pointer),
      pointer, _mqx_uint);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
