#ifndef __mqx_str_h__
#define __mqx_str_h__ 1
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
*** File: mqx_str.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
*** mqx string utilities.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */


/*--------------------------------------------------------------------------*/
/*                        DATATYPE DECLARATIONS                             */


/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void      _str_mqx_uint_to_hex_string(_mqx_uint, char_ptr);
extern _mqx_uint _strnlen(char_ptr, _mqx_uint);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
