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
*** File: lwdemo.h
*** 
***************************************************************************
*END**********************************************************************/

/* Defines and data structures for application */

#define LWSEMB            7
#define LWSEMA            8
#define LWEVENTB          9
#define LWEVENTA          10
#define SENDER           13
#define RESPONDER        14
#define MAIN_TASK        15

#define NO_TIMEOUT        0
#define SIZE_UNLIMITED    0

/* Definitions for LW Message Queue Component */
#define NUM_MESSAGES		  4
#define MSG_SIZE			  1

/*
**   Externs for global data
*/
/* LW Event Definitions */
extern	 LWEVENT_STRUCT lwevent;

/* LW Sem Definitions */
extern	 LWSEM_STRUCT lwsem;

/* LW Message Queue Definitions */
extern	 uint_32 main_queue[];
extern	 uint_32 sender_queue[];
extern	 uint_32 responder_queue[];

/*
** Externs for Tasks and ISRs
*/
extern void LWSemB(uint_32);
extern void LWSemA(uint_32);
extern void LWEventB(uint_32);
extern void LWEventA(uint_32);
extern void Sender(uint_32);
extern void Responder(uint_32);
extern void main_task(uint_32);


/* EOF */
