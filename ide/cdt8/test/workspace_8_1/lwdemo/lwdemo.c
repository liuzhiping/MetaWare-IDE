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
*** File: lwdemo.c
*** 
*** Comments:
***    This file contains source for the Lightweight MQX demo test.
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <errno.h>
#include <lwevent.h>
#include <lwmsgq.h>
#include "lwdemo.h"

/*
** Global variable definitions
*/
/* Message Queue Ids */
_queue_id      Sender_Queue_qid;

/* Message Queue Ids */
_queue_id      Responder_Queue_qid;

/* Message Queue Ids */
_queue_id      Main_Queue_qid;

/* Message Pool Ids */
_pool_id       MsgPool_pool_id;

/* Use light weight events */
LWEVENT_STRUCT lwevent;

/* Use light weight semaphores */
LWSEM_STRUCT lwsem;

/* Use light weight message queues */
uint_32 main_queue[sizeof(LWMSGQ_STRUCT)/sizeof(uint_32) +
   NUM_MESSAGES * MSG_SIZE];
uint_32 sender_queue[sizeof(LWMSGQ_STRUCT)/sizeof(uint_32) +
   NUM_MESSAGES * MSG_SIZE];
uint_32 responder_queue[sizeof(LWMSGQ_STRUCT)/sizeof(uint_32) +
   NUM_MESSAGES * MSG_SIZE];

/*   Function Code       */


/* End of File */
