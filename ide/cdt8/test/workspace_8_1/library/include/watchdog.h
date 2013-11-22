#ifndef __watchdog_h__
#define __watchdog_h__ 1
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
*** File: watchdog.h
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

/* Error codes */

#define WATCHDOG_INVALID_ERROR_FUNCTION   (WATCHDOG_ERROR_BASE|0x01)
#define WATCHDOG_INVALID_INTERRUPT_VECTOR (WATCHDOG_ERROR_BASE|0x02)


/*--------------------------------------------------------------------------*/
/*                        DATA STRUCTURE DEFINITIONS                        */

/*
**  external declarations for the interface procedures
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _watchdog_create_component(_mqx_uint, void (_CODE_PTR_)(pointer));
extern boolean   _watchdog_stop(void);
extern boolean   _watchdog_start(uint_32);
extern boolean   _watchdog_start_ticks(MQX_TICK_STRUCT_PTR);
extern _mqx_uint _watchdog_test(pointer _PTR_, pointer _PTR_);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
