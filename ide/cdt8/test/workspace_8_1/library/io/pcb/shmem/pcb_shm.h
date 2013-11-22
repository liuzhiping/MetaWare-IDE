#ifndef __pcb_shm_h__
#define __pcb_shm_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: pcb_shm.h
***
*** Comments: 
***   This file contains the definitions for the PCB device driver that
*** sends and receives packets over a asynchrnous serial device.  The
*** packets are in MQX IPC async packet format.
*** 
***************************************************************************
*END**********************************************************************/


/*--------------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/*
** Initialization errors
*/
#define IO_PCB_SHM_DEVICE_ALREADY_OPEN         (0x2090)
#define IO_PCB_SHM_INCORRECT_SERIAL_DEVICE     (0x2091)

/* Start CR 617 */
#if 0
/*
**             PACKET STRUCTURE CONTROL FIELD BIT DEFINITIONS
*/

#define IO_PCB_SHM_HDR_LITTLE_ENDIAN           (0x40)
#define IO_PCB_SHM_DATA_LITTLE_ENDIAN          (0x20)

#define IO_PCB_SHM_HDR_BIG_ENDIAN              (0x00)
#define IO_PCB_SHM_DATA_BIG_ENDIAN             (0x00)
#endif
/* End CR 617 */

/*--------------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/

/*
** IO_PCB_SHM_INIT_STRUCT
** This structure contains the initialization information for the
** async shared memory protocol
**
*/
typedef struct io_pcb_shm_init_struct
{

   /* Shared memory base address */
   pointer    TX_BD_ADDR;

   /* TX ring linmit */ 
   pointer    TX_LIMIT_ADDR;

   /* Shared memory base address */
   pointer    RX_BD_ADDR;

   /* RX ring limit */
   pointer    RX_LIMIT_ADDR;
   
   /* Maximum size of input packet */
   _mem_size  INPUT_MAX_LENGTH;

   /* interrupt vector */
   uint_32    RX_VECTOR;
   uint_32    TX_VECTOR;
   uint_32    REMOTE_RX_VECTOR;
   uint_32    REMOTE_TX_VECTOR;

   /* The address of function to trigger interrupts */
   void (_CODE_PTR_ INT_TRIGGER)(uint_32);

} IO_PCB_SHM_INIT_STRUCT, _PTR_ IO_PCB_SHM_INIT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                          C PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_uint _io_pcb_shm_install(char _PTR_, pointer);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
