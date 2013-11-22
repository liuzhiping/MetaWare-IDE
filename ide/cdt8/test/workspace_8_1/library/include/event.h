#ifndef __event_h__
#define __event_h__ 1
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
*** File: event.h
***
*** Comments: 
***    This include file is used to define constants and data types for the
***  event component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* ERROR messages */

#define EVENT_MULTI_PROCESSOR_NOT_AVAILABLE     (EVENT_ERROR_BASE|0x00)
#define EVENT_DELETED                           (EVENT_ERROR_BASE|0x01)
#define EVENT_NOT_DELETED                       (EVENT_ERROR_BASE|0x02)
#define EVENT_INVALID_EVENT_HANDLE              (EVENT_ERROR_BASE|0x03)
#define EVENT_CANNOT_SET                        (EVENT_ERROR_BASE|0x04)
#define EVENT_CANNOT_GET_EVENT                  (EVENT_ERROR_BASE|0x05)
#define EVENT_INVALID_EVENT_COUNT               (EVENT_ERROR_BASE|0x06)
#define EVENT_WAIT_TIMEOUT                      (EVENT_ERROR_BASE|0x07)
#define EVENT_EXISTS                            (EVENT_ERROR_BASE|0x08)
#define EVENT_TABLE_FULL                        (EVENT_ERROR_BASE|0x09)
#define EVENT_NOT_FOUND                         (EVENT_ERROR_BASE|0x0A)
#define EVENT_INVALID_EVENT                     (EVENT_ERROR_BASE|0x0B)
#define EVENT_CANNOT_WAIT_ON_REMOTE_EVENT       (EVENT_ERROR_BASE|0x0C)

/* Default component creation parameters */
#define EVENT_DEFAULT_INITIAL_NUMBER            (8)
#define EVENT_DEFAULT_GROW_NUMBER               (8)
#define EVENT_DEFAULT_MAXIMUM_NUMBER            (0) /* Unlimited */

/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _event_clear(pointer, _mqx_uint);
extern _mqx_uint _event_close(pointer);
extern _mqx_uint _event_create_component(_mqx_uint, _mqx_uint, _mqx_uint);
extern _mqx_uint _event_create_fast(_mqx_uint);
extern _mqx_uint _event_create_fast_auto_clear(_mqx_uint);
extern _mqx_uint _event_create(char _PTR_);
extern _mqx_uint _event_create_auto_clear(char _PTR_);
extern _mqx_uint _event_destroy(char _PTR_);
extern _mqx_uint _event_destroy_fast(_mqx_uint);
extern _mqx_uint _event_get_value(pointer, _mqx_uint_ptr);
extern _mqx_uint _event_get_wait_count(pointer);
extern _mqx_uint _event_open(char _PTR_, pointer _PTR_);
extern _mqx_uint _event_open_fast(_mqx_uint, pointer _PTR_);
extern _mqx_uint _event_set(pointer, _mqx_uint);
extern _mqx_uint _event_test(pointer _PTR_);
extern _mqx_uint _event_wait_all(pointer, _mqx_uint, uint_32);
extern _mqx_uint _event_wait_all_for(pointer, _mqx_uint, MQX_TICK_STRUCT_PTR);
extern _mqx_uint _event_wait_all_ticks(pointer, _mqx_uint, _mqx_uint);
extern _mqx_uint _event_wait_all_until(pointer, _mqx_uint, MQX_TICK_STRUCT_PTR);
extern _mqx_uint _event_wait_any(pointer, _mqx_uint, uint_32);
extern _mqx_uint _event_wait_any_for(pointer, _mqx_uint, MQX_TICK_STRUCT_PTR);
extern _mqx_uint _event_wait_any_ticks(pointer, _mqx_uint, _mqx_uint);
extern _mqx_uint _event_wait_any_until(pointer, _mqx_uint, MQX_TICK_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
