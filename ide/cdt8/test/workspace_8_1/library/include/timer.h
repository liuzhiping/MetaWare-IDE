#ifndef __timer_h__
#define __timer_h__ 1
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
*** File: timer.h
***
*** Comments: 
***    This include file is used to define constants and data types for the
***  timer component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* 
** This mode tells the timer to use the elapsed time when calculating time 
** (_time_get_elapsed)
*/
#define TIMER_ELAPSED_TIME_MODE       (1)

/* 
** This mode tells the timer to use the actual time when calculating time
** (_time_get)  Note that the time returned by _time_get can be modified
** by _time_set.
*/
#define TIMER_KERNEL_TIME_MODE        (2)

/* The error return from the timer start functions */
#define TIMER_NULL_ID                 ((_timer_id)0)

/* The default parameters for the timer_create_component function */
#define TIMER_DEFAULT_TASK_PRIORITY   (1)
#define TIMER_DEFAULT_STACK_SIZE      (800)

/*--------------------------------------------------------------------------*/
/*                     DATA STRUCTURE DEFINITIONS                           */

typedef _mqx_uint  _timer_id;

/*--------------------------------------------------------------------------*/
/*                       EXTERNAL DECLARATIONS                              */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint  _timer_cancel(_timer_id);
extern _mqx_uint  _timer_create_component(_mqx_uint, _mqx_uint);
extern _timer_id  _timer_start_oneshot_after(
   void (_CODE_PTR_)(_timer_id, pointer, uint_32, uint_32), pointer, _mqx_uint, 
   uint_32);
extern _timer_id  _timer_start_oneshot_at( 
   void (_CODE_PTR_)(_timer_id, pointer, uint_32, uint_32), pointer, _mqx_uint, 
   TIME_STRUCT_PTR);        
extern _timer_id  _timer_start_periodic_every( 
   void (_CODE_PTR_)(_timer_id id, pointer, uint_32, uint_32), pointer, _mqx_uint, 
   uint_32);        
extern _timer_id  _timer_start_periodic_at( 
   void (_CODE_PTR_)(_timer_id id, pointer, uint_32, uint_32), pointer, _mqx_uint,
   TIME_STRUCT_PTR, uint_32);
extern _timer_id  _timer_start_oneshot_after_ticks(
   void (_CODE_PTR_)(_timer_id, pointer, MQX_TICK_STRUCT_PTR), pointer, _mqx_uint, 
   MQX_TICK_STRUCT_PTR);
extern _timer_id  _timer_start_oneshot_at_ticks( 
   void (_CODE_PTR_)(_timer_id, pointer, MQX_TICK_STRUCT_PTR), pointer, _mqx_uint, 
   MQX_TICK_STRUCT_PTR);        
extern _timer_id  _timer_start_periodic_every_ticks( 
   void (_CODE_PTR_)(_timer_id id, pointer, MQX_TICK_STRUCT_PTR), pointer, _mqx_uint, 
   MQX_TICK_STRUCT_PTR);        
extern _timer_id  _timer_start_periodic_at_ticks( 
   void (_CODE_PTR_)(_timer_id id, pointer, MQX_TICK_STRUCT_PTR), pointer, _mqx_uint,
   MQX_TICK_STRUCT_PTR, MQX_TICK_STRUCT_PTR);
extern _mqx_uint  _timer_test(pointer _PTR_);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
