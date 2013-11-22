#ifndef __vmac_h__
#define __vmac_h__
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
*** File: vmac.h
***
*** Comments:
***   This include file defines the register structure, associated
***   constants and macros for the VMAC Ethernet Controller.   
***   
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        
**                            CONSTANT DEFINITIONS
*/

/* Constants used for polling task  creation */
#define VMAC_POLL_TASK_NAME                "ENET_poll_task"
#define VMAC_POLL_TASK_STACK_SIZE          (400)
#define VMAC_SIZE_CONST                    (~0x7ff107FF)

/* Autonegatiation falg */
#define ENET_AUTONEGOTIATE             (0x1)

/* Polling flag */
#define ENET_POLL_PEER_PERIODICALLY    (0x2)   


/* Interrupt Status Register Bit Masks */
#define VMAC_ISR_TXINT           0x00000001     /* Tx interrupt pending */
#define VMAC_ISR_RXINT           0x00000002     /* Rx interrupt pending */
#define VMAC_ISR_ERR             0x00000004     /* Error interrupt pending */
#define VMAC_ISR_BUFFERR         0x00000008     /* Error in Tx buffer */
#define VMAC_ISR_MISSERR         0x00000010     /* Missed pkt counter rolled over */
#define VMAC_ISR_SQE             0x00000020     /* SQE error */
#define VMAC_ISR_RXCRC           0x00000100     /* RXCRC counter rolled over */
#define VMAC_ISR_RXFRAME         0x00000200     /* RX frame counter rolled over */
#define VMAC_ISR_RXOFLOW         0x00000400     /* RX overflow counter rolled over */
#define VMAC_ISR_MDIO            0x00001000     /* MDIO complete */
#define VMAC_ISR_TXPOLL          0x80000000     /* Poll Tx descriptors */

/* Interrupt Enable Register Masks */
/* Same masks as ISR */

/* Control Register Bit Masks */
#define VMAC_CTRL_ENABLE         0x00000001     /* Disable/Enable ethernet */
#define VMAC_CTRL_TXRUN          0x00000008     /* Disable/Enable Tx */
#define VMAC_CTRL_RXRUN          0x00000010     /* Disable/Enable Rx */
#define VMAC_CTRL_RXINT2TX       0x00000020     /* Rx interrupt activates the Tx interrupt */
#define VMAC_CTRL_DISBDCST       0x00000100     /* Disable/Enable Rx broadcasts */
#define VMAC_CTRL_DISMCAST       0x00000200     /* Disable/Enable Rx multicasts */
#define VMAC_CTRL_ENBFULL        0x00000400     /* Disable/Enable full duplex */
#define VMAC_CTRL_PROM           0x00000800     /* Disable/Enable promiscuous mode */
#define VMAC_CTRL_ENB2PART       0x00001000     /* Disable/Enable 2 part deferral */
#define VMAC_CTRL_TEST           0x00002000     /* Silicon testing */
#define VMAC_CTRL_DISRETRY       0x00004000     /* Disable/Enable Tx retries */
#define VMAC_CTRL_DISADDFCS      0x00008000     /* Disable/Enable FCS appending */
#define VMAC_CTRL_TXBDTLEN       0x00FF0000     /* No. of BDTs in Tx ring 1-255 */
#define VMAC_CTRL_RXBDTLEN       0xFF000000     /* No. of BDTs in Rx ring 1-255 */

/* Poll Rate Register Bit Mask */
#define VMAC_POLLRATE_PR         0x0000FFFF     /* frequency of polling */

/* Rx Error Register Bit Masks */
#define VMAC_RXERR_RXCRC         0x000000FF     /* CRC errors count */
#define VMAC_RXERR_RXFRAM        0x0000FF00     /* Frame errors count */
#define VMAC_RXERR_RXOFLOW       0x00FF0000     /* Overflow errors count */

/* Missed Packet Register Bit Mask */
#define VMAC_MISS_MISSED_CTR     0x000000FF     /* Missed pkt counter */

/* Tx Ring Pointer Address Register Bit Mask */
#define VMAC_TX_RING_PTR         0xFFFFFFFF     /* Address of start of Tx RING of BDTs */

/* Rx Ring Pointer Address Register Bit Mask */
#define VMAC_RX_RING_PTR         0xFFFFFFFF     /* Address of start of Rx RING of BDTs */

/* Ethernet MAC Address Register Bit Masks */
#define VMAC_ENET_ADDRL          0xFFFFFFFF     /* Lower bits for 48-bit ethernet MAC address */
#define VMAC_ENET_ADDRH          0x0000FFFF     /* Higher bits for 48-bit ethernet MAC address */

/* Logical Address Filter Register Bit Masks */
#define VMAC_ENET_LADDRL         0xFFFFFFFF     /* Lower bits of Logical Address */
#define VMAC_ENET_LADDRH         0x0000FFFF     /* Higher bits Logical Address */

/* Physical Interface Control Register Bit Masks */
#define VMAC_PICR_MDI            0x00000001     /* MDI Input value of the MDIO pin */
#define VMAC_PICR_MDO            0x00000002     /* MDO Output value of the MDIO pin */
#define VMAC_PICR_MDOE           0x00000004     /* MDOE Tristate Enable for MDIO */
#define VMAC_MDC_CLKO            0x00000008     /* MDC Closck o/p */

