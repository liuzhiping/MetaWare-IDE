#ifndef __io_prv_h__
#define __io_prv_h__
/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: io_prv.h
***
*** Comments:      
***   This file includes the private definitions for the I/O subsystem.
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/


/* Flag meanings */

/* Is the stream at EOF? */
#define IO_FLAG_TEXT        (4)
#define IO_FLAG_AT_EOF      (8)

/* Maximum name check length */
#define IO_MAXIMUM_NAME_LENGTH (1024)

/*--------------------------------------------------------------------------*/
/*
**                            DATATYPE DECLARATIONS
*/

/*
** FILE DEVICE STRUCTURE
**
** This structure is used by the current I/O Subsystem to store
** state information.
** Use the same structure as the formatted I/O.
*/
typedef FILE FILE_DEVICE_STRUCT, _PTR_ FILE_DEVICE_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
**                            FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
