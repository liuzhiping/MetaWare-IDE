#ifndef __log_h__
#define __log_h__ 1
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
*** File: log.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
***  Log component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */


/* Maximum number of logs allowed */
#define LOG_MAXIMUM_NUMBER (16)

/*
** The kernel system logging uses log number 0
*/
#define LOG_KERNEL_LOG_NUMBER      (0)

/* Configuration flags */
#define LOG_OVERWRITE              (1)

/* Types of log reads */
#define LOG_READ_NEWEST            (1)
#define LOG_READ_OLDEST            (2)
#define LOG_READ_NEXT              (3)
#define LOG_READ_OLDEST_AND_DELETE (4)

/* Error codes */

#define LOG_INVALID                (LOG_ERROR_BASE|0x00)
#define LOG_EXISTS                 (LOG_ERROR_BASE|0x01)
#define LOG_DOES_NOT_EXIST         (LOG_ERROR_BASE|0x02)
#define LOG_FULL                   (LOG_ERROR_BASE|0x03)
#define LOG_ENTRY_NOT_AVAILABLE    (LOG_ERROR_BASE|0x04)
#define LOG_DISABLED               (LOG_ERROR_BASE|0x05)
#define LOG_INVALID_READ_TYPE      (LOG_ERROR_BASE|0x06)
#define LOG_INVALID_SIZE           (LOG_ERROR_BASE|0x07)

/*--------------------------------------------------------------------------*/
/*                        DATATYPE DECLARATIONS                             */

/* 
** LOG ENTRY HEADER STRUCT
**
** This structure is the front part of every log entry
*/
typedef struct log_entry_struct
{

   /* The size of this entry in _mqx_uints */
   _mqx_uint    SIZE;

   /* The sequence number for this entry */
   _mqx_uint    SEQUENCE_NUMBER;

   /* The time at which this entry was written */
   uint_32      SECONDS;
   uint_16      MILLISECONDS;
   uint_16      MICROSECONDS;

} LOG_ENTRY_STRUCT, _PTR_ LOG_ENTRY_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _log_create(_mqx_uint, _mqx_uint, uint_32);
extern _mqx_uint _log_create_component(void);
extern _mqx_uint _log_destroy(_mqx_uint);
extern _mqx_uint _log_disable(_mqx_uint);
extern _mqx_uint _log_enable(_mqx_uint);
extern _mqx_uint _log_read(_mqx_uint, _mqx_uint, _mqx_uint, 
   LOG_ENTRY_STRUCT_PTR);
extern _mqx_uint _log_reset(_mqx_uint);
extern _mqx_uint _log_test(_mqx_uint _PTR_);
extern _mqx_uint _log_write(_mqx_uint, _mqx_uint, ... );
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
