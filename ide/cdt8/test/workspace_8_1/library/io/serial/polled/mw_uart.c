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
*** File: mw_uart.c
***
*** Comments:
***   This file contains the functions for the polled serial I/O
*** low level functions for the simulated i/o device.
***
*** $Header:mw_uart.c, 10, 2/12/2004 4:46:51 PM, $
***
**************************************************************************
$Log:
 10   mqx 2.50  1.9         2/12/2004 4:46:51 PM   Mark Schimmel   Added code
      to allow hostlink writes when in an ISR.  The _hl_write routine tries to
      wait on a lightweight semaphore which is not allowed in an ISR.
 9    mqx 2.50  1.8         1/26/2004 5:07:53 PM   Mark Schimmel   copyright
      2004
 8    mqx 2.50  1.7         5/22/2003 1:58:41 PM   Behdad Besharat 
 7    mqx 2.50  1.6         5/14/2003 11:04:27 AM  Mark Schimmel   added
      prototype for _hl_read
 6    mqx 2.50  1.5         5/2/2003 2:09:50 PM    Mark Schimmel   PowerPC now
      supported
 5    mqx 2.50  1.4         3/7/2003 4:47:21 PM    Mark Schimmel   changed
      copyright to 2003
 4    mqx 2.50  1.3         2/21/2002 10:17:09 AM  mati sauks      
 3    mqx 2.50  1.2         2/15/2002 7:52:37 AM   jeremy kalman   
 2    mqx 2.50  1.1         1/23/2002 8:37:43 AM   mati sauks      
 1    mqx 2.50  1.0         3/22/2001 11:40:52 AM  mati sauks      
$

$NoKeywords$
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"
#include "fio_prv.h"
#include "io_prv.h"
#include "charq.h"
#include "serplprv.h"
#include "mw_uart.h"

/* Private function prototypes */
extern _mqx_uint _mw_simuart_serial_polled_init(MW_SIMUART_INIT_STRUCT_PTR, pointer _PTR_,
   char _PTR_);
extern _mqx_uint _mw_simuart_serial_polled_deinit(MW_SIMUART_INIT_STRUCT_PTR, pointer);
extern char      _mw_simuart_serial_polled_getc(MW_SIMUART_INIT_STRUCT_PTR);
extern void      _mw_simuart_serial_polled_putc(MW_SIMUART_INIT_STRUCT_PTR, char);
extern boolean   _mw_simuart_serial_polled_status(MW_SIMUART_INIT_STRUCT_PTR);
extern _mqx_uint _mw_simuart_serial_polled_ioctl(MW_SIMUART_INIT_STRUCT_PTR, _mqx_uint,
   pointer);

#ifdef BSP_DEFAULT_HOSTLINK     /* CR1449: del reference to __mwwrite_buf */
extern int _hl_write(int, const char _PTR_, unsigned int);
extern int _hl_read(int, char _PTR_, unsigned int);
#define WRITECHARS(fd,buf,len)  _hl_write(fd, buf, len)
#define READCHARS(fd,buf,len)   _hl_read(fd, buf, len)
#else
extern int _write(int, const char _PTR_, unsigned int);
extern int _read(int, char _PTR_, unsigned int);
#define WRITECHARS(fd,buf,len)  _write(fd, buf, len)
#define READCHARS(fd,buf,len)   _read(fd, buf, len)
#endif

