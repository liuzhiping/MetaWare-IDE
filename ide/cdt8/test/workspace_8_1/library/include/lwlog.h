#ifndef __lwlog_h__
#define __lwlog_h__ 1
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
*** File: lwlog.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
***  Log component.
*** 
***************************************************************************
*END**********************************************************************/

#include "log.h"

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Maximum number of data elements allowed in each entry (not including the header */
/* Start CR 238 */
/* #define LWLOG_MAXIMUM_DATA_ENETRIES (7) */
#define LWLOG_MAXIMUM_DATA_ENTRIES (7)
/* End CR 238 */

/*--------------------------------------------------------------------------*/
/*                        DATATYPE DECLARATIONS                             */

/* 
** LWLOG ENTRY STRUCT
**
** This structure is the front part of every log entry
*/
typedef struct lwlog_entry_struct
{

   /* The sequence number for this entry */
   _mqx_uint        SEQUENCE_NUMBER;

   /* The time stamp units are configurable */
#if MQX_LWLOG_TIME_STAMP_IN_TICKS == 0
   /* The time at which this entry was written - time */
   uint_32          SECONDS;
   uint_32          MILLISECONDS;
   uint_32          MICROSECONDS;
#else
   /* The time at which this entry was written - ticks */
   MQX_TICK_STRUCT  TIMESTAMP;
#endif

   /* Start CR 238 */
   /* _mqx_max_type    DATA[LWLOG_MAXIMUM_DATA_ENETRIES]; */
   _mqx_max_type    DATA[LWLOG_MAXIMUM_DATA_ENTRIES];
   /* End CR 238 */

   struct lwlog_entry_struct _PTR_ NEXT_PTR;

} LWLOG_ENTRY_STRUCT, _PTR_ LWLOG_ENTRY_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mem_size _lwlog_calculate_size(_mqx_uint);
extern _mqx_uint _lwlog_create(_mqx_uint, _mqx_uint, _mqx_uint);
extern _mqx_uint _lwlog_create_at(_mqx_uint, _mqx_uint, _mqx_uint, pointer);
extern _mqx_uint _lwlog_create_component(void);
extern _mqx_uint _lwlog_destroy(_mqx_uint);
extern _mqx_uint _lwlog_disable(_mqx_uint);
extern _mqx_uint _lwlog_enable(_mqx_uint);
extern _mqx_uint _lwlog_read(_mqx_uint, _mqx_uint, LWLOG_ENTRY_STRUCT_PTR);
extern _mqx_uint _lwlog_reset(_mqx_uint);
extern _mqx_uint _lwlog_test(_mqx_uint _PTR_);
extern _mqx_uint _lwlog_write(_mqx_uint, _mqx_max_type, _mqx_max_type, _mqx_max_type, _mqx_max_type,
   _mqx_max_type, _mqx_max_type, _mqx_max_type);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
