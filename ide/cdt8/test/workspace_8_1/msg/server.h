#ifndef __server_h__
#define __server_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2003 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: server.h
***
*** Comments: 
***    This file contains definitions for this application
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <message.h>

/* Number of clients */
#define NUM_CLIENTS 3

/* Task IDs */
#define SERVER_TASK       5
#define CLIENT_TASK       6

/* Queue IDs */
#define SERVER_QUEUE      8
#define CLIENT_QUEUE_BASE 9

/* This structure contains a data field and a message header structure */
typedef struct server_message
{
   MESSAGE_HEADER_STRUCT   HEADER;
   uchar                   DATA[5];
} SERVER_MESSAGE, _PTR_ SERVER_MESSAGE_PTR;

/* Function prototypes */
extern void server_task (uint_32 initial_data);
extern void client_task (uint_32 initial_data);
extern _pool_id message_pool;

#endif
/* EOF */
