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
*** File: ipc_ttt.c
***
*** Comments: 
***   This file contains the dummy router table for the IPC.
***
***
***************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "ipc.h"

#if MQX_USE_IPC
IPC_ROUTING_STRUCT _ipc_routing_table[] =
{
   { 0, 0, 0 }
};
#endif /* MQX_USE_IPC */

/* EOF */
