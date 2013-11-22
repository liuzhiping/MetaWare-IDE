#ifndef __enetprv_h__
#define __enetprv_h__
/*HEADER****************************************************************
************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: enetprv.h
***
*** Comments:  This file contains the private defines, externs and
***            data structure definitions required by the Ethernet
***            packet driver.
***
************************************************************************
*END*******************************************************************/
#include "vmac.h"
#include "io_pcb.h"

/* Start CR 809 */
/* 
** Uncomment this define to enale chaining on transmit
** This will allow zero-copy on the Tx side of the VMAC
** driver
*/
//#define VMAC_ENABLE_TX_CHAINING	1
/* End CR 809 */

/* Make sure the ring lengths do align the rings on word boundary */
#define RX_RING_LEN 32
#define TX_RING_LEN 32

#ifdef ENET_FRAMESIZE_MIN
#undef ENET_FRAMESIZE_MIN
#endif
#define ENET_FRAMESIZE_MIN    60

/* Start CR 552 */
#if 0
#define ENET_FRAMESIZE_MAX    (ENET_FRAMESIZE_HEAD \
                             + ENET_FRAMESIZE_MAXDATA \
                             + ENET_FRAMESIZE_TAIL)
#else
/* This define is to get around the zero buffer descriptor problem with the VMAC */
#define ENET_FRAMESIZE_EXTRA  4
#define ENET_FRAMESIZE_MAX    (ENET_FRAMESIZE_HEAD \
                             + ENET_FRAMESIZE_MAXDATA \
                             + ENET_FRAMESIZE_TAIL\
                             + ENET_FRAMESIZE_EXTRA)
#endif
/* End   CR 552 */

/* No need to align the buffers; we align for better performance */
#define ENET_FRAMESIZE_ALIGN(n)  ((n) + (-(n) & PSP_MEMORY_ALIGNMENT))

/* Macro for aligning the Ring start address */
#define ENET_DESCR_ALIGN(n)      ((n) + (-(n) & 3))

#define VMAC_ENET_TX_RETRIES     (TX_RING_LEN+1)

/* Autonegatiation falg */
#define ENET_AUTONEGOTIATE             (0x1)

/* Polling flag */
#define ENET_POLL_PEER_PERIODICALLY    (0x2)

/***************************************
**
** Code macros
**
*/

#define ENET_lock()              _int_disable()
#define ENET_unlock()            _int_enable()
#define ENET_memalloc(n)         _mem_alloc_system_zero((_mem_size)(n))
#define ENET_memfree(ptr)        _mem_free((pointer)(ptr))
#define ENET_memzero(ptr,n)      _mem_zero((pointer)(ptr),(_mem_size)(n))
#define ENET_memcopy(src,dst,n)  _mem_copy((pointer)(src),(pointer)(dst),(_mem_size)(n))

#define RX_INC(index)   if (++index == RX_RING_LEN) index = 0
#define TX_INC(index)   if (++index == TX_RING_LEN) index = 0

#define QADD(head,tail,pcb)      \
   if ((head) == NULL) {         \
      (head) = (pcb);            \
   } else {                      \
      (tail)->PRIVATE = (pcb);   \
   } /* Endif */                 \
   (tail) = (pcb);               \
   (pcb)->PRIVATE = NULL

#define QGET(head,tail,pcb)      \
   (pcb) = (head);               \
   if (head) {                   \
      (head) = (PCB_PTR)((head)->PRIVATE);  \
      if ((head) == NULL) {      \
         (tail) = NULL;          \
      } /* Endif */              \
   } /* Endif */

/***************************************
**
** Data structures
**
*/

/* Joined multicast groups */
typedef struct mcb {
   uint_16           HASH;
   _enet_address     GROUP;
   struct mcb _PTR_  NEXT;
} ENET_MCB_STRUCT, _PTR_ ENET_MCB_STRUCT_PTR;

/* Registered protocol numbers */
typedef struct ecb {
   uint_16              TYPE;
   void (_CODE_PTR_     SERVICE)(PCB_PTR, pointer);
   ENET_MCB_STRUCT_PTR  MCB_HEAD;
   pointer              PRIVATE;
   struct ecb _PTR_     NEXT;
} ENET_ECB_STRUCT, _PTR_ ENET_ECB_STRUCT_PTR;

