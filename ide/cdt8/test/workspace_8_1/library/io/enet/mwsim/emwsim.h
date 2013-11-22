#ifndef __emwsim_h__
#define __emwsim_h__ 1
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
*** File: emwsim.h
***
*** Comments:      
***   This file contains the definitions of constants and structures
***   required for the ethernet drivers for the metaware simulator
***   ethernet device.
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                    CONSTANT DEFINITIONS
*/

#define EMWSIM_ENET_INIT       (0)
#define EMWSIM_ENET_INIT_RTS   (1)

#define EMWSIM_DEFAULT_INT_LEVEL  (2)
#define EMWSIM_DEFAULT_INT_VECTOR (6)

/*-------------------------------------------------------------------------*/
/* Bit definitions for control field of the ethernet device */

/* Write this bit to request initialization */
#define EMWSIM_CONTROL_INITIALIZE    (0x0001)

/*
** Write this bit to enable (1) or disable(0) the device interrupts
** The device interrupts on a read or write completion.
** The interrupt is automatically cleared
*/
#define EMWSIM_CONTROL_INT_ENABLE    (0x0002)

/* Write this bit to deinstall the device */
#define EMWSIM_CONTROL_SHUTDOWN      (0x0004)

/* Write this bit to tell the device to transmit */
#define EMWSIM_CONTROL_TRANSMIT      (0x0008)

/* Write this bit to reset the RX Available bit in the status register */
#define EMWSIM_CONTROL_RECEIVED      (0x0010)

/* Write this bit when read to read a packet */
#define EMWSIM_CONTROL_READY_TO_RX   (0x0020)


/*-------------------------------------------------------------------------*/
/* Bit definitions for status field of the ethernet device */

/* The device is initialized */
#define EMWSIM_STATUS_INITIALIZED   (0x0001)

/* The device is able to transmit */
#define EMWSIM_STATUS_TX_AVAILABLE  (0x0002)

/* The device has received a packet */
#define EMWSIM_STATUS_RX_RECEIVED   (0x0004)

/*-------------------------------------------------------------------------*/
/* Data type declarations */


/*
** EMWSIM STRUCT
** This structure defines what the registers of the simulator ethernet
** device look like.
*/
typedef struct emwsim_struct {

   /* The control register */
   uint_32 CONTROL;

   /* The status register */
   uint_32 STATUS;

   /*
   ** If STATUS & TX_AVAILABLE, and you write CONTROL_TRANSMIT to the
   ** CONTROL register, the device will transmit the packed pointed to
   ** by TX_BUFFER for TX_SIZE.  During transmission, TX_AVAILABLE is
   ** is NOT set in STATUS; at completion of transmission, it is set,
   ** indicating we can transmit again.
   ** Upon completion of transmission, if CONTROL & INTERRUPT_ENABLE, 
   ** an interrupt is attempted.
   */
   uint_32 TX_SIZE;
   pointer TX_BUFFER;

   /*
   ** If CONTROL & RX_READY, the device is allowed to receive
   ** an ethernet packet.  It writes the packet to the address in RX_BUFFER
   ** puts its length in RX_SIZE, and sets RX_AVAILABLE in the STATUS
   ** register.  If CONTROL & INTERRUPT_ENABLE, an interrupt 
   ** is attempted.
   ** If RX_BUFFER is null, the packet is discarded, and no other changes
   ** are made.
   */
   uint_32 RX_SIZE;
   pointer RX_BUFFER;

   /*
   ** This is the interrupt configuration of the device.
   ** The upper 16 bits is the level (1 or 2)
   ** and the lower 16 bits is the vector.
   ** The default for the device is level 2, vector 6.
   ** This is set up when the device is configured and does not change.
   ** The device driver can read this register to determine how to
   ** catch the interrupt.
   */
   uint_32 INTERRUPT_CONFIGURATION;

} EMWSIM_STRUCT, _PTR_ EMWSIM_STRUCT_PTR;

#endif
/* EOF */
