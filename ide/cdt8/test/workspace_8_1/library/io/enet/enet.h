#ifndef __enet_h__
#define __enet_h__
/*HEADER****************************************************************
************************************************************************
***
*** Copyright (c) 1989-2005 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: enet.h
***
*** Comments:  This file contains the defines, externs and data
***            structure definitions required by application
***            programs in order to use the Ethernet packet driver.
***
************************************************************************
*END*******************************************************************/

#include <pcb.h>

/*--------------------------------------------------------------------------*/
/*                        
**                            CONSTANT DEFINITIONS
*/

/* Error codes */

#define ENET_OK                       0

#define ENETERR_MIN              0x0200
#define ENETERR_MAX              0x0213

#define ENETERR_INVALID_DEVICE   0x0200   /* Device number out of range  */
#define ENETERR_INIT_DEVICE      0x0201   /* Device already initialized  */
#define ENETERR_ALLOC_CFG        0x0202   /* Alloc state failed          */
#define ENETERR_ALLOC_PCB        0x0203   /* Alloc PCBs failed           */
#define ENETERR_ALLOC_BD         0x0204   /* Alloc BDs failed            */
#define ENETERR_INSTALL_ISR      0x0205   /* Install ISR failed          */
#define ENETERR_FREE_PCB         0x0206   /* PCBs in use                 */
#define ENETERR_ALLOC_ECB        0x0207   /* Alloc ECB failed            */
#define ENETERR_OPEN_PROT        0x0208   /* Protocol not open           */
#define ENETERR_CLOSE_PROT       0x0209   /* Protocol already open       */
#define ENETERR_SEND_SHORT       0x020A   /* Packet too short            */
#define ENETERR_SEND_LONG        0x020B   /* Packet too long             */
#define ENETERR_JOIN_MULTICAST   0x020C   /* Not a multicast address     */
#define ENETERR_ALLOC_MCB        0x020D   /* Alloc MCB failed            */
#define ENETERR_LEAVE_GROUP      0x020E   /* Not a joined group          */
#define ENETERR_SEND_FULL        0x020F   /* Transmit ring full          */
#define ENETERR_IP_TABLE_FULL    0x0210   /* IP Table full of IP pairs   */
#define ENETERR_ALLOC            0x0211   /* Generic alloc failed        */
#define ENETERR_INIT_FAILED      0x0212   /* Device failed to initialize */
#define ENETERR_DEVICE_TIMEOUT   0x0213   /* Device read/write timeout   */

/* Other constants */

#define ENET_FRAMESIZE_HEAD      sizeof(ENET_HEADER)
#define ENET_FRAMESIZE_MAXHEAD   (sizeof(ENET_HEADER)+sizeof(ENET_8021QTAG_HEADER))
#define ENET_FRAMESIZE_MAXDATA   1500
#define ENET_FRAMESIZE_MIN       64
#define ENET_FRAMESIZE_TAIL      4


#define SIZE_IP_ADDR_TABLE  100    /* max number of IP pairs in table */


/* ENET_send() options */

#define ENET_OPT_8023             0x0001
#define ENET_OPT_8021QTAG         0x0002
#define ENET_SETOPT_8021QPRIO(p)  (ENET_OPT_8021QTAG | \
   (((uint_32)(p) & 0x7) << 2))
#define ENET_GETOPT_8021QPRIO(f)  (((f) >> 2) & 0x7)

#define htone(p,x)   ((p)[0] = (x)[0], \
                      (p)[1] = (x)[1], \
                      (p)[2] = (x)[2], \
                      (p)[3] = (x)[3], \
                      (p)[4] = (x)[4], \
                      (p)[5] = (x)[5]  \
                     )

#define ntohe(p,x)   ((x)[0] = (p)[0] & 0xFF, \
                      (x)[1] = (p)[1] & 0xFF, \
                      (x)[2] = (p)[2] & 0xFF, \
                      (x)[3] = (p)[3] & 0xFF, \
                      (x)[4] = (p)[4] & 0xFF, \
                      (x)[5] = (p)[5] & 0xFF  \
                     )