/* the Ethernet state structure */
typedef volatile struct enet_cfg_struct {
   ENET_STATS                    STATS;      /* must be first field */
   uint_32                       DEV_NUM;
   uint_32                       DEV_VEC;
   VMAC_REG_STRUCT_PTR           DEV_PTR;
   VMAC_BUFFER_DESCR_STRUCT_PTR  TX_RING;
   VMAC_BUFFER_DESCR_STRUCT_PTR  RX_RING;
   _enet_address                 ADDRESS;
   uint_16                       RESERVED;

   /* the transmit-side state */
   uint_32                       TXENTRIES;
   uint_32                       TXNEXT;
   uint_32                       TXLAST;
   /* Start CR 809 */
   /* uchar                         PAD_ARRAY[ENET_FRAMESIZE_MIN]; */
   /* uchar_ptr                     TXCOMPLETEPACKETS;             */
   pointer                       START_TX_RING;

#if VMAC_ENABLE_TX_CHAINING
   uchar                         PAD_ARRAY[ENET_FRAMESIZE_MIN];
   uint_32                       TX_ERRORS;
   PCB_PTR                       TXPCBS[TX_RING_LEN];
#else
   /* 
   ** Following pointers are used for freeing the aligned/unaligned 
   ** memory in enstop 
   */
   pointer                       START_TXCOMPLETEPACKETS;
   uchar_ptr                     TXCOMPLETEPACKETS;
#endif
   /* End   CR 809 */

   /* the receive-side state */
   PCB_PTR                       RXHEAD;
   PCB_PTR                       RXTAIL;
   uint_32                       RXENTRIES;
   uint_32                       RXNEXT;
   uint_32                       RXLAST;
   boolean                       FULL_DUPLEX;

   ENET_ECB_STRUCT_PTR           ECB_HEAD;

   /* These fields are kept only for ENET_shutdown() */
   void (_CODE_PTR_              OLDISR_PTR)(pointer);
   pointer                       OLDISR_DATA;
   pointer                       PCB_BASE;
   
   /* Start CR 809 */
   /* 
   ** Following pointers are used for freeing the aligned/unaligned 
   ** memory in enstop 
   */
   /* pointer                       START_TX_RING;           */
   /* pointer                       START_TXCOMPLETEPACKETS; */
   /* End   CR 809 */

} ENET_CFG_STRUCT, _PTR_ ENET_CFG_STRUCT_PTR;


/***************************************
**
** Prototypes
**
*/

#ifdef __cplusplus
extern "C" {
#endif

extern void  ENET_read_address(ENET_CFG_STRUCT_PTR);

extern ENET_ECB_STRUCT_PTR ENET_recv(ENET_CFG_STRUCT_PTR, PCB_PTR);

extern uint_32 ENET_send_MAC(ENET_CFG_STRUCT_PTR, PCB_PTR, uint_32, uint_32, uint_32);
extern void    ENET_join_MAC(ENET_CFG_STRUCT_PTR, ENET_MCB_STRUCT_PTR);
extern void    ENET_rejoin_MAC(ENET_CFG_STRUCT_PTR);

extern void  ENET_rx_add(PCB_PTR);
extern void  ENET_rx_catchup(ENET_CFG_STRUCT_PTR);
extern void  ENET_rx_reset(ENET_CFG_STRUCT_PTR);
extern void  ENET_rx_error(ENET_CFG_STRUCT_PTR);
extern void  ENET_rx_record(ENET_CFG_STRUCT_PTR);
extern void  ENET_tx_catchup(ENET_CFG_STRUCT_PTR);
extern void  ENET_ISR (pointer);

extern void _bsp_enet_init (pointer, uint_32, uint_32);
extern VMAC_REG_STRUCT_PTR _bsp_enet_getbase(uint_32);
extern uint_32 _bsp_enet_getvec(uint_32);
extern uint_32 _bsp_enet_get_phyid(uint_32);
extern uint_32 _bsp_enet_get_poll_period(uint_32);
extern uint_32 _bsp_enet_get_poll_priority(uint_32);
/* Start CR 694 */
extern uint_32 _bsp_enet_get_speed(pointer);
/* End   CR 694 */
extern uint_32 _VMAC_mdio_read(VMAC_REG_STRUCT_PTR, uint_32, uint_32, uint_32_ptr);
extern boolean _VMAC_mdio_write(VMAC_REG_STRUCT_PTR, uint_32, uint_32, uint_32);
extern uint_32 _VMAC_init_poll_mode(pointer);
extern void    VMAC_poll_task(uint_32);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
