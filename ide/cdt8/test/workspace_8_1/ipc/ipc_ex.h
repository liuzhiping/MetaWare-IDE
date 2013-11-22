#ifndef __ipc_ex_h__
#define __ipc_ex_h__
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
*** File: ipc_ex.h
***
*** Comments: 
***    This file contains the source for the common definitions for the
*** IPC example
*** 
***************************************************************************
*END**********************************************************************/

#define TEST_ID            1
#define IPC_TTN            9
#define MAIN_TTN          10
#define QUEUE_TO_TEST2    63
#define MAIN_QUEUE        64
#define TEST2_ID           2
#define RESPONDER_TTN     11
#define QUEUE_TO_TEST     67
#define RESPONDER_QUEUE   65

typedef struct the_message
{
   MESSAGE_HEADER_STRUCT  HEADER;
   uint_32                DATA;
} THE_MESSAGE, _PTR_ THE_MESSAGE_PTR;

#endif
/* EOF */
