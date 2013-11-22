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
*** File: demo.h
*** 
***************************************************************************
*END**********************************************************************/

/*   Defines and data structures for application       */

#define MUTEXB      5
#define MUTEXA      6
#define SEMB      7
#define SEMA      8
#define EVENTB      9
#define EVENTA      10
#define BTIMESLICETASK      11
#define ATIMESLICETASK      12
#define SENDER      13
#define RESPONDER      14
#define MAIN_TASK      15

#define    NO_TIMEOUT     0
#define   SIZE_UNLIMITED  0


/* Defines for Semaphore Component */
#define   SEM_INITIAL_NUMBER    10
#define   SEM_GROWTH            10
#define   SEM_MAXIMUM           20

/* Defines for Event Component */
#define   EVENT_INITIAL_NUMBER    10
#define   EVENT_GROWTH            10
#define   EVENT_MAXIMUM           20

/*
**   Externs for global data
*/
/*  Message Queue Ids */
 extern   _queue_id   Sender_Queue_qid;
/*  Message Queue Ids */
 extern   _queue_id   Responder_Queue_qid;
/*  Message Queue Ids */
 extern   _queue_id   Main_Queue_qid;
/*  Message Pool Ids */
 extern   _pool_id   MsgPool_pool_id;
/*  Mutex Definitions */
extern   MUTEX_STRUCT      Mutex1;

/*
** Externs for Tasks and ISRs
*/
extern void MutexB (uint_32);
extern void MutexA (uint_32);
extern void SemB (uint_32);
extern void SemA (uint_32);
extern void EventB (uint_32);
extern void EventA (uint_32);
extern void BTimeSliceTask (uint_32);
extern void ATimeSliceTask (uint_32);
extern void Sender (uint_32);
extern void Responder (uint_32);
extern void main_task (uint_32);


/* EOF */
