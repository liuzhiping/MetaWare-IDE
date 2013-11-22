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
*** File: ttl.c
***
*** Comments: 
***    This file contains the task template list for this processor.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <lwmsgq.h>
#include "server.h"

uint_32 server_queue[sizeof(LWMSGQ_STRUCT)/sizeof(uint_32) +
   NUM_MESSAGES * MSG_SIZE];
uint_32 client_queue[sizeof(LWMSGQ_STRUCT)/sizeof(uint_32) +
   NUM_MESSAGES * MSG_SIZE];

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{  
    {SERVER_TASK, server_task, 800, 5, "server",
       MQX_AUTO_START_TASK, 0, 0},
    {CLIENT_TASK, client_task, 600, 5, "client",
       0,                   0, 0},    
    {0,           0,           0,   0, 0,
       0,                   0, 0}
};

/* EOF */
