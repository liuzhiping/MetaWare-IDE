#ifndef __wdog_prv_h__
#define __wdog_prv_h__ 1
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
*** File: wdog_prv.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
***  watchdog component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* The number of tasks in the alarm table */
#define WATCHDOG_TABLE_SIZE   (16)

/* The watchdog validation number */
#define WATCHDOG_VALID        (_mqx_uint)(0x77646f67)     /* "wdog" */

/*--------------------------------------------------------------------------*/
/*                       DATATYPE DEFINITIONS                               */

/*
**  watchdog alarm table structure
*/
typedef struct watchdog_alarm_table_struct
{

   /* The next table if required */
   struct watchdog_alarm_table_struct _PTR_ NEXT_TABLE_PTR;

   /* The tasks being monitored */
   TD_STRUCT_PTR                            TD_PTRS[WATCHDOG_TABLE_SIZE];

} WATCHDOG_ALARM_TABLE_STRUCT, _PTR_ WATCHDOG_ALARM_TABLE_STRUCT_PTR;

/*
** watchdog component structure
*/
typedef struct watchdog_component_struct
{

   /* The table of alarms */
   WATCHDOG_ALARM_TABLE_STRUCT ALARM_ENTRIES;

   /* Watchdog validation stamp */
   _mqx_uint                    VALID;
   
   /* The function to call when the watchdog expires */
   void            (_CODE_PTR_ ERROR_FUNCTION)(TD_STRUCT_PTR td_ptr);
   
   /* The old timer interrupt handler */
   void            (_CODE_PTR_ TIMER_INTERRUPT_HANDLER)(pointer parameter);

   /* The interrupt vector */
   _mqx_uint                    INTERRUPT_VECTOR;

} WATCHDOG_COMPONENT_STRUCT, _PTR_ WATCHDOG_COMPONENT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*                       PROTOTYPE DEFINITIONS                              */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void     _watchdog_cleanup(TD_STRUCT_PTR);
extern void     _watchdog_isr(pointer);
extern boolean  _watchdog_start_internal(MQX_TICK_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

