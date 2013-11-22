#ifndef __iomemprv_h__
#define __iomemprv_h__
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
*** File: iomemprv.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions private to the ram disk.
*** 
***
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                          CONSTANT DEFINITIONS
*/
/* Start CR 810 */
/* These are used to determine how memory was allocated for the RAM drive */
#define MEM_TYPE_DYNAMIC   (1)
#define MEM_TYPE_STATIC    (2)

/* Define the block size when driver is opened in block mode */
/* Defines size as a power of 2 */
#define IO_MEM_BLOCK_SIZE_POWER   (9)
#define IO_MEM_BLOCK_SIZE         (1 << IO_MEM_BLOCK_SIZE_POWER)

/* Properties of device */
#define IO_MEM_ATTRIBS  (IO_DEV_ATTR_READ | IO_DEV_ATTR_REMOVE | \
   IO_DEV_ATTR_SEEK | IO_DEV_ATTR_WRITE)
/* End   CR 810 */

/*----------------------------------------------------------------------*/
/*
**                    DATATYPE DEFINITIONS
*/

/*
** IO_MEM STRUCT
**
** The address of this structure is used to maintain ramdisk specific 
** information.
*/
typedef struct io_mem_struct
{
   /* Type of memory, dynamic or static */
   _mqx_uint        TYPE;

   /* Address of the fdv_ram device */
   uchar_ptr        BASE_ADDR;

   /* The total size of memory in the ram disk */
   _file_size       SIZE;

   /* Light weight semaphore struct */
   LWSEM_STRUCT     LWSEM;
   
   /* The current error code for the device */
   _mqx_uint        ERROR_CODE;

   /* Start CR 810 */
   /* The current mode for the device */
   boolean          BLOCK_MODE;

   /* The number of blocks for the device */
   _file_size       NUM_BLOCKS;
   /* End   CR 810 */

} IO_MEM_STRUCT, _PTR_ IO_MEM_STRUCT_PTR;


/* Internal functions to IO_MEM */
#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_int _io_mem_open(FILE_DEVICE_STRUCT_PTR, char_ptr, char_ptr);
extern _mqx_int _io_mem_close(FILE_DEVICE_STRUCT_PTR);
extern _mqx_int _io_mem_write(FILE_DEVICE_STRUCT_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_mem_read (FILE_DEVICE_STRUCT_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_mem_ioctl(FILE_DEVICE_STRUCT_PTR, _mqx_uint, pointer);
extern _mqx_int _io_mem_uninstall(IO_DEVICE_STRUCT_PTR);

/* Start CR 810 */
extern _mqx_int _io_mem_write_blocks(FILE_DEVICE_STRUCT_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_mem_read_blocks (FILE_DEVICE_STRUCT_PTR, char_ptr, _mqx_int);
/* End   CR 810 */

#ifdef __cplusplus
}
#endif

#endif

/* EOF */
