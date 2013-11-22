#ifndef __evnt_prv_h__
#define __evnt_prv_h__ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: evnt_prv.h
***
*** Comments: 
***    This include file is used to define constants and data types private
*** to the event component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Used to mark a block of memory as belonging to an event group */
#define EVENT_VALID                    ((_mqx_uint)(0x65766E74))   /* "evnt" */

/* Used to indicate that an event occurred */
#define EVENT_OCCURRED                 (2)
#define EVENT_WANTS_ALL                (1)

#define EVENT_MAX_WAITING_TASKS        ((uint_16)0)

/* IPC Message types for remote event access */
#define IPC_EVENT_OPEN                 (1)
#define IPC_EVENT_SET                  (2)

/*--------------------------------------------------------------------------*/
/*                      DATA STRUCTURE DEFINITIONS                          */

/* 
** EVENT COMPONENT STRUCTURE
**
** This is the base structure pointed to by the kernel data
** structure COMPONENT field for events
*/
typedef struct event_component_struct
{

   /* The maximum number of event instances allowed */
   _mqx_uint        MAXIMUM_NUMBER;

   /* The number of event instances to grow by when table full */
   _mqx_uint        GROW_NUMBER;

   /* A validation stamp allowing for checking of memory overwrite. */
   _mqx_uint        VALID;

   /* The handle to the naming table for events */
   pointer         NAME_TABLE_HANDLE;

} EVENT_COMPONENT_STRUCT, _PTR_ EVENT_COMPONENT_STRUCT_PTR;


/* 
** EVENT STRUCTURE
**
** This is the structure of an instance of an event.  The address
** is kept in the event name table, associated with the name.
*/
typedef struct event_struct
{

   /* This is the queue of waiting tasks.
   ** What is queued is the address  of the handle provided 
   ** to the user (EVENT_COMPONENT_STRUCT)
   */
   QUEUE_STRUCT    WAITING_TASKS;

   /* This is a validation stamp for the event */
   _mqx_uint       VALID;
   
   /* the actual event bits */
   _mqx_uint       EVENT;

   /* event type */
   boolean         AUTO_CLEAR;
   
   /* The string name of the event, includes null */
   char            NAME[NAME_MAX_NAME_SIZE];

} EVENT_STRUCT, _PTR_ EVENT_STRUCT_PTR;


/* 
** EVENT CONNECTION STRUCTURE
**    This is the structure whose address is returned to the user
** as an event handle.
*/
typedef struct event_connection_struct
{

   /* 
   ** These pointers are used to link the connection struct onto
   ** the WAITING TASK queue of the event
   */
   pointer           NEXT;
   pointer           PREV;

   /* This is a validation stamp for the data structure */
   _mqx_uint         VALID;

   /* 
   ** Is this event on a remote procssor, if non-zero it is the processor 
   ** number of the remote processor.
   */
   /* Start SPR P171-0012-01 */
   /* uint_32 REMOTE_CPU;    */
   _mqx_uint         REMOTE_CPU;
   /* End SPR P171-0012-01 */

   /* The bit mask of bits to wait for */
   _mqx_uint         MASK;
   
   /* A flag indicating whether all bits are required, or whether
   ** an event has been set
   */
   _mqx_uint         FLAGS;
   
   /* The address of the task descriptor that owns this connection */
   TD_STRUCT_PTR     TD_PTR;

   /* The address of the event structure associated with this connection */
   EVENT_STRUCT_PTR  EVENT_PTR;

    
} EVENT_CONNECTION_STRUCT, _PTR_ EVENT_CONNECTION_STRUCT_PTR;

/* ANSI c prototypes */
#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void      _event_cleanup(TD_STRUCT_PTR);
extern _mqx_uint _event_create_internal(char _PTR_, EVENT_STRUCT_PTR _PTR_);
extern _mqx_uint _event_create_fast_internal(_mqx_uint, EVENT_STRUCT_PTR _PTR_);
extern _mqx_uint _event_wait_internal(pointer, _mqx_uint, MQX_TICK_STRUCT_PTR, 
   boolean, boolean);
/* Start CR 1947 */
extern _mqx_uint _event_wait_any_internal(pointer, _mqx_uint, MQX_TICK_STRUCT_PTR, 
   boolean);
/* End CR 1947 */   
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
