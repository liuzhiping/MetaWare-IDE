#ifndef __io_mem_h__
#define __io_mem_h__
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
*** File: io_mem.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions private to the fdv Ramdisk drivers
*** 
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                          CONSTANT DEFINITIONS
*/


/*
** IO_MEM IOCTL calls
*/
#define IO_MEM_IOCTL_GET_BASE_ADDRESS     (0x0101)
#define IO_MEM_IOCTL_GET_TOTAL_SIZE       (0x0102)
#define IO_MEM_IOCTL_GET_DEVICE_ERROR     (0x0103)

#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_uint _io_mem_install(char_ptr, pointer, _file_size);

#ifdef __cplusplus
}
#endif

#endif

/* EOF */
