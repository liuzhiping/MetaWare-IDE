#ifndef _flashx_h_
#define _flashx_h_
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
*** File: flashx.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions specific for the flash drivers
*** 
***
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                          CONSTANT DEFINITIONS
*/

/*
** Flash IOCTL calls
*/
#define FLASH_IOCTL_GET_BASE_ADDRESS     (0x0101)
#define FLASH_IOCTL_GET_NUM_SECTORS      (0x0102)
#define FLASH_IOCTL_GET_SECTOR_SIZE      (0x0103)
#define FLASH_IOCTL_GET_WIDTH            (0x0104)
#define FLASH_IOCTL_GET_SECTOR_BASE      (0x0105)
#define FLASH_IOCTL_GET_BLOCK_GROUPS     (0x0106)
#define FLASH_IOCTL_GET_BLOCK_MAP        (0x0107)
#define FLASH_IOCTL_FLUSH_BUFFER         (0x0108)
#define FLASH_IOCTL_ENABLE_BUFFERING     (0x0109)
#define FLASH_IOCTL_DISABLE_BUFFERING    (0x010A)
#define FLASH_IOCTL_ERASE_SECTOR         (0x010B)
#define FLASH_IOCTL_ERASE_CHIP           (0x010C)

#define IO_FLASH_BUFFER_ENABLED          (0x00000800)

/*----------------------------------------------------------------------*/
/*
**                    Structure Definitions
*/

/*
** BLOCK INFO STRUCT
**
** This structure is used to map the blocks of odd size sectors on a flash chip
*/

typedef struct flashx_block_info_struct
{
   /* The number of sectors with the same size in this block */
   _mqx_uint        NUM_SECTORS;
   
   /* Start address of the block of same size sectors */
   _mem_size        START_ADDR;

   /* The size of the sectors in this block */
   _mem_size        SECT_SIZE;

} FLASHX_BLOCK_INFO_STRUCT, _PTR_ FLASHX_BLOCK_INFO_STRUCT_PTR;

/*
** FLASHX STRUCT
**
** The address of this structure is used to maintain flash specific 
** information.
*/

typedef struct io_flashx_struct
{
   /* The function to call to erase a sector on the device */
   boolean (_CODE_PTR_           SECTOR_ERASE)(struct io_flashx_struct _PTR_, 
      uchar_ptr, _mem_size);

   /* The function to call to program a sector on the device */
   boolean (_CODE_PTR_           SECTOR_PROGRAM)(struct io_flashx_struct _PTR_, 
      uchar_ptr, uchar_ptr, _mem_size);
   
   /* The function to call to erase the entire device */
   boolean (_CODE_PTR_           CHIP_ERASE)(struct io_flashx_struct _PTR_);
   
   /* The function to call to initialize the device */
   boolean (_CODE_PTR_           INIT)(struct io_flashx_struct _PTR_);
   
   /* The function to call to disable the device */
   void (_CODE_PTR_              DEINIT)(struct io_flashx_struct _PTR_);
   
   /* The function to call to write enable or protect the device */
   boolean (_CODE_PTR_           WRITE_PROTECT)(struct io_flashx_struct _PTR_, 
      _mqx_uint);
   
   /* This struture provides a mapping of the blocks on the flash device */ 
   FLASHX_BLOCK_INFO_STRUCT_PTR  BLOCK_INFO_PTR;
   
   /* Address of the flash device */
   uchar_ptr                     BASE_ADDR;
   
   /* The number of blocks of sectors of a common size on the device */
   _mqx_uint                     BLOCK_GROUPS;
   
   /* The maximum sector size of this device */
   _mem_size                     MAX_SECT_SIZE;
   
   /* The total size of the device */
   _mem_size                     TOTAL_SIZE;
   
   /* The width of the device */
   _mqx_uint                     WIDTH;
   
   /* The number of parallel devices */
   _mqx_uint                     DEVICES;
   
   /* When finished programming, should a comparison of data be made
   ** to verify that the write worked correctly.
   */
   _mqx_uint                     WRITE_VERIFY;

   /* Next three are needed for buffering data */
   _mqx_uint                     DIRTY_DATA;
   _mqx_uint                     CURRENT_BLOCK;
   _mqx_uint                     CURRENT_SECTOR;
   
   /* Light weight semaphore struct */
   LWSEM_STRUCT                  LWSEM;
   
   /* The address of temp buffer */
   pointer                       TEMP_PTR;
   
   /* The address of erase check array */
   _mqx_uint_ptr                 ERASE_ARRAY;
   
   /* The size of the erase array */
   _mqx_uint                     ERASE_ARRAY_SIZE;
   
   /* The current error code for the device */
   _mqx_uint                     ERROR_CODE;
   
   /* The number of tasks which have access to the flash device */
   _mqx_uint                     COUNT;
   
   /* Flags */
   _mqx_uint                     FLAGS;

} IO_FLASHX_STRUCT, _PTR_ IO_FLASHX_STRUCT_PTR;


/* 
** FLASHX INIT STRUCT
**
** This structure is used to initialize a flash device.
*/

typedef struct flashx_init_struct
{

   /* A pointer to a string that identifies the device for fopen */
   char_ptr                      ID_PTR;
   
   /* The function to call to erase a flashx block */
   boolean (_CODE_PTR_           SECTOR_ERASE)(IO_FLASHX_STRUCT_PTR, uchar_ptr,
      _mem_size);

   /* The function to call to program a flash block */
   boolean (_CODE_PTR_           SECTOR_PROGRAM)(IO_FLASHX_STRUCT_PTR, 
      uchar_ptr, uchar_ptr, _mem_size);

   /* The function to call to erase the entire flash chip */
   boolean (_CODE_PTR_           CHIP_ERASE)(IO_FLASHX_STRUCT_PTR);

   /* The function to call to initialize the flash device, if needed */
   boolean (_CODE_PTR_           INIT)(IO_FLASHX_STRUCT_PTR);

   /* The function to call to disable the flash device, if needed */
   void (_CODE_PTR_              DEINIT)(IO_FLASHX_STRUCT_PTR);
   
   /* The function to call to enable or disable writing to the flash device */
   boolean (_CODE_PTR_           WRITE_PROTECT)(IO_FLASHX_STRUCT_PTR, 
      _mqx_uint);
   
   /* Pointer to an array of mappings for blocks and their sizes */
   FLASHX_BLOCK_INFO_STRUCT_PTR  MAP_PTR;
   
   /* the address of the flash */
   pointer                       BASE_ADDR; 
   
   /* the width of the device in bytes */
   _mqx_uint                     WIDTH;

   /* the number of devices in parallel */
   _mqx_uint                     DEVICES;

   /* When finished programming, should a comparison of data be made
   ** to verify that the write worked correctly.
   */
   _mqx_uint                     WRITE_VERIFY;

} FLASHX_INIT_STRUCT, _PTR_ FLASHX_INIT_STRUCT_PTR;


/*----------------------------------------------------------------------*/
/*
**                    FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif
 
extern _mqx_uint _io_flashx_install(FLASHX_INIT_STRUCT _PTR_);
extern _mqx_int  _io_flashx_uninstall(IO_DEVICE_STRUCT_PTR);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
