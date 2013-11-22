#ifndef __lwlogprv_h__
#define __lwlogprv_h__ 1
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
*** File: lwlogprv.h
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

#define LWLOG_VALID          ((_mqx_uint)(0x6C776C67))   /* "lwlg" */

/* Control bits in the control flags */
#define LWLOG_ENABLED        (0x1000)

/* Types of logs */
#define LWLOG_STATIC         (1)
#define LWLOG_DYNAMIC        (2)

/*--------------------------------------------------------------------------*/
/*                      DATA STRUCTURE DEFINITIONS                          */

/* 
** LOG HEADER STRUCT
**
** This structure is stored at the front of each log to provide
** information about the current state of the log
*/
typedef struct lwlog_header_struct
{

   /* Control flags for the log */
   _mqx_uint                FLAGS;     

   /* The sequence number for next write */
   _mqx_uint                NUMBER;    

   /* The number of entries currently in use */
   _mqx_uint                CURRENT_ENTRIES;

   /* The maximum number of log entries */
   _mqx_uint                MAX_ENTRIES;

   /* How many read nexts in row have been done */
   _mqx_uint                READS;

   /* The type of log */
   _mqx_uint                TYPE;

   LWLOG_ENTRY_STRUCT_PTR   WRITE_PTR;
   LWLOG_ENTRY_STRUCT_PTR   READ_PTR;
   LWLOG_ENTRY_STRUCT_PTR   OLDEST_PTR;

   LWLOG_ENTRY_STRUCT       FIRST_ENTRY;

} LWLOG_HEADER_STRUCT, _PTR_ LWLOG_HEADER_STRUCT_PTR;



/* 
** LW LOG COMPONENT STRUCT
**
** This structure is used to store information 
** required for log retrieval. Its address is stored in the kernel component 
** field of the kernel data structure
*/
typedef struct lwlog_component_struct
{
   /* A validation stamp to verify structure correctness */
   _mqx_uint               VALID;

   /* The address of the log headers */
   LWLOG_HEADER_STRUCT_PTR LOGS[LOG_MAXIMUM_NUMBER];
   
} LWLOG_COMPONENT_STRUCT, _PTR_ LWLOG_COMPONENT_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _lwlog_create_internal(_mqx_uint, _mqx_uint, _mqx_uint, 
   LWLOG_HEADER_STRUCT_PTR);
extern _mqx_uint _lwlog_write_internal(_mqx_uint, _mqx_max_type, _mqx_max_type,
    _mqx_max_type, _mqx_max_type, _mqx_max_type, _mqx_max_type, _mqx_max_type);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
