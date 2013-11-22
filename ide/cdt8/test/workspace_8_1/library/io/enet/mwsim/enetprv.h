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

#define RX_RING_LEN 16

#define ENET_FRAMESIZE_MIN    64
#define ENET_FRAMESIZE_MAX    (ENET_FRAMESIZE_HEAD \
                             + ENET_FRAMESIZE_MAXDATA \
                             + ENET_FRAMESIZE_TAIL)

#define ENET_FRAMESIZE_ALIGN(n)  (n)

/***************************************
**
** Code macros
**
*/

#define ENET_lock()              _int_disable()
#define ENET_unlock()            _int_enable()
#define ENET_memalloc(n)         _mem_alloc_system_zero(n)
#define ENET_memfree(ptr)        _mem_free(ptr)
#define ENET_memzero(ptr,n)      _mem_zero(ptr,n)
#define ENET_memcopy(src,dst,n)  _mem_copy(src,dst,n)

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
typedef volatile struct {
   ENET_STATS        STATS;      /* must be first field */
   uint_32           DEV_NUM;
   uint_32           DEV_VEC;
   volatile EMWSIM_STRUCT _PTR_ DEV_PTR;
   _enet_address     ADDRESS;

   /* the transmit-side state */
   char              TxBuffer[ENET_FRAMESIZE_MAX+16];

   /* the receive-side state */
   PCB_PTR           RxHead;
   PCB_PTR           RxTail;
   uint_32           RxEntries;
   PCB_PTR           RxCurrent;

   ENET_ECB_STRUCT_PTR ECB_HEAD;

   /* These fields are kept only for ENET_shutdown() */
   void (_CODE_PTR_  OLDISR_PTR)(pointer);
   pointer           OLDISR_DATA;
   pointer           PCB_BASE;

   /* Counters */
   uint_32           INTS;
   uint_32           RX_INTS;

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

extern void    ENET_rx_add(PCB_PTR);
extern void    ENET_rx_catchup(ENET_CFG_STRUCT_PTR);
extern void    ENET_rx_reset(ENET_CFG_STRUCT_PTR);
extern void    ENET_rx_error(ENET_CFG_STRUCT_PTR);
extern void    ENET_rx_record(ENET_CFG_STRUCT_PTR);
extern void    ENET_tx_catchup(ENET_CFG_STRUCT_PTR);

extern void    ENET_ISR(pointer);

extern void    _bsp_enet_init(uint_32, uint_32, uint_32);
extern pointer _bsp_enet_getbase(uint_32);
extern uint_32 _bsp_enet_getvec(uint_32);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
