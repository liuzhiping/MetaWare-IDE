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
*** File: pvuart.c
***
*** Comments:      
***   This file contains the functions for the polled serial I/O
***   low level functions for the VAutomation VUART.
***
***
`1**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "io_prv.h"
#include "charq.h"
#include "fio_prv.h"
#include "serplprv.h"

extern _mqx_uint _vuart_serial_polled_init(VUART_SERIAL_INIT_STRUCT_PTR, pointer _PTR_, char_ptr);
extern _mqx_uint _vuart_serial_polled_deinit(VUART_SERIAL_INIT_STRUCT_PTR, VUART_SERIAL_INFO_STRUCT_PTR );
extern char      _vuart_serial_getc(VUART_SERIAL_INFO_STRUCT_PTR);
extern void      _vuart_serial_putc(VUART_SERIAL_INFO_STRUCT_PTR, char);
extern boolean   _vuart_serial_status(VUART_SERIAL_INFO_STRUCT_PTR);
extern _mqx_uint _vuart_serial_ioctl(VUART_SERIAL_INFO_STRUCT_PTR, _mqx_uint, pointer);

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _vuart_uart_serial_polled_install
* Returned Value   : uint_32 a task error code or MQX_OK
* Comments         :
*    Install a polled uart serial device.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _vuart_serial_polled_install
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
      (_mqx_uint (_CODE_PTR_)(pointer, pointer _PTR_, char _PTR_))_vuart_serial_polled_init,
      (_mqx_uint (_CODE_PTR_)(pointer, pointer))_vuart_serial_polled_deinit,
      (char      (_CODE_PTR_)(pointer))_vuart_serial_getc,
      (void      (_CODE_PTR_)(pointer, char))_vuart_serial_putc,
      (boolean   (_CODE_PTR_)(pointer))_vuart_serial_status,
      (_mqx_uint (_CODE_PTR_)(pointer, _mqx_uint, pointer))_vuart_serial_ioctl,
      init_data_ptr,
      queue_size);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_polled_init  
* Returned Value   : MQX_OK or a MQX error code.
* Comments         :
*    This function initializes the uart
*
*END*********************************************************************/

_mqx_uint _vuart_serial_polled_init
   (
      /* [IN] the initialization information for the device being opened */
      VUART_SERIAL_INIT_STRUCT_PTR io_init_ptr,

      /* [OUT] the address to store device specific information */
      pointer _PTR_                io_info_ptr_ptr,
       
      /* [IN] the rest of the name of the device opened */
      char_ptr                     open_name_ptr
   )                       
{ /* Body */
   VUART_SERIAL_INFO_STRUCT_PTR        io_info_ptr;
   VUART_DEVICE_STRUCT_PTR             uart_ptr;
   uint_32                             div;

   io_info_ptr  = (VUART_SERIAL_INFO_STRUCT_PTR)_mem_alloc_system_zero(
      (_mem_size)sizeof(VUART_SERIAL_INFO_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (!io_info_ptr) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   uart_ptr = (VUART_DEVICE_STRUCT_PTR)io_init_ptr->DEVICE_ADDRESS;

   *io_info_ptr_ptr      = io_info_ptr;

   io_info_ptr->INIT        = *io_init_ptr;
   io_info_ptr->CLOCK_SPEED = BSP_SYSTEM_CLOCK;
   io_info_ptr->UART_PTR    = uart_ptr;

   /* Tell uart not to interrupt */
   _BSP_WRITE_VUART(&uart_ptr->STATUS,0);
   _BSP_IO_EIEIO;

   /* initialize baud rate */
   div = ((io_info_ptr->CLOCK_SPEED / (io_init_ptr->BAUD_RATE << 1) + 1) >> 1) 
      - 1;

   /* LSB of baud rate */
   _BSP_WRITE_VUART(&uart_ptr->BAUD_LOW, div & 0xFF);
   _BSP_IO_EIEIO;

   /* MSB of baud rate */
   _BSP_WRITE_VUART(&uart_ptr->BAUD_HIGH,(div >> 8) & 0xFF);
   _BSP_IO_EIEIO;
   
   return MQX_OK;

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_polled_deinit  
* Returned Value   : MQX_OK or a mqx error code.
* Comments         :
*    This function de-initializes the UART
*
*END*********************************************************************/

_mqx_uint _vuart_serial_polled_deinit
   (
      /* [IN] the initialization information for the device being opened */
      VUART_SERIAL_INIT_STRUCT_PTR io_init_ptr,

      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr
   )
{ /* Body */
          
   _mem_free(io_info_ptr);

   return(MQX_OK);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_getc
* Returned Value   : char
* Comments         : 
*   Return a character when it is available.  This function polls the 
* device for input.
*
*************************************************************************/

char _vuart_serial_getc
   (
      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr
   )
{ /* Body */
   VUART_DEVICE_STRUCT_PTR   uart_ptr;
   uint_32                   data;

   uart_ptr = io_info_ptr->UART_PTR;

   _BSP_READ_VUART(&uart_ptr->STATUS, data);
   _BSP_IO_EIEIO;
   while (data & VUART_STATUS_RX_FIFO_EMPTY) {
      _BSP_READ_VUART(&uart_ptr->STATUS,data);
      _BSP_IO_EIEIO;
   } /* Endwhile */
   _BSP_READ_VUART(&uart_ptr->TX_RX_DATA, data);

   io_info_ptr->RX_CHARS++;

   return (char)data;

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_putc
* Returned Value   : void
* Comments         : 
*   Writes the provided character.
*
*END*********************************************************************/

void _vuart_serial_putc
   (
      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr,

      /* [IN] the character to write */
      char                         c
   )
{ /* Body */
   VUART_DEVICE_STRUCT_PTR   uart_ptr;
   uint_32                   status;

   uart_ptr = io_info_ptr->UART_PTR;

   _BSP_READ_VUART(&uart_ptr->STATUS, status);
   _BSP_IO_EIEIO;
   while (!(status & VUART_STATUS_TX_EMPTY)) {
      _BSP_READ_VUART(&uart_ptr->STATUS, status);
      _BSP_IO_EIEIO;
   } /* Endwhile */
   _BSP_WRITE_VUART(&uart_ptr->TX_RX_DATA, (uint_32)c);

   io_info_ptr->TX_CHARS++;

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_status
* Returned Value   : boolean
* Comments         : 
*            This function returns TRUE if a character is available
*            on the on I/O device, otherwise it returns FALSE.
*
*END*********************************************************************/

boolean _vuart_serial_status
   (
      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr
   )
{ /* Body */
   VUART_DEVICE_STRUCT_PTR   uart_ptr;
   uint_32                   status;

   uart_ptr = io_info_ptr->UART_PTR;
   _BSP_READ_VUART(&uart_ptr->STATUS, status);
   _BSP_IO_EIEIO;
   if (status & VUART_STATUS_RX_FIFO_EMPTY) {
      return FALSE;
   } else {
      return TRUE;
   } /* Endif */

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_ioctl
* Returned Value   : uint_32 MQX_OK or a mqx error code.
* Comments         : 
*    This function performs miscellaneous services for
*    the I/O device.  
*
*END*********************************************************************/

_mqx_uint _vuart_serial_ioctl
   (
      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr,

      /* [IN] The command to perform */
      _mqx_uint                    cmd,

      /* [IN] Parameters for the command */
      pointer                      param
   )
{ /* Body */
   VUART_DEVICE_STRUCT_PTR   uart_ptr;
   uint_32_ptr               param_ptr = (uint_32_ptr)param;
   uint_32                   div;

   uart_ptr = io_info_ptr->UART_PTR;

   switch (cmd) {
      case IO_IOCTL_SERIAL_GET_BAUD:
         *param_ptr = io_info_ptr->INIT.BAUD_RATE;
         break;
      case IO_IOCTL_SERIAL_SET_BAUD:
         io_info_ptr->INIT.BAUD_RATE = *param_ptr;

         /* set baud rate */
         div = ((io_info_ptr->CLOCK_SPEED / (io_info_ptr->INIT.BAUD_RATE << 1) 
            + 1) >> 1) - 1;
         /* LSB of baud rate */
         _BSP_WRITE_VUART(&uart_ptr->BAUD_LOW, div & 0xFF);
         _BSP_IO_EIEIO;

         /* MSB of baud rate */
         _BSP_WRITE_VUART(&uart_ptr->BAUD_HIGH,(div >> 8) & 0xFF);
         _BSP_IO_EIEIO;
         break;
      case IO_IOCTL_SERIAL_GET_STATS:
         *param_ptr++ = io_info_ptr->RX_DROPPED_INPUT;
         *param_ptr++ = io_info_ptr->INTERRUPTS;
         *param_ptr++ = io_info_ptr->RX_GOOD_CHARS;
         *param_ptr++ = io_info_ptr->RX_CHARS;
         *param_ptr++ = io_info_ptr->TX_CHARS;
         *param_ptr++ = io_info_ptr->RX_PARITY_ERRORS;
         *param_ptr++ = io_info_ptr->RX_FRAMING_ERRORS;
         *param_ptr++ = io_info_ptr->RX_OVERRUNS;
         *param_ptr++ = io_info_ptr->RX_BREAKS;
         break;
      case IO_IOCTL_SERIAL_CLEAR_STATS:
         io_info_ptr->RX_DROPPED_INPUT  = 0;
         io_info_ptr->INTERRUPTS        = 0;
         io_info_ptr->RX_GOOD_CHARS     = 0;
         io_info_ptr->RX_CHARS          = 0;
         io_info_ptr->TX_CHARS          = 0;
         io_info_ptr->RX_PARITY_ERRORS  = 0;
         io_info_ptr->RX_FRAMING_ERRORS = 0;
         io_info_ptr->RX_OVERRUNS       = 0;
         io_info_ptr->RX_BREAKS         = 0;
         break;
      default:
         return(IO_ERROR_INVALID_IOCTL_CMD);
   } /* Endswitch */
   
   return MQX_OK;

} /* Endbody */

/* EOF */
