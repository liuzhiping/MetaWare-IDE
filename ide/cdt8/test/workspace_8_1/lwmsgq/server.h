#ifndef __server_h__
#define __server_h__
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
*** File: server.h
***
*** Comments: 
***    This file contains definitions for this application
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>

/* Number of clients */
#define NUM_CLIENTS 3

/* Task IDs */
#define SERVER_TASK       5
#define CLIENT_TASK       6

/* This structure contains a data field and a message header structure */
#define NUM_MESSAGES  3
#define MSG_SIZE      1
extern uint_32 server_queue[];
extern uint_32 client_queue[];

/* Function prototypes */
extern void server_task (uint_32 initial_data);
extern void client_task (uint_32 initial_data);

#endif
/* EOF */
