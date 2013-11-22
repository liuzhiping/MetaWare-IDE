#ifndef __lxt970a_h__
#define __lxt970a_h__
/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: lxt970a.h
***
*** Comments:
***   This include file defines the register structure, associated
***   constants and macros for the Vautomation VMAC Ethernet Controller.   
***   
**************************************************************************
*END*********************************************************************/

/* Control register bit definitions */
#define LXT970A_CTRL_RESET         0x8000
#define LXT970A_CTRL_LOOPBACK      0x4000
#define LXT970A_CTRL_SPEED         0x2000
#define LXT970A_CTRL_AUTONEG       0x1000
#define LXT970A_CTRL_DUPLEX        0x0100
#define LXT970A_CTRL_RESTART_AUTO  0x0200
#define LXT970A_CTRL_COLL          0x0080
#define LXT970A_CTRL_DUPLEX_MODE   0x0100

/* Status register bit definitions */
#define LXT970A_STATUS_COMPLETE     0x20          
#define LXT970A_STATUS_AUTONEG_ABLE 0x04
#define LXT970A_STATUS2_COMPLETE    0x80
          
/* Status2 register bit definitions */
#define LXT970A_STATUS2_FULL       0x200          
#define LXT970A_STATUS2_LINK_UP    0x400          
#define LXT970A_STATUS2_100        0x4000          

/* Auto-negatiation advertisement register bit definitions */
#define LXT970A_AUTONEG_ADV_100BTX_FULL     0x100       
#define LXT970A_AUTONEG_ADV_100BTX          0x80      
#define LXT970A_AUTONEG_ADV_10BTX_FULL      0x40  
#define LXT970A_AUTONEG_ADV_10BT            0x20       
#define AUTONEG_ADV_IEEE_8023               0x1

/* Auto-negatiation Link register bit definitions */
#define LXT970A_AUTONEG_LINK_100BTX_FULL     0x100       
#define LXT970A_AUTONEG_LINK_100BTX          0x80      
#define LXT970A_AUTONEG_LINK_10BTX_FULL      0x40  

/* LXT970 Registers */
#define LXT970A_CTRL_REG               0x00
#define LXT970A_STATUS_REG             0x01
#define LXT970A_AUTONEG_ADV_REG        0x4
#define LXT970A_AUTONEG_LINK_REG       0x5
#define LXT970A_MIRROR_REG             0x10
#define LXT970A_STATUS2_REG            0x11



/* Physical Control Register bits */
#define PHY_MDI               0x0001
#define PHY_MDO               0x0002
#define PHY_MDOE              0x0004
#define PHY_MDC               0x0008

/* Byte Enable */
#define PHY_BYTE3             0x08
#define PHY_BYTE2             0x04
#define PHY_BYTE1             0x02
#define PHY_BYTE0             0x01

#ifdef __MET__
#define NOP     \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop"); \
   _ASM("nop")
#else
   #error COMPILER NOT DEFINED
#endif

#endif



