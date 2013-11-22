#ifndef __vuart_h__
#define __vuart_h__ 1
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
*** File: vuart.h
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

/* VUART Status register bit definitions */

#define VUART_STATUS_RX_FRAME_ERROR      (0x01)
#define VUART_STATUS_RX_OVERFLOW_ERROR   (0x02)
#define VUART_STATUS_RX_INT_ENABLE       (0x04)
#define VUART_STATUS_RX_FIFIO_FULL       (0x08)
#define VUART_STATUS_RX_FIFIO_NEAR_FULL  (0x10)
#define VUART_STATUS_RX_FIFO_EMPTY       (0x20)
#define VUART_STATUS_TX_INT_ENABLE       (0x40)
#define VUART_STATUS_TX_EMPTY            (0x80)

/* Helpful status bit combinations */
#define VUART_RX_ERROR \
   (VUART_STATUS_RX_FRAME_ERROR | VUART_STATUS_RX_OVERFLOW_ERROR)


/*-----------------------------------------------------------------------*/
/*
**                      TYPE DEFINITIONS
*/

/*
** What the VUART registers look like in memory
*/
typedef volatile struct vuart_device_struct
{
#ifdef BSP_NON_BVCI_VUART
   /* ID register */
   uint_32 ID[1];
   /* 
   ** Read = Receive data register
   ** Write = Trasnmit data register
   */
   uint_32 TX_RX_DATA;
   uint_32 STATUS;                             /* Status Register  */
   uint_32 BAUD_LOW;                           /* Lower 8 bits of baud rate    */
   uint_32 BAUD_HIGH;                          /* Upper 8 bits of baud rate    */
#else   
   /* ID register */
   uint_32 ID[4];
   /* 
   ** Read = Receive data register
   ** Write = Trasnmit data register
   */
   uint_32 TX_RX_DATA;
   uint_32 STATUS;                             /* Status Register  */
   uint_32 BAUD_LOW;                           /* Lower 8 bits of baud rate    */
   uint_32 BAUD_HIGH;                          /* Upper 8 bits of baud rate    */
#endif
} VUART_DEVICE_STRUCT, _PTR_ VUART_DEVICE_STRUCT_PTR;

#endif
/* EOF */
