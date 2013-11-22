#ifndef __ionulprv_h__
#define __ionulprv_h__
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
*** File: ionulprv.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions private to the null disk.
*** 
***
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                    DATATYPE DEFINITIONS
*/


/* Internal functions to IO_NULL */
#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_int _io_null_open(FILE_PTR, char_ptr, char_ptr);
extern _mqx_int _io_null_close(FILE_PTR);
extern _mqx_int _io_null_read (FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_null_write(FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_null_ioctl(FILE_PTR, _mqx_uint, pointer);

#ifdef __cplusplus
}
#endif

#endif

/* EOF */