#undef  MAX_BUFFER_SIZE
#define MAX_BUFFER_SIZE 16
uchar   _mw_simuart_buffer[MAX_BUFFER_SIZE+1];
uint_32 _mw_simuart_index;

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _mw_simuart_serial_polled_install
* Returned Value   : uint_32 a task error code or MQX_OK
* Comments         :
*    Install a polled simulated uart serial device.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mw_simuart_serial_polled_install
   (
      /* [IN] A string that identifies the device for fopen */
      char_ptr           identifier,

      /* [IN] The I/O init data pointer */
      pointer            init_data_ptr,

      /* [IN] The I/O queue size to use */
      _mqx_uint          queue_size
   )
{ /* Body */

   return _io_serial_polled_install(identifier,
      (_mqx_uint (_CODE_PTR_)(pointer, pointer _PTR_, char _PTR_))_mw_simuart_serial_polled_init,
      (_mqx_uint (_CODE_PTR_)(pointer, pointer))_mw_simuart_serial_polled_deinit,
      (char      (_CODE_PTR_)(pointer))_mw_simuart_serial_polled_getc,
      (void      (_CODE_PTR_)(pointer, char))_mw_simuart_serial_polled_putc,
      (boolean   (_CODE_PTR_)(pointer))_mw_simuart_serial_polled_status,
      (_mqx_uint (_CODE_PTR_)(pointer, _mqx_uint, pointer))_mw_simuart_serial_polled_ioctl,
      init_data_ptr,
      queue_size);

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_init
* Returned Value   : none
* Comments         :
*    This function initializes the selected I/O device.
*
*END*********************************************************************/

_mqx_uint _mw_simuart_serial_polled_init
   (
      /* [IN] the device initialization information */
      MW_SIMUART_INIT_STRUCT_PTR mw_simuart_serial_polled_init_ptr,

      /* [OUT] the address to store device specific information */
      pointer _PTR_         mw_simuart_info_ptr_ptr,

      /* [IN] the rest of the name of the device opened */
      char_ptr              open_name_ptr
   )
{ /* Body */

   _mw_simuart_index = 0;
   *mw_simuart_info_ptr_ptr = (pointer)mw_simuart_serial_polled_init_ptr;

   return MQX_OK;

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_deinit
* Returned Value   : none
* Comments         :
*    This function turns off the driver.
*
*END*********************************************************************/

_mqx_uint _mw_simuart_serial_polled_deinit
   (
      /* [IN] the device initialization information */
      MW_SIMUART_INIT_STRUCT_PTR mw_simuart_serial_polled_init_ptr,

      /* [IN] the address of the device specific information */
      pointer               mw_simuart_info_ptr
   )
{ /* Body */

   _mw_simuart_index = 0;
   return MQX_OK;

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_getc
* Returned Value   : char
* Comments         :
*   Return a character when it is available.  This function polls the
* device for input.
*
*************************************************************************/

#undef getchar
extern int getchar(void);

char _mw_simuart_serial_polled_getc
   (
      /* [IN] the information for the device */
      MW_SIMUART_INIT_STRUCT_PTR mw_simuart_serial_polled_init_ptr
   )
{ /* Body */

   char c;

   return (READCHARS(0, &c, 1) == -1) ? 0 : c;

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_putc
* Returned Value   : void
* Comments         :
*   Writes the provided character out.
*
*END*********************************************************************/

#undef write
extern int write(int handle, char *bufptr, unsigned int count);

static void hostlink_write
   (
      char *b,
      unsigned int len
   )
{ /* Body */

#if MQX_CHECK_ERRORS && defined(BSP_DEFAULT_HOSTLINK)
   KERNEL_DATA_STRUCT_PTR kernel_data;
   uint_16                in_isr;

   _GET_KERNEL_DATA(kernel_data);
   _INT_DISABLE();

   //marks@2/11/04 UNSAFE, but work around to get printf output
   //              while in an interrupt service routine.
   in_isr = kernel_data->IN_ISR;
   kernel_data->IN_ISR = 0;
#endif

   WRITECHARS(1, b, len);

#if MQX_CHECK_ERRORS && defined(BSP_DEFAULT_HOSTLINK)
   kernel_data->IN_ISR = in_isr;
   _INT_ENABLE();
#endif

} /* Endbody */

void _mw_simuart_serial_polled_putc
   (
      /* [IN] the information for the device */
      MW_SIMUART_INIT_STRUCT_PTR mw_simuart_serial_polled_init_ptr,

      /* [IN] the character to write */
      char                  c
   )
{ /* Body */

   _mw_simuart_buffer[_mw_simuart_index++] = c;
   if ((c == '\n') || (_mw_simuart_index == MAX_BUFFER_SIZE)) {
      hostlink_write((char *)&_mw_simuart_buffer[0], _mw_simuart_index);
      _mw_simuart_index = 0;
   } /* Endif */

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_output_check
* Returned Value   : void
* Comments         :
*   Writes the buffered output to the host, meant to be called from timer ISR
*
*END*********************************************************************/

void _mw_simuart_serial_polled_output_check
   (
      void
   )
{ /* Body */

   if (_mw_simuart_index) {
      hostlink_write((char *)&_mw_simuart_buffer[0], _mw_simuart_index);
      _mw_simuart_index = 0;
   } /* Endif */

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_status
* Returned Value   : boolean
* Comments         :
*            This function returns TRUE if a character is available
*            on the on I/O device, otherwise it returns FALSE.
*
*END*********************************************************************/

boolean _mw_simuart_serial_polled_status
   (
      /* [IN] the information for the device */
      MW_SIMUART_INIT_STRUCT_PTR mw_simuart_serial_polled_init_ptr
   )
{ /* Body */
   static result = FALSE;

#if !_PPC // PowerPC traps for every hostlink operation
   result = ! result;
#endif

   return result;

} /* Endbody */


/*FUNCTION****************************************************************
*
* Function Name    : _mw_simuart_serial_polled_ioctl
* Returned Value   : boolean
* Comments         :
*    This function performs miscellaneous services for
*    the I/O device.
*
*END*********************************************************************/

_mqx_uint _mw_simuart_serial_polled_ioctl
   (
      /* [IN] the information for the device */
      MW_SIMUART_INIT_STRUCT_PTR mw_simuart_serial_polled_init_ptr,

      /* [IN] The command to perform */
      _mqx_uint                  cmd,

      /* [IN] pointer to the parameters for the command */
      pointer                    param_ptr
   )
{ /* Body */
   _mqx_uint result;

   switch (cmd) {
      case IO_IOCTL_FLUSH_OUTPUT:
         _mw_simuart_serial_polled_output_check();
         result = MQX_OK;
         break;
      default:
         result = IO_ERROR_INVALID_IOCTL_CMD;
         break;
   } /* Endswitch */
   return result;

} /* Endbody */

/* EOF */
