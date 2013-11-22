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
*** File: demo.c
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <message.h>
#include <errno.h>
#include <mutex.h>
#include <sem.h>
#include <event.h>
#include <log.h>
#include <klog.h>
#include "demo.h"
/*  Message Queue Ids */
 _queue_id   Sender_Queue_qid;
/*  Message Queue Ids */
 _queue_id   Responder_Queue_qid;
/*  Message Queue Ids */
 _queue_id   Main_Queue_qid;
/*  Message Pool Ids */
 _pool_id   MsgPool_pool_id;
/*  Mutex Definitions */
MUTEX_STRUCT      Mutex1;


/*   Function Code       */


/* End of File */
