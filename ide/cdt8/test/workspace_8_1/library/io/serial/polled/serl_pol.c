/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: serl_pol.c
***
*** Comments:
***   This file contains the driver functions for the polled serial
*** asynchronous character I/O.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"
#include "serial.h"
#include "charq.h"
#include "serplprv.h"
#include "bsp.h"        /* BSP_SERIAL_POLLED_WRAPPER */
#include "bsp_prv.h"    /* BSP_SERIAL_POLLED_WRAPPER */

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _io_serial_polled_install
* Returned Value   : _mqx_uint a task error code or MQX_OK
* Comments         :
*    Install a polled serial device.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _io_serial_polled_install
   (
      /* [IN] A string that identifies the device for fopen */
      char_ptr             identifier,

      /* [IN] The I/O init function */
      _mqx_uint (_CODE_PTR_ init)(pointer, pointer _PTR_, char _PTR_),

      /* [IN] The I/O de-init function */
      _mqx_uint (_CODE_PTR_ deinit)(pointer, pointer),

      /* [IN] The input function */
      char     (_CODE_PTR_ getc)(pointer),

      /* [IN] The output function */
      void     (_CODE_PTR_ putc)(pointer, char),

      /* [IN] The char available (status) function */
      boolean  (_CODE_PTR_ status)(pointer),

      /* [IN] The I/O ioctl function */
      _mqx_uint (_CODE_PTR_ ioctl)(pointer, _mqx_uint, pointer),

      /* [IN] The I/O init data pointer */
      pointer              init_data_ptr,

      /* [IN] The I/O queue size to use */
      _mqx_uint             queue_size
   )
{ /* Body */
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR dev_ptr;

   dev_ptr = _mem_alloc_system_zero((_mem_size)
      sizeof(IO_SERIAL_POLLED_DEVICE_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (dev_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   dev_ptr->DEV_INIT          = init;
   dev_ptr->DEV_DEINIT        = deinit;
   dev_ptr->DEV_GETC          = getc;
   dev_ptr->DEV_PUTC          = putc;
   dev_ptr->DEV_STATUS        = status;
   dev_ptr->DEV_IOCTL         = ioctl;
   dev_ptr->DEV_INIT_DATA_PTR = init_data_ptr;
   dev_ptr->QUEUE_SIZE        = queue_size;

#ifdef BSP_SERIAL_POLLED_WRAPPER
   /*
   ** Allow the BSP to wrap the I/O functions with a composite
   ** object that delivers the characters to a debugger window
   */
   BSP_SERIAL_POLLED_WRAPPER(dev_ptr);
#endif

   return (_io_dev_install(identifier,
      _io_serial_polled_open, _io_serial_polled_close,
      _io_serial_polled_read, _io_serial_polled_write,
      _io_serial_polled_ioctl,
      (pointer)dev_ptr));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _io_serial_polled_uninstall
* Returned Value   : _mqx_int a task error code or MQX_OK
* Comments         :
*    Un-Install a polled serial device.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_serial_polled_uninstall
   (
      /* [IN] The IO device structure for the device */
      IO_DEVICE_STRUCT_PTR   io_dev_ptr
   )
{ /* Body */
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR polled_dev_ptr = io_dev_ptr->DRIVER_INIT_PTR;

   if (polled_dev_ptr->COUNT == 0) {
      if (polled_dev_ptr->DEV_DEINIT) {
         (*polled_dev_ptr->DEV_DEINIT)(polled_dev_ptr->DEV_INIT_DATA_PTR,
            polled_dev_ptr->DEV_INFO_PTR);
      } /* Endif */
      if (polled_dev_ptr->CHARQ) {
         _mem_free(polled_dev_ptr->CHARQ);
      } /* Endif */
      polled_dev_ptr->CHARQ = NULL;
      _mem_free(polled_dev_ptr);
      io_dev_ptr->DRIVER_INIT_PTR = NULL;
      return(IO_OK);
   } else {
      return(IO_ERROR_DEVICE_BUSY);
   } /* Endif */

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _io_serial_polled_open
* Returned Value   : _mqx_int error code
* Comments         :
*    This routine initializes a polled serial I/O channel. It acquires
*    memory, then stores information into it about the channel.
*    It then calls the hardware interface function to initialize the channel.
*
*END**********************************************************************/

_mqx_int _io_serial_polled_open
   (
      /* [IN] the file handle for the device being opened */
      FILE_PTR   fd_ptr,

      /* [IN] the remaining portion of the name of the device */
      char       _PTR_ open_name_ptr,

      /* [IN] the flags to be used during operation:
      ** echo, translation, xon/xoff, encoded into a pointer.
      */
      char       _PTR_ flags
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR               io_dev_ptr;
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR polled_dev_ptr;
   _mqx_uint                           result = MQX_OK;

   io_dev_ptr     = fd_ptr->DEV_PTR;
   polled_dev_ptr = (pointer)io_dev_ptr->DRIVER_INIT_PTR;

   if (polled_dev_ptr->COUNT) {
      /* Device is already opened */
      polled_dev_ptr->COUNT++;
      fd_ptr->FLAGS = polled_dev_ptr->FLAGS;
      return(result);
   } /* Endif */

   polled_dev_ptr->CHARQ = (CHARQ_STRUCT_PTR)_mem_alloc_system((_mem_size)(
      sizeof(CHARQ_STRUCT) - (4 * sizeof(char)) + polled_dev_ptr->QUEUE_SIZE));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (polled_dev_ptr->CHARQ == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   _CHARQ_INIT(polled_dev_ptr->CHARQ, polled_dev_ptr->QUEUE_SIZE);
   polled_dev_ptr->FLAGS = (_mqx_uint)flags;
   fd_ptr->FLAGS = (_mqx_uint)flags;

   result = (*polled_dev_ptr->DEV_INIT)(polled_dev_ptr->DEV_INIT_DATA_PTR,
      &polled_dev_ptr->DEV_INFO_PTR, open_name_ptr);
   if (result != MQX_OK) {
      _mem_free(polled_dev_ptr->CHARQ);
      return(result);
   } /* Endif */
   polled_dev_ptr->COUNT    = 1;

   return(result);

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _io_serial_polled_close
* Returned Value   : _mqx_int error code
* Comments         :
*    This routine closes the polled serial I/O channel.
*
*END**********************************************************************/

_mqx_int _io_serial_polled_close
   (
      /* [IN] the file handle for the device being closed */
      FILE_PTR fd_ptr
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR               io_dev_ptr;
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR polled_dev_ptr;
   _mqx_int                            result = MQX_OK;

   io_dev_ptr = fd_ptr->DEV_PTR;
   polled_dev_ptr = (pointer)io_dev_ptr->DRIVER_INIT_PTR;

   if (--polled_dev_ptr->COUNT == 0) {
      /* Device is ready to be closed */
      if (polled_dev_ptr->DEV_DEINIT) {
         result = (*polled_dev_ptr->DEV_DEINIT)(polled_dev_ptr->DEV_INIT_DATA_PTR,
            polled_dev_ptr->DEV_INFO_PTR);
      } /* Endif */
      _mem_free(polled_dev_ptr->CHARQ);
      polled_dev_ptr->CHARQ = NULL;
   } /* Endif */
   return(result);

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _io_serial_polled_read
* Returned Value   : _mqx_int number of characters read
* Comments         :
*    This routine reads characters from the serial I/O channel
*    device, converting carriage return ('\r') characters to newlines,
*    and then echoing the input characters.
*
*END*********************************************************************/

_mqx_int _io_serial_polled_read
   (
      /* [IN] the file handle for the device */
      FILE_PTR   fd_ptr,

      /* [IN] where the characters are to be stored */
      char_ptr   data_ptr,

      /* [IN] the number of characters to input */
      _mqx_int   num
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR               io_dev_ptr;
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR polled_dev_ptr;
   uchar                              character;
   _mqx_uint                          flags;
   _mqx_uint                          i = num;
   pointer                            info_ptr;

   io_dev_ptr     = fd_ptr->DEV_PTR;
   polled_dev_ptr = (pointer)io_dev_ptr->DRIVER_INIT_PTR;
   flags          = polled_dev_ptr->FLAGS;
   info_ptr       = polled_dev_ptr->DEV_INFO_PTR;

   while ( i ) {

      if (flags & IO_SERIAL_XON_XOFF) {
         while ((! _CHARQ_FULL(polled_dev_ptr->CHARQ)) &&
            (*polled_dev_ptr->DEV_STATUS)(info_ptr))
         {
            character = (*polled_dev_ptr->DEV_GETC)(info_ptr);
            if (character == CNTL_S) {
               while (character != CNTL_Q) {
                  character = (uchar)(*polled_dev_ptr->DEV_GETC)(info_ptr);
                  if (character != CNTL_Q) {
                     _CHARQ_ENQUEUE(polled_dev_ptr->CHARQ, character);
                  } /* Endif */
               } /* Endwhile */
            } else {
               _CHARQ_ENQUEUE(polled_dev_ptr->CHARQ, character);
            } /* Endif */
         } /* Endwhile */
      } else {
         while ((! _CHARQ_FULL(polled_dev_ptr->CHARQ)) &&
                (*polled_dev_ptr->DEV_STATUS)(info_ptr))
         {
            character = (uchar)(*polled_dev_ptr->DEV_GETC)(info_ptr);
            _CHARQ_ENQUEUE(polled_dev_ptr->CHARQ, character);
         } /* Endwhile */
      } /* Endif */

      if ( _CHARQ_SIZE(polled_dev_ptr->CHARQ) ) {
         _CHARQ_DEQUEUE(polled_dev_ptr->CHARQ, character);
         if (flags & IO_SERIAL_TRANSLATION) {
            if (character == '\r') {
               if (flags & IO_SERIAL_ECHO) {
                  (*polled_dev_ptr->DEV_PUTC)(info_ptr, (char)'\r');
               } /* Endif */
               character = '\n';
            } else if ( (character == '\b') && (flags & IO_SERIAL_ECHO) ) {
               (*polled_dev_ptr->DEV_PUTC)(info_ptr, (char)'\b');
               (*polled_dev_ptr->DEV_PUTC)(info_ptr, (char)' ');
            } /* Endif */
         } /* Endif */
         if (flags & IO_SERIAL_ECHO) {
            (*polled_dev_ptr->DEV_PUTC)(info_ptr, (char)character);
         } /* Endif */
         *data_ptr++ = character;
         --i;
      } /* Endif */

   } /* Endwhile */
   return num;

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _io_serial_polled_write
* Returned Value   : _mqx_int number of characters written
* Comments         :
*    This routine writes characters to the serial I/O channel.
*    It monitors for input of ^S and ^Q (XON, XOFF protocol)
*    It also converts the C '\n' into '\n\r'.
*
*END**********************************************************************/

_mqx_int _io_serial_polled_write
   (
      /* [IN] the file handle for the device */
      FILE_PTR  fd_ptr,

      /* [IN] where the characters are */
      char_ptr  data_ptr,

      /* [IN] the number of characters to output */
      _mqx_int  num
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR               io_dev_ptr;
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR polled_dev_ptr;
   uchar                              character;
   _mqx_uint                           flags;
   _mqx_uint                           i = num + 1;
   pointer                            info_ptr;

   io_dev_ptr     = fd_ptr->DEV_PTR;
   polled_dev_ptr = (pointer)io_dev_ptr->DRIVER_INIT_PTR;
   flags          = polled_dev_ptr->FLAGS;
   info_ptr       = polled_dev_ptr->DEV_INFO_PTR;

   while (--i) {

      if (flags & IO_SERIAL_XON_XOFF) {
         while ((! _CHARQ_FULL(polled_dev_ptr->CHARQ)) &&
                (*polled_dev_ptr->DEV_STATUS)(info_ptr))
         {
            character = (uchar)(*polled_dev_ptr->DEV_GETC)(info_ptr);
            if (character == CNTL_S) {
               while ( character != CNTL_Q ) {
                  character = (uchar)(*polled_dev_ptr->DEV_GETC)(info_ptr);
                  if (character != CNTL_Q) {
                     _CHARQ_ENQUEUE(polled_dev_ptr->CHARQ, character);
                  } /* Endif */
               } /* Endwhile */
            } else {
               _CHARQ_ENQUEUE(polled_dev_ptr->CHARQ,character);
            } /* Endif */
         } /* Endwhile */
      } /* Endif */

      if (flags & IO_SERIAL_TRANSLATION) {
         if (*data_ptr == '\n') {
            (*polled_dev_ptr->DEV_PUTC)(info_ptr, '\r');
         } /* Endif */
      } /* Endif */
      (*polled_dev_ptr->DEV_PUTC)(info_ptr, *data_ptr++);

   } /* Endwhile */
   return num;

} /* Endbody */


/*FUNCTION*****************************************************************
*
* Function Name    : _io_serial_polled_ioctl
* Returned Value   : _mqx_int
* Comments         :
*    Returns result of ioctl operation.
*
*END*********************************************************************/

_mqx_int _io_serial_polled_ioctl
   (
      /* [IN] the file handle for the device */
      FILE_PTR       fd_ptr,

      /* [IN] the ioctl command */
      _mqx_uint      cmd,

      /* [IN] the ioctl parameters */
      pointer        param_ptr
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR               io_dev_ptr;
   IO_SERIAL_POLLED_DEVICE_STRUCT_PTR polled_dev_ptr;
   _mqx_uint                          result = MQX_OK;
   boolean _PTR_                      bparam_ptr;
   _mqx_uint_ptr                      pparam_ptr;

   io_dev_ptr     = fd_ptr->DEV_PTR;
   polled_dev_ptr = (pointer)io_dev_ptr->DRIVER_INIT_PTR;

   switch (cmd) {
      case IO_IOCTL_CHAR_AVAIL:
         bparam_ptr = (boolean _PTR_)param_ptr;
         if ( _CHARQ_SIZE(polled_dev_ptr->CHARQ) ) {
           *bparam_ptr = TRUE;
         } else {
           *bparam_ptr = (*polled_dev_ptr->DEV_STATUS)(
              polled_dev_ptr->DEV_INFO_PTR);
         } /* Endif */
         break;

      case IO_IOCTL_SERIAL_GET_FLAGS:
         pparam_ptr  = (_mqx_uint_ptr)param_ptr;
         *pparam_ptr = polled_dev_ptr->FLAGS;
         break;

      case IO_IOCTL_SERIAL_SET_FLAGS:
         pparam_ptr  = (_mqx_uint_ptr)param_ptr;
         polled_dev_ptr->FLAGS = *pparam_ptr;
         fd_ptr->FLAGS = *pparam_ptr;
         break;

      case IO_IOCTL_UNINSTALL:

      default:
         if (polled_dev_ptr->DEV_IOCTL != NULL) {
            result = (*polled_dev_ptr->DEV_IOCTL)(polled_dev_ptr->DEV_INFO_PTR,
               cmd, param_ptr);
         } /* Endif */
         break;
   } /* Endswitch */
   return result;

} /* Endbody */

/* EOF */
