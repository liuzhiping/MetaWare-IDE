#ifndef __lwmsq_h__
#define __lwmsq_h__ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: lwmsgq.h
***
*** Comments:
***   This file containts definitions for use with light weight message queues
***
***************************************************************************
*END**********************************************************************/


/*--------------------------------------------------------------------------*/
/*
**                            MACRO DEFINITIONS
*/

/* control bits for lwmsgq_send */

/* Block writing task if msgq if full */
#define LWMSGQ_SEND_BLOCK_ON_FULL           (0x01)

/* Block the sending task once message has been sent */
#define LWMSGQ_SEND_BLOCK_ON_SEND           (0x02)

/* Block the reading task if msgq is empty */
#define LWMSGQ_RECEIVE_BLOCK_ON_EMPTY       (0x04)

/* Perform a timeout using the tick structure as the absolute time */
#define LWMSGQ_TIMEOUT_UNTIL                (0x08)

/* Perform a timeout using the tick structure as the relative time */
#define LWMSGQ_TIMEOUT_FOR                  (0x10)

/* Error messages */
#define LWMSGQ_INVALID                      (MSG_ERROR_BASE | 0x80)
#define LWMSGQ_FULL                         (MSG_ERROR_BASE | 0x81)
#define LWMSGQ_EMPTY                        (MSG_ERROR_BASE | 0x82)
#define LWMSGQ_TIMEOUT                      (MSG_ERROR_BASE | 0x83)

/* Helpful macros */

#define LWMSGQ_SIZE(lwq) (((LWMSGQ_STRUCT_PTR)(lwq))->CURRENT_SIZE)


/* Return whether the queue is empty */
#define LWMSGQ_IS_EMPTY(lwq) (((LWMSGQ_STRUCT_PTR)(lwq))->CURRENT_SIZE == 0)


/* Return whether the queue is full */
#define LWMSGQ_IS_FULL(q) \
   (((LWMSGQ_STRUCT_PTR)(q))->CURRENT_SIZE >= ((LWMSGQ_STRUCT_PTR)(q))->MAX_SIZE)

/*--------------------------------------------------------------------------*/
/*
**                            DATATYPE DECLARATIONS
*/

/*---------------------------------------------------------------------
**
** LWMSGQ STRUCTURE
**
** This structure used to store a circular long word queue.
** The structure must be the LAST if it is included into another
** data structure, as the queue falls off of the end of
** this structure.
*/
typedef struct lwmsgq_struct
{

   /* Start CR 1944 */
   /* Queue data structures */
   QUEUE_ELEMENT_STRUCT LINK;
   /* End CR 1944 */

   /* A Queue of task descriptors waiting to write */
   QUEUE_STRUCT WAITING_WRITERS;

   /* A Queue of task descriptors waiting to read */
   QUEUE_STRUCT WAITING_READERS;

   /* The validity check field */
   _mqx_uint    VALID;

   /* The size of the message chunk in the queue in _mqx_max_type's */
   _mqx_uint    MSG_SIZE;

   /* The maximum number of msgs for the queue, as specified in
    * initialization of the queue.
    */
   _mqx_uint     MAX_SIZE;

   /* The current number of messages in the queue. */
   _mqx_uint    CURRENT_SIZE;

   /* Next message location to write to */
   _mqx_max_type_ptr MSG_WRITE_LOC;

   /* Next message location to read from */
   _mqx_max_type_ptr MSG_READ_LOC;

   /* Starting location of messages */
   _mqx_max_type_ptr MSG_START_LOC;

   /* Location past end of messages */
   _mqx_max_type_ptr MSG_END_LOC;

} LWMSGQ_STRUCT, _PTR_ LWMSGQ_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
** FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

_mqx_uint _lwmsgq_init(pointer, _mqx_uint, _mqx_uint);
_mqx_uint _lwmsgq_send(pointer, _mqx_max_type_ptr, _mqx_uint);
_mqx_uint _lwmsgq_receive(pointer, _mqx_max_type_ptr, _mqx_uint, _mqx_uint,
   MQX_TICK_STRUCT_PTR);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
