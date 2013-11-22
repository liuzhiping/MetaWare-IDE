#ifndef __name_h__
#define __name_h__ 1
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
*** File: name.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
***  name component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Error codes */

#define NAME_TABLE_FULL             (NAME_ERROR_BASE|0x00)
#define NAME_EXISTS                 (NAME_ERROR_BASE|0x01)
#define NAME_NOT_FOUND              (NAME_ERROR_BASE|0x02)
#define NAME_TOO_LONG               (NAME_ERROR_BASE|0x03)
#define NAME_TOO_SHORT              (NAME_ERROR_BASE|0x04)

/* The maximum name size for a name component name */
#define NAME_MAX_NAME_SIZE          (32)

/* Default component creation parameters */
#define NAME_DEFAULT_INITIAL_NUMBER (8)
#define NAME_DEFAULT_GROW_NUMBER    (8)
#define NAME_DEFAULT_MAXIMUM_NUMBER (0) /* Unlimited */

/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _name_create_component(_mqx_uint, _mqx_uint, _mqx_uint);
extern _mqx_uint _name_add(char_ptr, _mqx_max_type);
extern _mqx_uint _name_delete(char_ptr);
extern _mqx_uint _name_find(char_ptr, _mqx_max_type_ptr);
extern _mqx_uint _name_find_by_number(_mqx_max_type, char_ptr);
extern _mqx_uint _name_test(pointer _PTR_, pointer _PTR_);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
