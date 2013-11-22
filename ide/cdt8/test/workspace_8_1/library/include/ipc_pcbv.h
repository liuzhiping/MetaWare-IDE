#ifndef __ipc_pcbv_h__
#define __ipc_pcbv_h__ 1
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
*** File: ipc_pcbv.h
***
*** Comments: 
***   This file contains the private definitions for the IO PCB
*** interprocessor drivers.
*** 
***************************************************************************
*END**********************************************************************/


/*--------------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/* Stack sizes */
#define IPC_PCB_STACK_SIZE IPC_DEFAULT_STACK_SIZE

/*--------------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/


/*
** IPC_PCB_INFO_STRUCT
** This structure contains protocol information for the IPC over PCBs
**
*/
typedef struct ipc_pcb_info_struct
{
   /* Queue headers for keeping track of driver */
   QUEUE_ELEMENT_STRUCT QUEUE;

   /* The IO PCB device to use */
   FILE   _PTR_         FD;

   /* input definitions */
   _io_pcb_pool_id      PCB_INPUT_POOL;
   _pool_id             MSG_INPUT_POOL;
 
   /* output message qid */
   _io_pcb_pool_id      PCB_OUTPUT_POOL;
   _queue_id            OUT_MSG_QID;
   
   /* statistical information */
   _mqx_uint            OUTPUT_MESSAGE_COUNT;
   _mqx_uint            INPUT_MESSAGE_COUNT;

} IPC_PCB_INFO_STRUCT, _PTR_ IPC_PCB_INFO_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
**                          C PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _ipc_pcb_free(IO_PCB_STRUCT_PTR);
extern IO_PCB_STRUCT_PTR _ipc_pcb_alloc(IO_PCB_STRUCT_PTR, pointer);
extern void _ipc_pcb_output_notification(pointer);
extern void _ipc_pcb_input_notification(FILE_PTR, IO_PCB_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
