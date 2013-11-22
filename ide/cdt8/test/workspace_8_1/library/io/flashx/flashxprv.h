#ifndef _flashxprv_h_
#define _flashxprv_h_
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
*** File: flashxprv.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions private to the flash drivers
*** 
***
**************************************************************************
*END*********************************************************************/


/*----------------------------------------------------------------------*/
/*
**                    DATATYPE DEFINITIONS
*/
#define MQX_FLASH_INIT_ERROR   (0x1234)
#define IO_FLASH_WRITE_ENABLE  (0x4567)
#define IO_FLASH_WRITE_DISABLE (0x4321)

/*----------------------------------------------------------------------*/
/*
**                    Structure Definitions
*/


/*----------------------------------------------------------------------*/
/*
**                    FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

/* These are from flashx.c */
extern _mqx_int  _io_flashx_flush_buffer(IO_FLASHX_STRUCT_PTR); 
extern  boolean   _io_flashx_erase_sector(IO_FLASHX_STRUCT_PTR, _mqx_uint, 
   _mqx_uint, _mqx_uint);
extern  void      _io_flashx_find_correct_sectors(IO_FLASHX_STRUCT_PTR, 
   _mqx_int, _mem_size, _mqx_uint _PTR_, _mqx_uint _PTR_, _mqx_uint _PTR_, 
   _mqx_uint _PTR_, _mqx_uint _PTR_);
extern _mqx_int  _io_flashx_open(FILE_PTR, char_ptr, char_ptr);
extern _mqx_int  _io_flashx_close(FILE_PTR);
extern _mqx_int  _io_flashx_write(FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int  _io_flashx_read (FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int  _io_flashx_ioctl(FILE_PTR, _mqx_uint, pointer);
extern _mem_size _io_flashx_write_partial_sector(FILE_PTR, _mqx_uint, _mqx_uint, 
   _mem_size, _mem_size, _mqx_uint, uchar_ptr);
extern void      _io_flashx_wait_us(_mqx_int);
   
#ifdef __cplusplus
}
#endif

#endif
/* EOF */
