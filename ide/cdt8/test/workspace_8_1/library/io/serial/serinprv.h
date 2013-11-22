#ifndef __serinprv_h__
#define __serinprv_h__
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
*** File: serinprv.h
***
*** Comments:      
***   This file includes the private definitions for the interrupt
*** driven serial I/O drivers.
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/

/*
** Xon/Xoff protocol characters
*/
#define CNTL_S   ((char) 0x13)  /* Control S == XOFF.   */
#define CNTL_Q   ((char) 0x11)  /* Control Q == XON.    */

/*--------------------------------------------------------------------------*/
/*
**                            DATATYPE DECLARATIONS
*/

/*---------------------------------------------------------------------
**
** IO SERIAL INT DEVICE STRUCT
**
** This structure used to store information about a interrupt serial io device
** for the IO device table
*/
typedef struct io_serial_int_device_struct
{

   /* The I/O init function */
   _mqx_uint (_CODE_PTR_ DEV_INIT)(pointer, char _PTR_);

   /* The enable interrupts function */
   _mqx_uint (_CODE_PTR_ DEV_ENABLE_INTS)(pointer);

   /* The I/O deinit function */
   _mqx_uint (_CODE_PTR_ DEV_DEINIT)(pointer, pointer);

   /* The output function, used to write out the first character */
   void    (_CODE_PTR_ DEV_PUTC)(pointer, char);

   /* The ioctl function, (change bauds etc) */
   _mqx_uint (_CODE_PTR_ DEV_IOCTL)(pointer, _mqx_uint, pointer);

   /* The I/O channel initialization data */
   pointer             DEV_INIT_DATA_PTR;
   
   /* Device specific information */
   pointer             DEV_INFO_PTR;

   /* The queue size to use */
   _mqx_uint             QUEUE_SIZE;
   
   /* Open count for number of accessing file descriptors */
   _mqx_uint             COUNT;

   /* Open flags for this channel */
   _mqx_uint             FLAGS;

   /* The input queue */
   CHARQ_STRUCT_PTR    IN_QUEUE;

   /* The input waiting tasks */
   pointer             IN_WAITING_TASKS;

   /* The output queue */
   CHARQ_STRUCT_PTR    OUT_QUEUE;

   /* The output waiting tasks */
   pointer             OUT_WAITING_TASKS;

   /* Has output been started */
   boolean             OUTPUT_ENABLED;

   /* Protocol flag information */
   _mqx_uint             HAVE_STOPPED_OUTPUT;
   _mqx_uint             HAVE_STOPPED_INPUT;
   _mqx_uint             MUST_STOP_INPUT;
   _mqx_uint             MUST_START_INPUT;
   _mqx_uint             INPUT_HIGH_WATER_MARK;
   _mqx_uint             INPUT_LOW_WATER_MARK;

} IO_SERIAL_INT_DEVICE_STRUCT, _PTR_ IO_SERIAL_INT_DEVICE_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                            FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

/* Interrupt I/O prototypes */
extern _mqx_int  _io_serial_int_open(FILE_DEVICE_STRUCT_PTR, char _PTR_, 
   char _PTR_);
extern _mqx_int  _io_serial_int_close(FILE_DEVICE_STRUCT_PTR);
extern _mqx_int  _io_serial_int_read(FILE_DEVICE_STRUCT_PTR, char _PTR_, _mqx_int);
extern _mqx_int  _io_serial_int_write(FILE_DEVICE_STRUCT_PTR, char _PTR_, _mqx_int);
extern _mqx_int  _io_serial_int_ioctl(FILE_DEVICE_STRUCT_PTR, _mqx_uint, 
   pointer);
extern _mqx_int _io_serial_int_uninstall(IO_DEVICE_STRUCT_PTR);

/* Callback Functions called by lower level interrupt I/O interrupt handlers */
extern boolean _io_serial_int_addc(IO_SERIAL_INT_DEVICE_STRUCT_PTR, char);
extern _mqx_int  _io_serial_int_nextc(IO_SERIAL_INT_DEVICE_STRUCT_PTR);

/* Internal helper functions */
extern void    _io_serial_int_putc_internal(IO_SERIAL_INT_DEVICE_STRUCT_PTR, 
   char);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
