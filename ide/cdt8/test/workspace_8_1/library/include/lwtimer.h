#ifndef __lwtimer_h__
#define __lwtimer_h__ 1
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
*** File: lwtimer.h
***
*** Comments: 
***    This include file is used to define constants and data types for the
***  light weight timer component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                     DATA STRUCTURE DEFINITIONS                           */

/*
**  LWTIMER STRUCTURE
**
** This structure defines a light weight timer.
** These timers implement a system where the specified function
** will be called at a periodic interval.
*/
typedef struct lwtimer_struct
{

   /* Queue data structures */
   QUEUE_ELEMENT_STRUCT       LINK;

   /* The relative number of ticks until this timer is to fire */
   _mqx_uint                  RELATIVE_TICKS;

   /* A validation stamp */
   _mqx_uint                  VALID;

   /* The function to be called by this timer */
   void           (_CODE_PTR_ TIMER_FUNCTION)(pointer);
   
   /* The parameter to be passed to the timer function */
   pointer                    PARAMETER;

   /* What period is this timer attatched to */
   pointer                    PERIOD_PTR;
  
} LWTIMER_STRUCT, _PTR_ LWTIMER_STRUCT_PTR;


/*
**  LWTIMER PERIOD STRUCTURE
**
** This structure controls any number of timers
** wishing to be executed at the periodic rate defined by this structure.
** The periodic rate will be a multiple of the BSP_ALARM_RESOLUTION.
*/
typedef struct lwtimer_period_struct
{

   /* Queue data structures */
   QUEUE_ELEMENT_STRUCT       LINK;

   /* The period of this group of timers (in ticks) */
   _mqx_uint                  PERIOD;

   /* The number of ticks that have elapsed in this period */
   _mqx_uint                  EXPIRY;

   /*
   ** The number of ticks to allow to elapse before starting to
   ** process this queue
   */
   _mqx_uint                  WAIT;

   /* A queue of timers to fire at this periodic rate */
   QUEUE_STRUCT               TIMERS;

   /* The last timer on the queue processed */
   LWTIMER_STRUCT_PTR         TIMER_PTR;

   /* A validation stamp */
   _mqx_uint                  VALID;

} LWTIMER_PERIOD_STRUCT, _PTR_ LWTIMER_PERIOD_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*                       EXTERNAL DECLARATIONS                              */

#ifdef __cplusplus
extern "C" {
#endif
#ifndef __TAD_COMPILE__

extern _mqx_uint _lwtimer_add_timer_to_queue(LWTIMER_PERIOD_STRUCT_PTR,
   LWTIMER_STRUCT_PTR, _mqx_uint, void (_CODE_PTR_)(pointer), pointer);
extern _mqx_uint _lwtimer_cancel_period(LWTIMER_PERIOD_STRUCT_PTR);
extern _mqx_uint _lwtimer_cancel_timer(LWTIMER_STRUCT_PTR);
extern _mqx_uint _lwtimer_create_periodic_queue(LWTIMER_PERIOD_STRUCT_PTR,
   _mqx_uint, _mqx_uint);
extern _mqx_uint _lwtimer_test(pointer _PTR_, pointer _PTR_);

#endif
#ifdef __cplusplus
}
#endif

#endif
/* EOF */
