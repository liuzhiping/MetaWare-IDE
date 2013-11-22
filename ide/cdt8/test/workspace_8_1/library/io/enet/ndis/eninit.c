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
*** File: eninit.c
***
*** Comments:  This file contains the NDIS Packet driver Ethernet initialization
***            functions.
***
***
************************************************************************
*END*******************************************************************/

/* Start SPR P155-0122-01 */

#include "mqx_inc.h"
#include "bsp.h"
#include "ntddndis.h"
#include "packet32.h"
#include "enet.h"
#include "enetprv.h"
#include <stddef.h>

/* Used by ENET_get_handle */
_enet_handle      ENET_Handle = NULL;

/* Used for mutual exclusion between library packet functions */
CRITICAL_SECTION  ENET_cs;


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_initialize
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Initializes the device.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_initialize
   (
      /* [IN] the SCC to initialize */
      uint_32              sccnum,

      /* [IN] the local Ethernet address */
      _enet_address        address,

      /* [IN] optional flags, zero = default */
      uint_32              flags,

      /* [OUT] the Ethernet state structure */
      _enet_handle _PTR_   handle
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr;
   uchar_ptr           pcb_base, buf_ptr;
   PCB_PTR             pcb_ptr;
   uint_32             i;
   uint_32             error;

   InitializeCriticalSection(&ENET_cs); 

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself.
   */
   ENET_lock();

   /* Allocate the state structure */
   enet_ptr = ENET_memalloc(sizeof(ENET_CFG_STRUCT));
   if (!enet_ptr) {
      ENET_unlock();
      return ENETERR_ALLOC_CFG;
   } /* Endif */

   /* Allocate the PCBs */
   pcb_base = ENET_memalloc(RX_RING_LEN * (sizeof(PCB) + sizeof(PCB_FRAGMENT) +
      ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX)) + ENET_FRAMESIZE_ALIGN(1));
   if (!pcb_base) {
      ENET_memfree((pointer)enet_ptr);
      ENET_unlock();
      return ENETERR_ALLOC_PCB;
   } /* Endif */
   enet_ptr->PCB_BASE = pcb_base;

   /* Initialize the state structure */
   ENET_memcopy(address, enet_ptr->ADDRESS, sizeof(_enet_address));
   enet_ptr->ECB_HEAD  = NULL;

   /* Enqueue the PCBs onto the receive ring */
   pcb_ptr = (PCB_PTR)pcb_base;
   buf_ptr = pcb_base + RX_RING_LEN * (sizeof(PCB) + sizeof(PCB_FRAGMENT));
   buf_ptr = (uchar_ptr)ENET_FRAMESIZE_ALIGN((uint_32)buf_ptr);
   i = RX_RING_LEN + 1;
   while ( --i ) {
      pcb_ptr->FREE    = ENET_rx_add;
      pcb_ptr->PRIVATE = enet_ptr;
      pcb_ptr->FRAG[0].FRAGMENT = buf_ptr;
      pcb_ptr->FRAG[1].LENGTH   = 0;
      pcb_ptr->FRAG[1].FRAGMENT = NULL;
      ENET_rx_add(pcb_ptr);
      pcb_ptr = (PCB_PTR)((uchar_ptr)pcb_ptr + sizeof(PCB) + sizeof(PCB_FRAGMENT));
      buf_ptr += ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX);
   } /* Endwhile */

   if (_int_install_isr(BSP_ENET_INTERRUPT_VECTOR, ENET_ISR, (pointer)enet_ptr) 
      == NULL) 
   {
      ENET_memfree((pointer)enet_ptr);
      ENET_memfree((pointer)pcb_base);
      ENET_unlock();
      return(ENETERR_INSTALL_ISR);
   } /* Endif */

   error = ENET_initialize_packet_driver(enet_ptr, address, flags);
   if (error != ENET_OK) {
      ENET_memfree((pointer)enet_ptr);
      ENET_memfree((pointer)pcb_base);
      ENET_unlock();
      return error;
   } /* Endif */

   ENET_unlock();

   *handle = (pointer)enet_ptr;
   ENET_Handle = (_enet_handle)enet_ptr;
    
   return ENET_OK;

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : ENET_initialize_packet_driver
* Returned Value   : 
* Comments         : NDIS-specific initialization
*
*END*-------------------------------------------------------------------------*/

