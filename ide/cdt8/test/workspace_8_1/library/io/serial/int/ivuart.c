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
*** File: ivuart.c
***
*** Comments:      
***   This file contains the low level functions for the interrupt driven
*** serial I/O for the VAutomation VUART.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "io_prv.h"
#include "charq.h"
#include "fio_prv.h"
#include "serinprv.h"

extern _mqx_uint _vuart_serial_int_init(IO_SERIAL_INT_DEVICE_STRUCT_PTR, char_ptr);
extern _mqx_uint _vuart_serial_int_deinit(VUART_SERIAL_INIT_STRUCT_PTR,
   VUART_SERIAL_INFO_STRUCT_PTR);
extern _mqx_uint _vuart_serial_int_enable(VUART_SERIAL_INFO_STRUCT_PTR);
extern void _vuart_serial_int_isr(pointer);
extern void _vuart_serial_int_putc(IO_SERIAL_INT_DEVICE_STRUCT_PTR, char);
extern _mqx_uint _vuart_serial_ioctl(VUART_SERIAL_INFO_STRUCT_PTR,_mqx_uint,
  pointer);
extern _mqx_uint _vuart_serial_polled_init(
   VUART_SERIAL_INIT_STRUCT_PTR, pointer _PTR_, char_ptr);
extern _mqx_uint _vuart_serial_polled_deinit(
   VUART_SERIAL_INIT_STRUCT_PTR, VUART_SERIAL_INFO_STRUCT_PTR );


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _vuart_serial_int_install
* Returned Value   : uint_32 a task error code or MQX_OK
* Comments         :
*    Install an interrupt driven serial device.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _vuart_serial_int_install
   (   
      /* [IN] A string that identifies the device for fopen */
      char_ptr   identifier,
  
      /* [IN] The I/O init data pointer */
      pointer    init_data_ptr,
      
      /* [IN] The I/O queue size to use */
      _mqx_uint  queue_size
   )
{ /* Body */

   return _io_serial_int_install(identifier,
      (_mqx_uint (_CODE_PTR_)(pointer, char _PTR_))_vuart_serial_int_init, 
      (_mqx_uint (_CODE_PTR_)(pointer))_vuart_serial_int_enable, 
      (_mqx_uint (_CODE_PTR_)(pointer,pointer))_vuart_serial_int_deinit, 
      (void    (_CODE_PTR_)(pointer, char))_vuart_serial_int_putc, 
      (_mqx_uint (_CODE_PTR_)(pointer, _mqx_uint, pointer))_vuart_serial_ioctl, 
      init_data_ptr,
      queue_size);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_int_init
* Returned Value   : uint_32 a task error code or MQX_OK
* Comments         :
*    This function initializes the UART in interrupt mode.
*
*END*********************************************************************/

_mqx_uint _vuart_serial_int_init
   (
      /* [IN] the interrupt I/O initialization information */
      IO_SERIAL_INT_DEVICE_STRUCT_PTR int_io_dev_ptr,
       
      /* [IN] the rest of the name of the device opened */
      char                      _PTR_ open_name_ptr
   )
{ /* Body */
   VUART_SERIAL_INIT_STRUCT_PTR io_init_ptr;
   VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr;
   _mqx_uint                    result;

   io_init_ptr = int_io_dev_ptr->DEV_INIT_DATA_PTR;

   result = _vuart_serial_polled_init((pointer)io_init_ptr, 
      &int_io_dev_ptr->DEV_INFO_PTR, open_name_ptr);
   if (result != MQX_OK) {
      return result;
   } /* Endif */

   io_info_ptr = int_io_dev_ptr->DEV_INFO_PTR;

   io_info_ptr->VECTOR  = io_init_ptr->VECTOR;

   /* Setup the interrupt */
   io_info_ptr->OLD_ISR_DATA = _int_get_isr_data(io_info_ptr->VECTOR);
   io_info_ptr->OLD_ISR_EXCEPTION_HANDLER = 
      _int_get_exception_handler(io_info_ptr->VECTOR);
   
   io_info_ptr->OLD_ISR = 
      _int_install_isr(io_info_ptr->VECTOR, _vuart_serial_int_isr, 
      int_io_dev_ptr);

   io_info_ptr->VECTOR  = io_init_ptr->VECTOR;

   return MQX_OK;

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_int_deinit
* Returned Value   : uint_32 a task error code or MQX_OK
* Comments         :
*    This function de-initializes the UART in interrupt mode.
*
*END*********************************************************************/

_mqx_uint _vuart_serial_int_deinit
   (
      /* [IN] the interrupt I/O initialization information */
      VUART_SERIAL_INIT_STRUCT_PTR io_init_ptr,
       
      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr
   )
{ /* Body */
   VUART_DEVICE_STRUCT_PTR  uart_ptr;

   uart_ptr  = io_info_ptr->UART_PTR;

   /* Tell uart not to interrupt */
   _BSP_WRITE_VUART(&uart_ptr->STATUS, 0);

   /* Restore old vector */
   _int_install_isr(io_init_ptr->VECTOR, io_info_ptr->OLD_ISR, 
      io_info_ptr->OLD_ISR_DATA);

   _vuart_serial_polled_deinit(io_init_ptr, io_info_ptr);

   return(MQX_OK);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_int_enable
* Returned Value   : uint_32 a task error code or MQX_OK
* Comments         :
*    This function enables the UART interrupts mode.
*
*END*********************************************************************/

_mqx_uint _vuart_serial_int_enable
   (
      /* [IN] the address of the device specific information */
      VUART_SERIAL_INFO_STRUCT_PTR io_info_ptr
   )
{ /* Body */
   VUART_DEVICE_STRUCT_PTR  uart_ptr;
   uint_32                  tmp;

   uart_ptr  = io_info_ptr->UART_PTR;

   /* Set the level of the interrupt */
   _psp_set_int_level(io_info_ptr->VECTOR, io_info_ptr->INIT.LEVEL);

   /* Enable receive interrupts */
   _BSP_READ_VUART(&uart_ptr->STATUS, tmp);

   tmp |= VUART_STATUS_RX_INT_ENABLE;
   _BSP_WRITE_VUART(&uart_ptr->STATUS, tmp);

   return MQX_OK;

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_int_isr
* Returned Value   : none
* Comments         : 
*   interrupt handler for the serial I/O interrupts.
*
*************************************************************************/

void _vuart_serial_int_isr
   (
      /* [IN] the address of the device specific information */
      pointer parameter
   )
{ /* Body */
   IO_SERIAL_INT_DEVICE_STRUCT_PTR      int_io_dev_ptr = parameter;
   VUART_SERIAL_INFO_STRUCT_PTR         io_info_ptr;
   VUART_DEVICE_STRUCT_PTR              uart_ptr;
   boolean                              work;
   int_32                               c;
   uint_32                              status, ch;

   io_info_ptr = int_io_dev_ptr->DEV_INFO_PTR;
   uart_ptr    = io_info_ptr->UART_PTR;
   ++io_info_ptr->INTERRUPTS;

   /* Perform start of interrupt handling */

   work = TRUE;
   while (work) {
      _BSP_READ_VUART(&uart_ptr->STATUS,status);
   
      work = FALSE;

      while (!(status & VUART_STATUS_RX_FIFO_EMPTY)) {
         work = TRUE;
         _BSP_READ_VUART(&uart_ptr->TX_RX_DATA,ch);
         ++io_info_ptr->RX_CHARS;
         c = ch;

         if (status & VUART_RX_ERROR) {
            if (status & VUART_STATUS_RX_FRAME_ERROR) {
               ++io_info_ptr->RX_FRAMING_ERRORS;
            } /* Endif */
            if (status & VUART_STATUS_RX_OVERFLOW_ERROR) {
               ++io_info_ptr->RX_OVERRUNS;
            } /* Endif */
         } else {
            ++io_info_ptr->RX_GOOD_CHARS;
         } /* Endif */

         if (!_io_serial_int_addc(int_io_dev_ptr, (char)c)) {
            io_info_ptr->RX_DROPPED_INPUT++;
         } /* Endif */

         _BSP_READ_VUART(&uart_ptr->STATUS,status);
      } /* Endwhile */

      c = 0;
      if (status & VUART_STATUS_TX_EMPTY) {
         c = _io_serial_int_nextc(int_io_dev_ptr);
         if (c != (-1)) {
            work = TRUE;
            ch = c;
            _BSP_WRITE_VUART(&uart_ptr->TX_RX_DATA,ch);
            ++io_info_ptr->TX_CHARS;
         } /* Endif */
      } /* Endif */

   } /* Endwhile */

   if ( c != (-1) ) {
      status = VUART_STATUS_TX_INT_ENABLE | VUART_STATUS_RX_INT_ENABLE;
   } else {
      status = VUART_STATUS_RX_INT_ENABLE;
   } /* Endif */

   _BSP_WRITE_VUART(&uart_ptr->STATUS, status);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _vuart_serial_int_putc
* Returned Value   : none
* Comments         :
*   This function is called to write out the first character, when
* the output serial device and output ring buffers are empty.
*
*END*********************************************************************/

void _vuart_serial_int_putc
   (
      /* [IN] the address of the device specific information */
      IO_SERIAL_INT_DEVICE_STRUCT_PTR   int_io_dev_ptr,

      /* [IN] the character to write out now */
      char                              c
   )
{ /* Body */
   VUART_SERIAL_INFO_STRUCT_PTR    io_info_ptr;
   VUART_DEVICE_STRUCT_PTR         uart_ptr;
   uint_32                         status;

   io_info_ptr = int_io_dev_ptr->DEV_INFO_PTR;
   uart_ptr    = io_info_ptr->UART_PTR;

   _BSP_READ_VUART(&uart_ptr->STATUS, status);

   while (!(status & VUART_STATUS_TX_EMPTY)) {
      _BSP_READ_VUART(&uart_ptr->STATUS, status);
   } /* Endwhile */

   _BSP_WRITE_VUART(&uart_ptr->TX_RX_DATA, (unsigned)c);

   status = VUART_STATUS_TX_INT_ENABLE | VUART_STATUS_RX_INT_ENABLE;
   _BSP_WRITE_VUART(&uart_ptr->STATUS, status);

   ++io_info_ptr->TX_CHARS;

} /* Endbody */

/* EOF */
