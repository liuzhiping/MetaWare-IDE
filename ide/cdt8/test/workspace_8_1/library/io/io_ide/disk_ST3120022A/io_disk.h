#ifndef __io_disk_h__
#define __io_disk_h__
/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International.
***
*** File: io_disk.h
***
*** Comments: The file contains functions prototype, defines, structure 
***           definitions of a hard disk. This file is added to address
***           CR 2283
***
**************************************************************************
*END*********************************************************************/

/*-----------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/* Errors compatible with file system */
#define  IDE_DISK_NO_ERROR           0x00
#define  IDE_DISK_ERROR              0x02
#define  IDE_DISK_WRITE_PROTECTED    0x03
#define  IDE_DISK_SECTOR_NOT_FOUND   0x04
#define  IDE_DISK_BAD_CRC            0x08
#define  IDE_DISK_SEEK_FAILED        0x40
#define  IDE_DISK_FAILED_TO_RESPOND  0x80

/* Internal drive type */
#define IDE_DRIVE_TYPE_UNKNOWN       0
#define IDE_DRIVE_TYPE_ATA           1
#define IDE_DRIVE_TYPE_ATAPI         2

#define IDE_DISK_READ_ERROR          0x200
#define IDE_DISK_WRITE_ERROR         0x300
#define IDE_DISK_HARDWARE_ERROR      0x400
#define IDE_DISK_INVALID_SECTOR      0x500
#define IDE_DISK_DIAGNOSTIC_ERROR    0x600

#define IDE_DISK_DATA_REG            BSP_IDE_REGISTER_OFFSET

/*
** IO_DISK IOCTL calls
*/
#define IO_IDE_DISK_IOCTL_GET_TOTAL_SIZE           (0x0102)
#define IO_IDE_DISK_IOCTL_GET_DEVICE_ERROR         (0x0103)
#define IO_IDE_DISK_IOCTL_GET_NUM_SECTORS          (0x0104)
#define IO_IDE_DISK_IOCTL_GET_SECTOR_SIZE          (0x0105)
#define IO_IDE_DISK_IOCTL_DEVICE_IDLE              (0x0106)
#define IO_IDE_DISK_IOCTL_DEVICE_IDENTIFY          (0x0107)
#define IO_IDE_DISK_IOCTL_DEVICE_CHECK_POWER_MODE  (0x0108)
#define IO_IDE_DISK_IOCTL_DEVICE_FORMAT_TRACK      (0x0109)
#define IO_IDE_DISK_IOCTL_DEVICE_FLUSH_CACHE       (0x010A)
#define IO_IDE_DISK_IOCTL_DEVICE_IDLE_IMMEDIATE    (0x010B)
#define IO_IDE_DISK_IOCTL_DEVICE_INIT_PARAM        (0x010C)
#define IO_IDE_DISK_IOCTL_DEVICE_RECALIBRATE       (0x010D)
#define IO_IDE_DISK_IOCTL_DEVICE_DIAGNOSTIC        (0x010E)
#define IO_IDE_DISK_IOCTL_DEVICE_STANDBY           (0x010F)
#define IO_IDE_DISK_IOCTL_DEVICE_STANDBY_IMMEDIATE (0x0110)
#define IO_IDE_DISK_IOCTL_DEVICE_SLEEP             (0x0111)
#define IO_IDE_DISK_IOCTL_DEVICE_MEDIA_LOCK        (0x0112)
#define IO_IDE_DISK_IOCTL_DEVICE_MEDIA_UNLOCK      (0x0113)

/* These are the result of check power mode */
#define IO_IDE_DISK_STANDBY_MODE      (0x00) // device is in Standby mode.
#define IO_IDE_DISK_IDLE_MODE         (0x80) // device is in Idle mode.
#define IO_IDE_DISK_ACTIVE_IDLE_MODE  (0xFF) // device is in Active mode or Idle mode.   

/* These are the result of execute device diagnostic */
#define IO_IDE_DISK_DEVICE0_PASSED_DEVICE1_PASSED (0x01) // Device 0 passed, Device 1 passed or not present
#define IO_IDE_DISK_DEVICE0_FAILED_DEVICE1_PASSED (0x00) // Device 0 failed, Device 1 passed or not present
#define IO_IDE_DISK_DEVICE0_PASSED_DEVICE1_FAILED (0x81) // Device 0 passed, Device 1 failed
#define IO_IDE_DISK_DEVICE0_FAILED_DEVICE1_FAILED (0x80) // Device 0 failed, Device 1 failed


/*-----------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/


typedef struct io_disk_info_struct
{

   /* Pointer to disk drive */   
   volatile pointer         INIT;

   /* Handle for IDE calls */
   FILE_PTR                 IDE_STREAM;
   
   /* Drive number to associate with this device */
   uint_32                  DRIVE;
   
   /* Sector size in bytes */
   uint_32                  SECTOR_SIZE;

   /* The number of sectors in the device */
   uint_64                  NUM_SECTORS;

   /* Total size of the device in bytes */
   uint_64                  SIZE;

   /* The number of sectors per track */
   uint_32                  SECTORS_PER_TRACK;
   
   /* The number of heads */
   uint_32                  NUMBER_OF_HEADS;
   
   /* The number of tracks */
   uint_32                  NUMBER_OF_TRACKS;

   /* Light weight semaphore struct */
   LWSEM_STRUCT             LWSEM;

   /* The address of temp buffer */
   uchar_ptr                TEMP_BUFF_PTR;

   /* The current error code for the device */
   uint_32                  ERROR_CODE;
                  
} IO_DISK_INFO_STRUCT, _PTR_ IO_DISK_INFO_STRUCT_PTR;



/*-----------------------------------------------------------------------*/
/*
**                          PROTOTYPE DECLARATIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_uint _io_disk_install(char_ptr);
extern _mqx_int _io_disk_open(FILE _PTR_, char_ptr, char_ptr);
extern _mqx_int _io_disk_close(FILE _PTR_);
extern _mqx_int _io_disk_write(FILE _PTR_, char_ptr, _mqx_int);
extern _mqx_int _io_disk_read (FILE _PTR_, char_ptr, _mqx_int);
extern _mqx_int _io_disk_ioctl(FILE _PTR_, _mqx_uint, uint_32_ptr);

#ifdef __cplusplus
}
#endif

#endif

/* EOF */