uint_32 ENET_initialize_packet_driver
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr,

      /* [IN] the local Ethernet address */
      _enet_address        address,

      /* [IN] optional flags, zero = default */
      uint_32              flags
   )
{ /* Body */
   uint_32     dummy;
   uint_32     buff_len = ADAPTER_NAME_LEN;

   /* Start the packet driver */
   PacketInit(NULL, 0);

   /* Get the available adapter names from the registry */
   PacketGetAdapterNames((PTSTR)enet_ptr->ADAPTER_NAME, &buff_len);

   /* Open the adapter */
   enet_ptr->DEV_HANDLE = PacketOpenAdapter((LPTSTR)enet_ptr->ADAPTER_NAME);

   /* Set the filter type to receive all packets */
   PacketSetFilter(enet_ptr->DEV_HANDLE, NDIS_PACKET_TYPE_BROADCAST | 
      NDIS_PACKET_TYPE_DIRECTED | NDIS_PACKET_TYPE_MULTICAST);

   /* Create the thread that will act as the "interrupt" for received data */
   enet_ptr->RX_THREAD_HANDLE  = CreateThread(NULL, 0L,
      (LPTHREAD_START_ROUTINE)ENET_Rx_data_thread, (LPVOID)enet_ptr,
      CREATE_SUSPENDED, &dummy);
   SetThreadPriority(enet_ptr->RX_THREAD_HANDLE, THREAD_PRIORITY_BELOW_NORMAL);
   ResumeThread(enet_ptr->RX_THREAD_HANDLE);

   if (enet_ptr->RX_THREAD_HANDLE == NULL) {
      PacketCloseAdapter(enet_ptr->DEV_HANDLE);
      return ENETERR_INSTALL_ISR;
   } /* Endif */

   return ENET_OK;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_get_handle
*  Returned Value : Initialized Ethernet handle or NULL
*  Comments       :
*        Retrieves an initialized Ethernet handle.
*
*END*-----------------------------------------------------------------*/

_enet_handle ENET_get_handle
   (
      /* [IN] the channel number */
      uint_32  channel_number
   )
{ /* Body */
   
   return ENET_Handle;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_rx_add
*  Returned Value : void
*  Comments       :
*        Enqueues a PCB onto the receive ring.
*
*END*-----------------------------------------------------------------*/

void ENET_rx_add
   (
      /* [IN] the PCB to enqueue */
      PCB_PTR  pcb_ptr
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR  enet_ptr = (ENET_CFG_STRUCT_PTR)pcb_ptr->PRIVATE;

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself.
   */
   ENET_lock();

   /*
   ** Add the PCB to the receive PCB queue (linked via PRIVATE) and
   ** increment the tail to the next descriptor
   */
   pcb_ptr->FRAG[0].LENGTH = 0;
   QADD(enet_ptr->RxHead, enet_ptr->RxTail, pcb_ptr);
   enet_ptr->RxEntries++;
   ENET_unlock();

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_rx_catchup
*  Returned Value : void
*  Comments       :
*        Records statistics following frame transmission.
*
*END*-----------------------------------------------------------------*/

void ENET_rx_catchup
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr,

      /* [IN] not used */
      uint_16              events
   )
{ /* Body */
   ENET_ECB_STRUCT_PTR     ecb_ptr;
   PCB_PTR                 pcb_ptr;

   while (TRUE) {

      /* Get current rx pcb */
      pcb_ptr = enet_ptr->RxHead;
      if (pcb_ptr->FRAG[0].LENGTH == 0) {
         /* No more to do */
         break;
      }/* Endif */

      enet_ptr->RxEntries--;
      QGET(enet_ptr->RxHead, enet_ptr->RxTail, pcb_ptr);
      enet_ptr->STATS.ST_RX_TOTAL++;

      pcb_ptr->PRIVATE = (pointer)enet_ptr;

      /* Find a receiver */
      ecb_ptr = ENET_recv(enet_ptr, pcb_ptr);
      if (ecb_ptr) {
         /* Forward the PCB to the application */
         ecb_ptr->SERVICE(pcb_ptr, ecb_ptr->PRIVATE);
      } else {
         enet_ptr->STATS.ST_RX_DISCARDED++;
         ENET_rx_add(pcb_ptr);
      } /* Endif */

   } /* Endwhile */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_tx_record
*  Returned Value : void
*  Comments       :
*        Records statistics following frame transmission.
*
*END*-----------------------------------------------------------------*/

void ENET_tx_record
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr,

      /* [IN] the descriptor status word (all buffers) */
      uint_16              status_all,

      /* [IN] the descriptor status word (last buffer) */
      uint_16              status_last
   )
{ /* Body */

   /* No other statistics available */
   enet_ptr->STATS.ST_TX_TOTAL++;

} /* Endbody */


/*NOTIFIER*-------------------------------------------------------------
*
*  Function Name  : ENET_ISR
*  Returned Value : void
*  Comments       :
*        Initializes the chip.
*
*END*-----------------------------------------------------------------*/

void ENET_ISR
   (
      /* [IN] the Ethernet state structure */
      pointer  enet
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR  enet_ptr = (ENET_CFG_STRUCT_PTR)enet;

   /* We only have to handle receive "interrupts" */
   ENET_rx_catchup(enet_ptr, 0);

} /* Endbody */


/*THREAD*-------------------------------------------------------------------------
*
* Thread Name : ENET_Rx_data_thread
* Comments  : Will call ENET_ISR every time data is received
*
*END*-------------------------------------------------------------------------*/

/* Number of times missed obtaining a NDIS packet */
uint_32 ENET_no_ndis_packets = 0;

/* Number of packets missed due to a lack of PCBS */
uint_32 ENET_rx_dropped_packets = 0;
 
/* Number of packets received */
uint_32 ENET_rx_total_packets = 0;

/* The location to receive data into before copying to a PCB */ 
uchar   ENET_rx_packet[ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX) + 
   ENET_FRAMESIZE_ALIGN(1)];

void ENET_Rx_data_thread
   (
      /* [IN] The enet handle */
      pointer enet
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr = (ENET_CFG_STRUCT_PTR)enet;
   PCB_PTR             pcb_scan_ptr;
   PCB_PTR             pcb_ptr;
   pointer             packet_ptr;
   uint_32             i;
   uint_32             length;

   while (TRUE) {

      packet_ptr = NULL;
      while (packet_ptr == NULL) {
         EnterCriticalSection(&ENET_cs);
         /* Allocate a packet object to receive data */
         packet_ptr = PacketAllocatePacket(enet_ptr->DEV_HANDLE);
         LeaveCriticalSection(&ENET_cs);
         if (packet_ptr == NULL) {
            ENET_no_ndis_packets++;
            Sleep(5); /* Wait for a while for a free packet */
         } /* Endif */
      } /* Endwhile */

      /* Give the receive buffer address to the packet */
      EnterCriticalSection(&ENET_cs);
      PacketInitPacket(packet_ptr, ENET_rx_packet,
         ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX) + ENET_FRAMESIZE_ALIGN(1));
      LeaveCriticalSection(&ENET_cs);

      /* Call a "blocking" receive function to wait for data */
      PacketReceivePacket(enet_ptr->DEV_HANDLE, packet_ptr, TRUE, 
         (PULONG)&length);

      ENET_rx_total_packets++;

      pcb_ptr = NULL;
      if (enet_ptr->RxEntries != 0) {
         /* Scan the list of entries looking for one that has not been filled */
         i = enet_ptr->RxEntries;
         pcb_scan_ptr = enet_ptr->RxHead;
         while (i--)  {
            if (pcb_scan_ptr->FRAG[0].LENGTH == 0) {
               pcb_ptr = pcb_scan_ptr;
               break;
            } /* Endif */
            pcb_scan_ptr = QNEXT(pcb_scan_ptr);
         } /* Endwhile */
      } /* Endif */

      if (pcb_ptr) {
         _mem_copy(ENET_rx_packet, pcb_ptr->FRAG[0].FRAGMENT, length);
         pcb_ptr->FRAG[0].LENGTH = length;
         /* Simulate an interrupt, and pass on received data */
         _psp_generate_interrupt(BSP_ENET_INTERRUPT_VECTOR);
      } else {
         ENET_rx_dropped_packets++;
      }/* Endif */
      
      /* Release the packet object */
      EnterCriticalSection(&ENET_cs);
      PacketFreePacket(packet_ptr);
      LeaveCriticalSection(&ENET_cs);

   } /* Endwhile */

} /* Endbody */

/* End SPR P155-0122-01 */

/* EOF */
