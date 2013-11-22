#ifndef __serplprv_h__
#define __serplprv_h__
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
*** File: serplprv.h
***
*** Comments:      
***   This file includes the private definitions for the polled serial I/O
*** drivers.
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
** IO SERIAL POLLED DEVICE STRUCT
**
** This structure used to store information about a polled serial io device
** for the IO device table.
*/
typedef struct io_serial_polled_device_struct
{
 
   /* The I/O init function */
   _mqx_uint (_CODE_PTR_ DEV_INIT)(pointer, pointer _PTR_, char _PTR_);

   /* The I/O deinit function */
   _mqx_uint (_CODE_PTR_ DEV_DEINIT)(pointer, pointer);

   /* The input function */
   char    (_CODE_PTR_ DEV_GETC)(pointer);

   /* The output function */
   void    (_CODE_PTR_ DEV_PUTC)(pointer, char);

   /* The status function, (character available) */
   boolean (_CODE_PTR_ DEV_STATUS)(pointer);

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

   /* The Character Queue Ring buffer, for input buffering */
   CHARQ_STRUCT_PTR    CHARQ;

} IO_SERIAL_POLLED_DEVICE_STRUCT, _PTR_ IO_SERIAL_POLLED_DEVICE_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
**                            FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

/*
** Polled I/O prototypes
*/
extern _mqx_int _io_serial_polled_open(FILE_PTR, char _PTR_, char _PTR_);
extern _mqx_int _io_serial_polled_close(FILE_PTR);
extern _mqx_int _io_serial_polled_read(FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_serial_polled_write(FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_serial_polled_ioctl(FILE_PTR, _mqx_uint, pointer);
extern _mqx_int _io_serial_polled_uninstall(IO_DEVICE_STRUCT_PTR);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
