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

/* Start SPR P155-0122-01 */

#include <stdlib.h>

#define ADAPTER_NAME_LEN      64

#define RX_RING_LEN           16
#define ENET_FRAMESIZE_MIN    64
#define ENET_FRAMESIZE_MAX \
   (ENET_FRAMESIZE_HEAD + ENET_FRAMESIZE_MAXDATA + ENET_FRAMESIZE_TAIL)
#define ENET_FRAMESIZE_ALIGN(n)  ((n) + (-(n) & 15))
#define ENET_BD_ALIGN(n)         ((n) + (-(n) & 15))


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

#define QNEXT(pcb) (pcb)->PRIVATE

#define QGET(head,tail,pcb)      \
   (pcb) = (head);               \
   if (head) {                   \
      (head) = (head)->PRIVATE;  \
      if ((head) == NULL) {      \
         (tail) = NULL;          \
      } /* Endif */              \
   } /* Endif */

#define ENET_ALIGN_ADDR_TO_HIGHER_MEM(mem_ptr) (pointer) \
   ((uint_32)((uchar_ptr)(mem_ptr) + PSP_MEMORY_ALIGNMENT) & PSP_MEMORY_ALIGNMENT_MASK)

/***************************************
**
** Data structures
**
*/

/* Joined multicast groups */
typedef struct mcb {
   _enet_address     GROUP;
   uint_32           HASH;
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

/* The Ethernet state structure */
typedef volatile struct {
   ENET_STATS              STATS;      /* must be first field */
   uint_32                 RESERVED1;
   _enet_address           ADDRESS;
   uint_16                 RESERVED2;

   /* NDIS Fields */
   pointer                 DEV_HANDLE;
   HANDLE                  RX_THREAD_HANDLE;
   char                    ADAPTER_NAME[ADAPTER_NAME_LEN];
  
   /*
   ** Ring descriptor information:
   **
   **    XxHead    = address of the first PCB queued.
   **    XxTail    = address of the last PCB queued.
   **    XxEntries = number of queued PCBs.
   */
   PCB_PTR                 RxHead;
   PCB_PTR                 RxTail;
   uint_32                 RxEntries;

   ENET_ECB_STRUCT_PTR     ECB_HEAD;

   /* This field is kept only for ENET_shutdown() */
   pointer                 PCB_BASE;

} ENET_CFG_STRUCT, _PTR_ ENET_CFG_STRUCT_PTR;


/***************************************
**
** Prototypes
**
*/
extern CRITICAL_SECTION ENET_cs;
_enet_handle EnetHandle;

uint_32 ENET_initialize_packet_driver(ENET_CFG_STRUCT_PTR, _enet_address, uint_32);
void    ENET_rx_catchup              (ENET_CFG_STRUCT_PTR, uint_16);
void    ENET_rx_add                  (PCB_PTR);
void    ENET_tx_record               (ENET_CFG_STRUCT_PTR, uint_16, uint_16);
void    ENET_ISR                     (pointer);
void    ENET_Rx_data_thread          (pointer);

ENET_ECB_STRUCT_PTR ENET_recv(ENET_CFG_STRUCT_PTR, PCB_PTR);

uint_32 ENET_send_MAC   (ENET_CFG_STRUCT_PTR, PCB_PTR, uint_32, uint_32, uint_32);
void    ENET_join_MAC   (ENET_CFG_STRUCT_PTR, ENET_MCB_STRUCT_PTR);
void    ENET_rejoin_MAC (ENET_CFG_STRUCT_PTR);

/* End SPR P155-0122-01 */

#endif
/* EOF */