/*--------------------------------------------------------------------------*/
/*                        
**                            TYPE DEFINITIONS
*/

typedef uchar   _enet_address[6];
typedef pointer _enet_handle;

/*
** Ethernet packet header
*/
typedef struct enet_header {
   uchar    DEST[6];
   uchar    SOURCE[6];
   uchar    TYPE[2];
} ENET_HEADER, _PTR_ ENET_HEADER_PTR;

typedef struct enet_8021qtag_header {
   uchar    TAG[2];
   uchar    TYPE[2];
} ENET_8021QTAG_HEADER, _PTR_ ENET_8021QTAG_HEADER_PTR;

typedef struct enet_8022_header {
   uchar    DSAP[1];
   uchar    SSAP[1];
   uchar    COMMAND[1];
   uchar    OUI[3];
   uchar    TYPE[2];
} ENET_8022_HEADER, _PTR_ ENET_8022_HEADER_PTR;


typedef struct enet_stats {

   uint_32     ST_RX_TOTAL;         /* Total number of received packets    */
   uint_32     ST_RX_MISSED;        /* Number of missed packets            */
   uint_32     ST_RX_DISCARDED;     /* Discarded -- unrecognized protocol  */
   uint_32     ST_RX_ERRORS;        /* Discarded -- error during reception */

   uint_32     ST_TX_TOTAL;         /* Total number of transmitted packets */
   uint_32     ST_TX_MISSED;        /* Discarded -- transmit ring full     */
   uint_32     ST_TX_DISCARDED;     /* Discarded -- bad packet             */
   uint_32     ST_TX_ERRORS;        /* Error during transmission           */

   uint_32     ST_TX_COLLHIST[16];  /* Collision histogram      */

   /* Following stats are physical errors/conditions */
   uint_32     ST_RX_ALIGN;         /* Frame Alignment error    */
   uint_32     ST_RX_FCS;           /* CRC error                */
   uint_32     ST_RX_RUNT;          /* Runt packet received     */
   uint_32     ST_RX_GIANT;         /* Giant packet received    */
   uint_32     ST_RX_LATECOLL;      /* Late collision           */
   uint_32     ST_RX_OVERRUN;       /* DMA overrun              */

   uint_32     ST_TX_SQE;           /* Heartbeat lost           */
   uint_32     ST_TX_DEFERRED;      /* Transmission deferred    */
   uint_32     ST_TX_LATECOLL;      /* Late collision           */
   uint_32     ST_TX_EXCESSCOLL;    /* Excessive collisions     */
   uint_32     ST_TX_CARRIER;       /* Carrier sense lost       */
   uint_32     ST_TX_UNDERRUN;      /* DMA underrun             */

} ENET_STATS, _PTR_ ENET_STATS_PTR;



/*--------------------------------------------------------------------------*/
/*                        
**                            PROTOTYPES AND GLOBAL EXTERNS
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
uint_32        ENET_close(_enet_handle, uint_16);
uint_32        ENET_get_address(_enet_handle, _enet_address);
_enet_handle   ENET_get_handle(uint_32);
ENET_STATS_PTR ENET_get_stats(_enet_handle);
_mqx_uint      ENET_initialize(_mqx_uint, _enet_address, _mqx_uint, 
   _enet_handle _PTR_);
uint_32        ENET_join(_enet_handle, uint_16, _enet_address);
uint_32        ENET_leave(_enet_handle, uint_16, _enet_address);
uint_32        ENET_open(_enet_handle, uint_16, 
   void (_CODE_PTR_)(PCB_PTR, pointer), pointer);
uint_32        ENET_send(_enet_handle, PCB_PTR, uint_16, _enet_address, 
   uint_32);
uint_32        ENET_shutdown(_enet_handle);
const char_ptr ENET_strerror(_mqx_uint);
uint_32        ENET_translate (_enet_handle, uint_32, uint_32);
uint_32        ENET_get_speed (_enet_handle);
#endif

#ifdef __cplusplus
}
#endif

#endif

/* EOF */