/* Tx Control Information Written By the CPU Bit Masks */
#define VMAC_TX_CTL_INFO_CPU_TX_LEN 0x000007FF    /* Tx Length in this buffer to be Xmitted */
#define VMAC_TX_CTL_INFO_CPU_FIRST  0x00010000    /* First Tx buffer in the packet */
#define VMAC_TX_CTL_INFO_CPU_LAST   0x00020000    /* Last Tx buffer in the packet */
#define VMAC_TX_CTL_INFO_CPU_ADDCRC 0x00040000    /* Add the CRC tp the pkt */
#define VMAC_TX_CTL_INFO_CPU_OWN    0x80000000    /* CPU/VMAC Ownership of buffer */

/* Tx Control Information Written By the VMAC Bit Masks */
#define VMAC_TX_CTL_INFO_TX_LEN     0x000007FF    /* Tx Length in this buffer to be Xmitted */
#define VMAC_TX_CTL_INFO_FIRST      0x00010000    /* First Tx buffer in the packet */
#define VMAC_TX_CTL_INFO_LAST       0x00020000    /* Last Tx buffer in the packet */
#define VMAC_TX_CTL_INFO_ADDCRC     0x00040000    /* Add the CRC to the pkt that is transmitted */
#define VMAC_TX_CTL_INFO_CARR_LOSS  0x00200000    /* Carrier Lost during xmission */
#define VMAC_TX_CTL_INFO_DEFER      0x00400000    /* xmission deferred due to traffic */
#define VMAC_TX_CTL_INFO_DROPPED    0x00800000    /* pkt dropped after 16 retries */
#define VMAC_TX_CTL_INFO_RETRY      0x0F000000    /* Retry count for Tx */
#define VMAC_TX_CTL_INFO_LATE_COLL  0x10000000    /* Late Collision */
#define VMAC_TX_CTL_INFO_UFLO       0x20000000    /* Data not available on time */
#define VMAC_TX_CTL_INFO_BUFF       0x40000000    /* Buffer error - bad FIRST and LAST */
#define VMAC_TX_CTL_INFO_OWN        0x80000000    /* CPU/VMAC Ownership of buffer */

/* Pointer To Tx Buffer Bit Mask */
#define VMAC_TX_BUFFER_PTR          0xFFFFFFFF    /* Physical address of the start of the buffer of data */

/* Rx Control Information Written By the VMAC Bit Masks */
#define VMAC_RX_CTL_INFO_RX_LEN     0x000007FF    /* Rx Length in this buffer to be Xmitted */
#define VMAC_RX_CTL_INFO_FIRST      0x00010000    /* First Rx buffer in the packet */
#define VMAC_RX_CTL_INFO_LAST       0x00020000    /* Last Rx buffer in the packet */
#define VMAC_RX_CTL_INFO_OWN        0x80000000    /* CPU/VMAC Ownership of buffer */

/* Rx Control Information Written By the CPU Bit Masks */
#define VMAC_RX_CTL_INFO_CPU_RX_LEN 0x000007FF    /* Rx Length in this buffer to be Xmitted */
#define VMAC_RX_CTL_INFO_CPU_FIRST  0x00010000    /* First Rx buffer in the packet */
#define VMAC_RX_CTL_INFO_CPU_LAST   0x00020000    /* Last Rx buffer in the packet */
#define VMAC_RX_CTL_INFO_CPU_OWN    0x80000000    /* CPU/VMAC Ownership of buffer */

/* Pointer To Rx Buffer Bit Mask */
#define VMAC_RX_BUFFER_PTR          0xFFFFFFFF    /* Physical address of the start of the buffer of data */

/* MDIO_DATA register bits */
#define VMAC_MDIO_SFD               0x40000000
#define VMAC_MDIO_OP_WRITE          0x10000000
#define VMAC_MDIO_OP_READ           0x20000000
#define VMAC_MDIO_PHY_MASK          0x0F800000
#define VMAC_MDIO_PHY_MASK_NOT      (~VMAC_MDIO_PHY_MASK)     
#define VMAC_MDIO_REG_MASK          0x007C0000
#define VMAC_MDIO_REG_MASK_NOT      (~VMAC_MDIO_REG_MASK)
#define VMAC_MDIO_TA                0x00020000
#define VMAC_MDIO_DATA_MASK         0x0000FFFF
#define VMAC_MDIO_DATA_MASK_NOT     (~VMAC_MDIO_DATA_MASK)


/*--------------------------------------------------------------------------*/
/* 
**                        DATA TYPES
*/

typedef volatile struct vmac_buffer_descr_struct {
   /* Control information written by VMAC/CPU */
   uint_32     CTRL_INFO;
   /* Address of the start of data */
   uchar_ptr   BUFFER_PTR;
} VMAC_BUFFER_DESCR_STRUCT, _PTR_ VMAC_BUFFER_DESCR_STRUCT_PTR;


typedef volatile struct vmac_reg_struct {
   uint_32  ID;    
   uint_32  INT_STATUS;    
   uint_32  INT_ENABLE;    
   uint_32  CONTROL;       
   uint_32  POLLRATE;
   uint_32  RXERR;
   uint_32  MISS;
   /* 
   ** Address of the start of 
   ** the Tx Ring of BDTs 
   */
   VMAC_BUFFER_DESCR_STRUCT_PTR  TXRINGPTR;
   /* Address of the start of 
   ** the Rx Ring of BDTs 
   */  
   VMAC_BUFFER_DESCR_STRUCT_PTR  RXRINGPTR;  
   uint_32  MAC_ADDR_L;
   uint_32  MAC_ADDR_H;
   uint_32  MAC_LAF_L;
   uint_32  MAC_LAF_H;
   uint_32  MDIO_DATA;
   uint_32  TXRINGPTR_READ;
   uint_32  RXRINGPTR_READ;
} VMAC_REG_STRUCT, _PTR_ VMAC_REG_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/* 
**                        FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
