#ifndef __ipc_prv__
#define __ipc_prv__
/*HEADER*******************************************************************
***************************************************************************
*** 
*** Copyright (c) 1989-2006 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: ipc_prv.h
***
*** Comments: 
***   This file contains the private definitions for the inter-processor
*** communications component.
***
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/*
** The number of ipc messages in the ipc message pool
** which is created at initialization time.
*/
#define IPC_NUM_MESSAGES        (8)
#define IPC_GROW_MESSAGES       (8)
#define IPC_LIMIT_MESSAGES      (0)

/*
** 2.3X message types 
*/
#define IPC_MQX_CREATE          (0x01)
#define IPC_MQX_DESTROY         (0x02)
#define IPC_MQX_ACTIVATE        (0x03)
/* Start CR# 1903 */
//#define IPC_MQX_TYPE_MASK       (0x03)
#define IPC_MQX_ABORT           (0x05)  
#define IPC_MQX_RESTART         (0x06)
#define IPC_MQX_TYPE_MASK       (0x07)
/* End CR# 1903 */

/* Helpful macros for packing and unpacking the message type */
#define IPC_GET_COMPONENT(t) \
   (((uint_32)(t) >> 16) & 0x3FF)
#define IPC_SET_COMPONENT(t,c)  \
   ((uint_32)(t) | (((uint_32)(c) & 0x3FF) << 16))
#define IPC_SET_IO_COMPONENT(t,c)  \
   ((uint_32)(t) | ((((uint_32)(c) + MAX_KERNEL_COMPONENTS) & 0x3FF) << 16))

#define IPC_GET_NON_BLOCKING(t) \
   ((uint_32)(t) & 0x80000000)
#define IPC_SET_NON_BLOCKING(t,c) \
   ((c) ? ((uint_32)(t) | 0x80000000) : ((uint_32)(t)))

#define IPC_GET_TYPE(t) \
   (((uint_32)(t) >> 8) & 0xFF)
#define IPC_SET_TYPE(t,c) \
   ((uint_32)(t) | ((uint_32)(c) & 0xFF) << 8)

#define IPC_SET_MESSAGE_TYPE(comp,type,non_blocking,val) \
   ((val) | \
   (((uint_32)(comp) & 0x3FF) << 16) | \
   (((uint_32)(type) & 0xFF) << 8) | \
   ((non_blocking) ? 0x80000000 : 0x0))

#define IPC_SET_IO_MESSAGE_TYPE(comp,type,non_blocking,val) \
   ((val) | \
   ((((uint_32)(comp) + MAX_KERNEL_COMPONENTS) & 0x3FF) << 16) | \
   (((uint_32)(type) & 0xFF) << 8) | \
   ((non_blocking) ? 0x80000000 : 0x0))

/* The ipc_task message queue number */
#define IPC_MESSAGE_QUEUE_NUMBER  (1)

/* The most number of parameters that can be sent via the IPC */
#if IPC_DATA_SIZE < 3
#error INVALID IPC DATA SIZE
#endif
#define IPC_MAX_PARAMETERS        (IPC_DATA_SIZE-3)

/* The last parameter is a string to send to the target */
#define IPC_STRING_PARAMETER      (0x8000)

/*--------------------------------------------------------------------------*/
/*
**                         DATATYPE DECLARATIONS
*/

/*
** IPC MESSAGE STRUCT
**
** This structure defines the message sent to the ipc task.
*/
typedef struct ipc_message_struct
{

   /* MQX standard message header */
   MESSAGE_HEADER_STRUCT HEADER;

   /* 
   ** The high bit is set if the message is non-blocking
   ** Bits 25 to 16 indicate the component number.
   ** Bits 15 to 8  indicate the message type for the component.
   ** Bits 1  to 0  are used for task creation, destruction and
   **   activation.
   */
   /* What type of message is this */
   uint_32               MESSAGE_TYPE;

   /* The number of parameters to follow: */
   uint_32               NUMBER_OF_PARAMETERS;
   
   /* The task ID of the requesting task */
   _task_id              REQUESTOR_ID;

   /* The maximum number of parameters to send */
   uint_32               PARAMETERS[IPC_MAX_PARAMETERS];
   
} IPC_MESSAGE_STRUCT, _PTR_ IPC_MESSAGE_STRUCT_PTR;


/*
** IPC MQX MESSAGE STRUCTURE
**
** This structure defines the ipc messages for 2.3x compatibility.
*/

typedef struct ipc_mqx_message_struct
{

   /* MQX standard message header */
   MESSAGE_HEADER_STRUCT HEADER;

   /* What type of message is this (See ipc_message_struct) */
   uint_32               MESSAGE_TYPE;

   /*
   ** The task template index on the processor.  If 0, then use
   ** the task template embedded in the message
   */
   uint_32               TEMPLATE_INDEX;

   /* The task ID of the requesting task */
   _task_id              REQUESTOR_ID;

   /* The task ID of the task to be destroyed */
   _task_id              VICTIM_ID;

   /* The task creation parameter for the task being created */
   uint_32               CREATE_PARAMETER;

   /* The embedded task template */
   TASK_TEMPLATE_STRUCT  TEMPLATE;

} IPC_MQX_MESSAGE_STRUCT, _PTR_ IPC_MQX_MESSAGE_STRUCT_PTR;


/*
** IPC COMPONENT STRUCT
** This structure contains the component information for the IPC task.
*/
typedef struct ipc_component_struct
{

   /* Handlers for ipc components */
   _mqx_uint (_CODE_PTR_ IPC_COMPONENT_HANDLER[MAX_KERNEL_COMPONENTS])(
      IPC_MESSAGE_STRUCT_PTR msg_ptr);

   /* Handlers for ipc components */
   _mqx_uint (_CODE_PTR_ IPC_IO_COMPONENT_HANDLER[MAX_IO_COMPONENTS])(
      IPC_MESSAGE_STRUCT_PTR msg_ptr);

} IPC_COMPONENT_STRUCT, _PTR_ IPC_COMPONENT_STRUCT_PTR;


/*
** IPC PROTOCOL INFO STRUCT
** This structure contains standard Inter-Processor protocol information,
** kept with the inter-processor driver.
*/
typedef struct ipc_protocol_info_struct
{

   /* What type of inter-processor communications protocol is this? */
   _mqx_uint           IPC_TYPE;

   /* This is the output queue number to which MQX+M will send messages
   ** that are to be sent to the remote processor
   */
   _queue_number       IPC_OUT_QUEUE;

   /* A string name used to identify this IPC, from the init record */
   char _PTR_          IPC_NAME;

   /* The queue ID of the output queue */
   _queue_id           IPC_OUT_QID;

   /* The address of protocol specific information */
   pointer             IPC_PROTOCOL_INFO_PTR;
   
   /* The address of the initialization record used to initialize this IPC */
   IPC_PROTOCOL_INIT_STRUCT_PTR IPC_INIT_PTR;

} IPC_PROTOCOL_INFO_STRUCT, _PTR_ IPC_PROTOCOL_INFO_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                          C PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _ipc_send_internal(boolean, _processor_number, _mqx_uint,
   _mqx_uint, _mqx_uint, ...);      
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
