#ifndef __lwevent_h__
#define __lwevent_h__ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: lwevent.h
***
*** Comments: 
***    This include file is used to define constants and data types for the
***  light weight event component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Creation flags */
#define LWEVENT_AUTO_CLEAR        (0x00000001)

/* Error code */
#define LWEVENT_WAIT_TIMEOUT      (EVENT_ERROR_BASE|0x10)

/*--------------------------------------------------------------------------*/
/*
**                         LWEVENT STRUCTURE
**
** This structure defines a light weight event.
** Tasks can wait on and set event bits.
*/
typedef struct lwevent_struct
{

   /* Queue data structures */
   QUEUE_ELEMENT_STRUCT       LINK;
  
   /* The queue of tasks waiting for bits to be set */
   QUEUE_STRUCT               WAITING_TASKS;
   
   /* A validation stamp */
   _mqx_uint                  VALID;

   /* the current bit value of the lwevent */
   _mqx_uint                  VALUE;

   /* flags associated with the light weight event */
   _mqx_uint                  FLAGS;
   
} LWEVENT_STRUCT, _PTR_ LWEVENT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _lwevent_clear(LWEVENT_STRUCT_PTR, _mqx_uint);
extern _mqx_uint _lwevent_create(LWEVENT_STRUCT_PTR, _mqx_uint);
extern _mqx_uint _lwevent_destroy(LWEVENT_STRUCT_PTR);
extern _mqx_uint _lwevent_set(LWEVENT_STRUCT_PTR, _mqx_uint);
extern _mqx_uint _lwevent_test(pointer _PTR_, pointer _PTR_);
extern _mqx_uint _lwevent_wait_for(LWEVENT_STRUCT_PTR, _mqx_uint,
   boolean, MQX_TICK_STRUCT_PTR);
extern _mqx_uint _lwevent_wait_ticks(LWEVENT_STRUCT_PTR, _mqx_uint,
   boolean, _mqx_uint);
extern _mqx_uint _lwevent_wait_until(LWEVENT_STRUCT_PTR, _mqx_uint,
   boolean, MQX_TICK_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
