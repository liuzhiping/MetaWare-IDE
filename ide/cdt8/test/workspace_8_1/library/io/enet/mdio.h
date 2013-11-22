#ifndef __mdio_h__
#define __mdio_h__
/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2005 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: mdio.h
***
*** Comments:
***   This file is generic to all Ethernet MACs connected via an MDIO
***   compliant phy. It contains register addresses and bit definitions
***   for those registers.
***   
**************************************************************************
*END*********************************************************************/

/* Common registers */
#define MDIO_CTRL_REG                          (0)
#define MDIO_STATUS1_REG                       (1)
#define MDIO_PHYID1_REG                        (2)
#define MDIO_PHYID2_REG                        (3)
#define MDIO_AUTONEG_ADV_REG                   (4)
#define MDIO_AUTONEG_LINK_REG                  (5)
#define MDIO_AUTONEG_EXP_REG                   (6)
#define MDIO_AUTONEG_NEXT_PAGE_TX_REG          (7)
#define MDIO_AUTONEG_LINK_RX_NEXT_PAGE_REG     (8)
#define MDIO_MSTR_SLV_CTRL_REG                 (9)
#define MDIO_MSTR_SLV_STATUS_REG               (10)

/* Reg 0: Control register bit defines */
#define MDIO_CTRL_RESET         (0x8000)
#define MDIO_CTRL_LOOPBACK      (0x4000)
#define MDIO_CTRL_SPEED_1       (0x2000)
#define MDIO_CTRL_AUTONEG       (0x1000)
#define MDIO_CTRL_PWR_DOWN      (0x0800)
#define MDIO_CTRL_ISOLATE       (0x0400)
#define MDIO_CTRL_RESTART_AUTO  (0x0200)
#define MDIO_CTRL_FULL_DUPLEX   (0x0100)
#define MDIO_CTRL_COLL          (0x0080)
#define MDIO_CTRL_SPEED_2       (0x0040)
#define MDIO_CTRL_RESERVED      (0x003F)

/* Reg 1: Status 1 register bit defines */
#define MDIO_STAT1_100BASE_T4           (0x8000)
#define MDIO_STAT1_100BASE_X_FULL_DUP   (0x4000)
#define MDIO_STAT1_100BASE_X_HALF_DUP   (0x2000)
#define MDIO_STAT1_10_FULL_DUP          (0x1000)
#define MDIO_STAT1_10_HALF_DUP          (0x0800)
#define MDIO_STAT1_100BASE_T2_FULL_DUP  (0x0400)
#define MDIO_STAT1_100BASE_T2_HALF_DUP  (0x0200)
#define MDIO_STAT1_EXT_STATUS           (0x0100)
#define MDIO_STAT1_SUP_MF_PREAMBLE      (0x0040)
#define MDIO_STAT1_AUTONEG_COMPLETE     (0x0020)
#define MDIO_STAT1_REMOTE_FAULT         (0x0010)
#define MDIO_STAT1_AUTONEG_ABILITY      (0x0008)
#define MDIO_STAT1_LINK_UP              (0x0004)
#define MDIO_STAT1_JABBER_DET           (0x0002)
#define MDIO_STAT1_EXT_ABILITY          (0x0001)

/* Reg 4: Autonegotiation advertisement bit defines */
#define MDIO_AUTO_ADV_NEXT_PAGE         (0x8000)
#define MDIO_AUTO_ADV_REMOTE_FAULT      (0x2000)
#define MDIO_AUTO_ADV_ASYM_PAUSE        (0x0800)
#define MDIO_AUTO_ADV_PAUSE             (0x0400)
#define MDIO_AUTO_ADV_100BASE_T4        (0x0200)
#define MDIO_AUTO_ADV_100BASE_TX_FULL   (0x0100)
#define MDIO_AUTO_ADV_100BASE_TX        (0x0080)
#define MDIO_AUTO_ADV_10BASE_T_FULL     (0x0040)
#define MDIO_AUTO_ADV_10BASE_T          (0x0020)

#define MDIO_AUTO_ADV_SELECTOR_802_3            (0x01)
#define MDIO_AUTO_ADV_SELECTOR_802_9_ISLAN_16T  (0x02)
#define MDIO_AUTO_ADV_SELECTOR_802_5            (0x03)

/* Reg 5: Link partner capabilities bit defines */
#define MDIO_AUTO_LNK_NEXT_PAGE         (0x8000)
#define MDIO_AUTO_LNK_ACK               (0x4000)
#define MDIO_AUTO_LNK_REMOTE_FAULT      (0x2000)
#define MDIO_AUTO_LNK_ASYM_PAUSE        (0x0800)
#define MDIO_AUTO_LNK_PAUSE             (0x0400)
#define MDIO_AUTO_LNK_100BASE_T4        (0x0200)
#define MDIO_AUTO_LNK_100BASE_TX_FULL   (0x0100)
#define MDIO_AUTO_LNK_100BASE_TX        (0x0080)
#define MDIO_AUTO_LNK_10BASE_T_FULL     (0x0040)
#define MDIO_AUTO_LNK_10BASE_T          (0x0020)

#endif



