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
*** File: io_disk.c
***
*** Comments:
***    This file contains the IDE Hard Disk driver functions. This file is
***    added to address CR 2283
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "fio_prv.h"
#include "io_prv.h"
#include "ata.h"
#include "io_disk.h"
#include "io_diskprv.h"
#include "io_ideprv.h"

#if BSP_USE_IDE

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_install
* Returned Value   : _mqx_uint a task error code or MQX_OK
* Comments         :
*    Install a disk driver.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _io_disk_install
   (
      /* [IN] A string that identifies the device for fopen */
      char_ptr     identifier
  
   )
{ /* Body */

   IO_DISK_INFO_STRUCT_PTR         info_ptr;
      
   info_ptr = (IO_DISK_INFO_STRUCT_PTR)_mem_alloc_system_zero((uint_32)sizeof(IO_DISK_INFO_STRUCT));
   
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (info_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   /* Only device 0 is supported, AA4 Aurora IDE interface supports only one device. */
   info_ptr->DRIVE         = 0; 
   
   info_ptr->SECTOR_SIZE   = ATA_SECTOR_SIZE;
   info_ptr->TEMP_BUFF_PTR = NULL;
   info_ptr->ERROR_CODE    = IO_OK;

   _lwsem_create(&info_ptr->LWSEM, 1L);

   return (_io_dev_install(identifier,
      _io_disk_open,
      _io_disk_close,
      _io_disk_read,
      _io_disk_write,
      _io_disk_ioctl,
      (pointer)info_ptr)); 

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_open
* Returned Value   : a null pointer
* Comments         : Opens and initializes IDE disk driver.
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_disk_open
   (
      /* [IN] the file handle for the device being opened */
      FILE_DEVICE_STRUCT_PTR   fd_ptr,
       
      /* [IN] the remaining portion of the name of the device */
      char_ptr                 open_name_ptr,

      /* [IN] the flags to be used during operation:
      ** blockmode
      */
      char_ptr                 flags
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR             io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR          info_ptr;
   IDE_DISK_REG_STRUCT_PTR          disk_ptr;
   IO_IDE_INIT_STRUCT_PTR           ide_init_ptr;
   ATA_IDENTIFY_DEVICE_STRUCT_PTR   identify_ptr;
   uchar_ptr                        temp_ptr;
   uchar_ptr                        tmp_ptr;
   uint_32                          error;
   uint_64                          num_sectors;
   uchar                            end_defn[] = {2, 0};
   FILE_PTR                         ide_stream = (FILE_PTR)flags;
   volatile uint_32                 temp;

   info_ptr = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;

#if MQX_CHECK_ERRORS
   if (ide_stream == NULL) {
      /* No device open */
      return(IO_ERROR);
   } /* Endif */
#endif

   /* 
   ** Allocate a ram buffer with the size of one sector to be used when reading/writing 
   ** partial sectors -- this buffer is also used in this function for the identify info 
   */
   temp_ptr = (uchar_ptr)_mem_alloc_system(info_ptr->SECTOR_SIZE);

#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (temp_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif   

   info_ptr->TEMP_BUFF_PTR = temp_ptr;   

   /* Save the IDE stream */
   info_ptr->IDE_STREAM = ide_stream;

   /* Save the IDE initialization struct into the info struct */
   ide_init_ptr = (IO_IDE_INIT_STRUCT_PTR)(info_ptr->IDE_STREAM->DEV_PTR->DRIVER_INIT_PTR);   

   /* Get memory locations of the IDE */
   /* Register memory */
   temp = ATA_REG_BASE;
   _io_ioctl(ide_stream, IO_IDE_IOCTL_GET_BASE_ADDRESS, (pointer)&temp);
   info_ptr->INIT = (volatile pointer)(temp + BSP_IDE_REGISTER_OFFSET);

   /* Perform a soft reset on the device */
   error = _io_disk_reset(info_ptr);
   if (error != IDE_DISK_NO_ERROR) {
      return(IO_ERROR);
   } /* Endif */

   /* Get information about the device */
   identify_ptr = (ATA_IDENTIFY_DEVICE_STRUCT_PTR)temp_ptr;
   _io_disk_identify_device(info_ptr, (uchar_ptr)identify_ptr);

   /* Store the info in the state structure */
#if (PSP_ENDIAN == MQX_BIG_ENDIAN)
   _mem_swap_endian((uchar_ptr)end_defn, &(identify_ptr->NUMBER_CYLINDERS));
   _mem_swap_endian((uchar_ptr)end_defn, &(identify_ptr->SECTORS_PER_TRACK));
   _mem_swap_endian((uchar_ptr)end_defn, &(identify_ptr->NUMBER_HEADS));
#endif
  
   /* Save disk information */  
   info_ptr->NUMBER_OF_HEADS   = identify_ptr->NUMBER_HEADS;
   info_ptr->NUMBER_OF_TRACKS  = identify_ptr->NUMBER_CYLINDERS;
   info_ptr->SECTORS_PER_TRACK = identify_ptr->SECTORS_PER_TRACK;
   
   /*
   ** The total disk size is simply the product of the total number of sectors 
   ** (number of cylinders x number of heads x number of sectors/track) with 
   ** 512 (the sector size).
   */   
   num_sectors =  info_ptr->NUMBER_OF_TRACKS * info_ptr->SECTORS_PER_TRACK *
                  info_ptr->NUMBER_OF_HEADS;
   
   info_ptr->NUM_SECTORS = num_sectors;  
   info_ptr->SIZE        = (uint_64) (num_sectors * (info_ptr->SECTOR_SIZE));

   return(MQX_OK); 
     
} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_close
* Returned Value   : ERROR CODE
* Comments         : Closes disk driver
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_disk_close
   (
      /* [IN] the file handle for the device being closed */
      FILE_DEVICE_STRUCT_PTR fd_ptr
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR info_ptr = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   _mqx_uint               result = MQX_OK;

   result = _mem_free(info_ptr->TEMP_BUFF_PTR);
   if(result == MQX_OK ) {
      info_ptr->TEMP_BUFF_PTR = NULL;
   } /* Endif */

   return ((_mqx_int)result);

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_read
* Returned Value   : number of characters read
* Comments         : Reads data from IDE disk driver
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_disk_read
   (
      /* [IN] the file handle for the device */
      FILE_DEVICE_STRUCT_PTR   fd_ptr,

      /* [IN/OUT] where the characters are to be stored */
      char_ptr                 data_ptr,

      /* [IN] the number of characters to input */
      _mqx_int                 bytes_to_read
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR info_ptr = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   uchar_ptr               dst_ptr;
    int_32                 results;
   uint_32                 start_sector;
   uint_32                 end_sector;
   uint_32                 remaining_bytes_to_read;
   uint_32                 offset;
   uint_32                 remaining_bytes;
   uint_32                 num_sectors;
   uint_32                 sectors_to_read;
   uint_32                 i;
   uint_32                 temp;

   /* Wait for drive availability */   
   _lwsem_wait(&info_ptr->LWSEM);

   if (fd_ptr->LOCATION > info_ptr->SIZE) {
      fd_ptr->ERROR = IO_ERROR_READ_ACCESS;
      _lwsem_post(&info_ptr->LWSEM);
      return(IO_ERROR);
   } else {
      if ((bytes_to_read + fd_ptr->LOCATION) > info_ptr->SIZE) {
         fd_ptr->ERROR = IO_ERROR_READ_ACCESS;
         bytes_to_read = (int_32)(info_ptr->SIZE - fd_ptr->LOCATION + 1);
      } /* Endif */
      start_sector = fd_ptr->LOCATION / info_ptr->SECTOR_SIZE; 
      end_sector = (fd_ptr->LOCATION + bytes_to_read - 1) / info_ptr->SECTOR_SIZE;
      num_sectors = end_sector - start_sector + 1;
      remaining_bytes_to_read = bytes_to_read;
      dst_ptr = data_ptr;
      
      /*
      ** We have three conditions:
      **   1. The read is contained in one sector
      **   2. The read is contained in two adjacent sectors
      **   3. The read spans many sectors   
      **
      ** In case 1, it is possible to have a partial sector. 
      ** In case 2, it is possible that both the end and the start are only 
      **  partial sectors.
      ** In case 3(really a subset of case 2), the start and end may be partial
      **  but the others will all be complete sectors.
      */
      
      offset = fd_ptr->LOCATION - info_ptr->SECTOR_SIZE * start_sector;
           
      if (offset > 0) {
         num_sectors--;
         remaining_bytes = info_ptr->SECTOR_SIZE - offset;
         if (remaining_bytes > remaining_bytes_to_read) {
            remaining_bytes = remaining_bytes_to_read;
         }/* Endif */
         results = _io_disk_read_partial_sector(fd_ptr, start_sector, offset,
                                               remaining_bytes, dst_ptr);
         if (results == IO_ERROR) {
            fd_ptr->ERROR = IO_ERROR_READ;
            _lwsem_post(&info_ptr->LWSEM);
            return(results);
         } else {
            fd_ptr->LOCATION += results;
            dst_ptr = data_ptr + results;
            remaining_bytes_to_read -= results;
            start_sector++;
         } /* Endif */
      } /* Endif */
      
      if (num_sectors > 1) {
         /*
         ** Read the middle sectors (if any )
         */
         
         /* Calculate number of sectors to read but do not include last one */
         sectors_to_read = num_sectors - 1;
         
         /* Read all the sectors, one at a time */
         for (i = 0; i < sectors_to_read; i++) {
            if (_io_disk_read_sector(info_ptr, start_sector + i, dst_ptr) == IDE_DISK_NO_ERROR) {
               results += info_ptr->SECTOR_SIZE;
               fd_ptr->LOCATION += info_ptr->SECTOR_SIZE;
               num_sectors--;
               dst_ptr += info_ptr->SECTOR_SIZE;
               remaining_bytes_to_read -= info_ptr->SECTOR_SIZE;
            } else {
               fd_ptr->ERROR = IO_ERROR_READ;
               _lwsem_post(&info_ptr->LWSEM);
               return(IO_ERROR);
            } /* Endif */
         } /* Endfor */

      } /* Endif */
      
      if (num_sectors) {
         /* Read the last sector */
         offset  = 0;
         remaining_bytes = remaining_bytes_to_read; 
         results  = _io_disk_read_partial_sector(fd_ptr, end_sector, offset, 
                                                remaining_bytes, dst_ptr);
         if (results == IO_ERROR) {
            fd_ptr->ERROR = IO_ERROR_READ;
            _lwsem_post(&info_ptr->LWSEM);
            return(results);
         } else {
            fd_ptr->LOCATION += results;
         } /* Endif */
      } /* Endif */

   } /* Endif */

   _lwsem_post(&info_ptr->LWSEM);
   
   return(bytes_to_read);
   
} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_write
* Returned Value   : number of characters written
* Comments         : Writes data to the fdv_ram device
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_disk_write
   (
      /* [IN] the file handle for the device */
      FILE_DEVICE_STRUCT_PTR  fd_ptr,

      /* [IN] where the characters are */
      char_ptr                data_ptr,

      /* [IN] the number of characters to output */
      _mqx_int                bytes_to_write
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR info_ptr = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   uchar_ptr               src_ptr;
    int_32                 results;
   uint_32                 remaining_bytes_to_write;
   uint_32                 start_sector;
   uint_32                 end_sector;
   uint_32                 offset;
   uint_32                 remaining_bytes;
   uint_32                 num_sectors;
   uint_32                 sectors_to_write;
   uint_32                 i;
   uint_32                 temp;
					    
   /* Wait for drive availability */
   _lwsem_wait(&info_ptr->LWSEM);

   if (fd_ptr->LOCATION > info_ptr->SIZE) 
   {
      fd_ptr->ERROR = IO_ERROR_WRITE_ACCESS;
      _lwsem_post(&info_ptr->LWSEM);
      return(IO_ERROR);
   } else {
      if ((bytes_to_write + fd_ptr->LOCATION) > info_ptr->SIZE) {
          fd_ptr->ERROR = IO_ERROR_WRITE_ACCESS;
          bytes_to_write = (int_32)(info_ptr->SIZE - fd_ptr->LOCATION + 1);
      } /* Endif */
      
      start_sector = fd_ptr->LOCATION / info_ptr->SECTOR_SIZE; 
      end_sector = (fd_ptr->LOCATION + bytes_to_write - 1) / info_ptr->SECTOR_SIZE;
      num_sectors = end_sector - start_sector + 1;
      remaining_bytes_to_write = bytes_to_write;
      src_ptr = data_ptr;
      
      /*
      ** We have three conditions:
      **   1. The write is contained in one sector
      **   2. The write is contained in two adjacent sectors
      **   3. The write spans many sectors   
      **
      ** In case 1, it is possible to have a partial sector. 
      ** In case 2, it is possible that both the end and the start are only 
      **  partial sectors.
      ** In case 3(really a subset of case 2), the start and end maybe partial
      **  but the others will all be complete sectors.
      */
      
      offset = fd_ptr->LOCATION - info_ptr->SECTOR_SIZE * start_sector;
           
      if (offset > 0) {
         num_sectors--;
         remaining_bytes = info_ptr->SECTOR_SIZE - offset;
         if (remaining_bytes > remaining_bytes_to_write) {
            remaining_bytes = remaining_bytes_to_write;
         }/* Endif */
         results = _io_disk_write_partial_sector(fd_ptr, start_sector, offset,
                                                remaining_bytes, src_ptr);
         if (results == IO_ERROR)
         {
            fd_ptr->ERROR = IO_ERROR_WRITE;
            _lwsem_post(&info_ptr->LWSEM);
            return(results);
         } else {
            fd_ptr->LOCATION += results;
            src_ptr = data_ptr + results;
            remaining_bytes_to_write -= results;
            start_sector++;
         } /* Endif */
      } /* Endif */
      
      if (num_sectors > 1) {
         /*
         ** Write the middle sectors (if any )
         */
         
         /* Calculate number of sectors to write but do not include last one */
         sectors_to_write = num_sectors - 1;
         
         /* Calculate the number of bytes need to write in this case */
         for (i = 0; i < sectors_to_write; i++) {
            if (_io_disk_write_sector(info_ptr, start_sector + i, src_ptr) == IDE_DISK_NO_ERROR) {
               results += info_ptr->SECTOR_SIZE;
               fd_ptr->LOCATION += info_ptr->SECTOR_SIZE;
               num_sectors--;
               src_ptr += info_ptr->SECTOR_SIZE;
               remaining_bytes_to_write -= info_ptr->SECTOR_SIZE;
            } else {
               fd_ptr->ERROR = IO_ERROR_WRITE;
               _lwsem_post(&info_ptr->LWSEM);
               return(IO_ERROR);
            } /* Endif */
         } /* Endfor */

      } /* Endif */
      
      if (num_sectors) {
         /* Write the last sector */
         offset  = 0;
         remaining_bytes = remaining_bytes_to_write;
         results  = _io_disk_write_partial_sector(fd_ptr, end_sector, offset, 
                                                 remaining_bytes, src_ptr);
         if (results == IO_ERROR) {
            fd_ptr->ERROR = IO_ERROR_WRITE;
            _lwsem_post(&info_ptr->LWSEM);
            return(results);
         } else {
            fd_ptr->LOCATION += results;
         } /* Endif */
      } /* Endif */
      
   } /* Endif */
   
   _lwsem_post(&info_ptr->LWSEM);

   return(bytes_to_write);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_ioctl
* Returned Value   : _mqx_int
* Comments         :
*    Returns result of ioctl operation.
*
*END*********************************************************************/

_mqx_int _io_disk_ioctl
   (
      /* [IN] the file handle for the device */
      FILE_DEVICE_STRUCT_PTR fd_ptr,

      /* [IN] the ioctl command */
      _mqx_uint               cmd,

      /* [IN/OUT] the ioctl parameters */
      uint_32_ptr             param_ptr
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR info_ptr = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   int_32                  result = MQX_OK;

   switch (cmd) {
      
      case IO_IOCTL_DEVICE_IDENTIFY:
         param_ptr[0] = IO_DEV_TYPE_PHYS_IDE;
         param_ptr[1] = IO_DEV_TYPE_LOGICAL_MFS;
         param_ptr[2] = IO_DEV_ATTR_ERASE | IO_DEV_ATTR_POLL |
                        IO_DEV_ATTR_WRITE | IO_DEV_ATTR_READ |
                        IO_DEV_ATTR_SEEK;
         break;
      case IO_IOCTL_SEEK:
         result = _io_disk_seek(info_ptr);      
         break;               
      case IO_IOCTL_GET_STATS:
         *param_ptr = _io_disk_get_stats(info_ptr);
         break;                
      case IO_IDE_DISK_IOCTL_DEVICE_IDENTIFY:
         result = _io_disk_identify_device(info_ptr, (uchar_ptr)param_ptr);                  
         break;
      case IO_IDE_DISK_IOCTL_GET_NUM_SECTORS:
         *param_ptr = info_ptr->NUM_SECTORS;
         break;
      case IO_IDE_DISK_IOCTL_GET_SECTOR_SIZE:
         *param_ptr = info_ptr->SECTOR_SIZE;
         break;
      case IO_IDE_DISK_IOCTL_GET_TOTAL_SIZE:
         *param_ptr = info_ptr->SIZE;
         break;
      case IO_IDE_DISK_IOCTL_GET_DEVICE_ERROR:
         *param_ptr = _io_disk_get_error(info_ptr);      
         break;               
      case IO_IDE_DISK_IOCTL_DEVICE_STANDBY_IMMEDIATE:
         result = _io_disk_spin_down(info_ptr);      
         break;               
      case IO_IDE_DISK_IOCTL_DEVICE_IDLE_IMMEDIATE:
         result = _io_disk_spin_up(info_ptr);      
         break;               
      case IO_IDE_DISK_IOCTL_DEVICE_IDLE:
         result = _io_disk_idle(info_ptr);      
         break;               
      case IO_IDE_DISK_IOCTL_DEVICE_STANDBY:
         result = _io_disk_standby(info_ptr);      
         break;               
      case IO_IDE_DISK_IOCTL_DEVICE_CHECK_POWER_MODE:
         *param_ptr = _io_disk_check_pwr_mode(info_ptr);
         if ((*param_ptr &IO_IDE_DISK_STANDBY_MODE) != IO_IDE_DISK_STANDBY_MODE) {
            if ((*param_ptr & IO_IDE_DISK_IDLE_MODE) != IO_IDE_DISK_IDLE_MODE) {
                if ((*param_ptr & IO_IDE_DISK_ACTIVE_IDLE_MODE ) != IO_IDE_DISK_ACTIVE_IDLE_MODE) {
                   result = IDE_DISK_HARDWARE_ERROR;                
                } /* Endif */
            } /* Endif */
         } /* Endif */
         break;               
      case IO_IOCTL_FLUSH_OUTPUT:
         result = _io_disk_flush_cache(info_ptr);      
         break;               
      case IO_IDE_DISK_IOCTL_DEVICE_DIAGNOSTIC:
         result = _io_disk_diagnos(info_ptr, param_ptr);
         break;               
      default:
         result = IO_ERROR_INVALID_IOCTL_CMD;
         break;
   
   } /* Endswitch */
   
   return(result);

} /* Endbody */

#endif

/* EOF */
