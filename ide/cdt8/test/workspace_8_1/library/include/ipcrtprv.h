#ifndef __ipcrtprv_h__
#define __ipcrtprv_h__ 1
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
*** File: ipcrtprv.h
***
*** Comments: 
***   This file contains the definitions private to the router used to route
*** IPC messages.
***
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/*--------------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/

/*
** IPC MSG ROUTING STRUCT
**
** This structure contains info for a particular route
*/
typedef struct ipc_msg_routing_struct
{

   /* Used to link all routing structures together */
   QUEUE_ELEMENT_STRUCT  LINK;

   /* The minimum processor number in the range */
   _processor_number     MIN_PROC_NUMBER;

   /* The maximum processor number in the range */
   _processor_number     MAX_PROC_NUMBER;

   /* The queue to use if the processor number is in the above range */
   _queue_number         QUEUE;

} IPC_MSG_ROUTING_STRUCT, _PTR_ IPC_MSG_ROUTING_STRUCT_PTR;

/*
** IPC MSG ROUTING COMPONENT STRUCT
**
** The structure kept in the kernel data by the message routing component
*/
typedef struct ipc_msg_routing_component_struct 
{
   /* Linked list of routes installed */
   QUEUE_STRUCT         ROUTING_LIST;

   boolean  (_CODE_PTR_ MSG_ROUTER)(_processor_number, pointer, boolean);
   
} IPC_MSG_ROUTING_COMPONENT_STRUCT, _PTR_ IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
**                          C PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint   _ipc_msg_route_init_internal(void);
extern _mqx_uint   _ipc_msg_route_internal(_processor_number, pointer, boolean);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
