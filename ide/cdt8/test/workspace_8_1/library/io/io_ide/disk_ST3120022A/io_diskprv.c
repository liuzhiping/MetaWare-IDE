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
*** File: io_diskprv.c
***
*** Comments:
***    This file contains the IDE Hard Disk driver private functions. This
***    file is added to address CR 2283
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
* Function Name    : _io_disk_read_sector
* Returned Value   : error code
* Comments         : 
*
* 
*END*----------------------------------------------------------------------*/

uint_32 _io_disk_read_sector
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR    info_ptr,

      /* [IN] The sector number to read */
      uint_32                    sector,

      /* [IN] Location to read data into */
      uchar_ptr                  data_ptr 
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR      disk_ptr;
   volatile uint_16 _PTR_       data16_ptr;
   uint_32                      data16_size;
   uint_32                      i;
   uchar                        tmp;

   /* Check the destination pointer */
   if (data_ptr == 0) {
      return(MQX_INVALID_POINTER);       
   } /* Endif */
   
   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   if (sector > info_ptr->NUM_SECTORS) {
      return(IDE_DISK_INVALID_SECTOR);
   } /* Endif */

   /* Each sector is 512 bytes */   
   data16_size = info_ptr->SECTOR_SIZE >> 1;
   data16_ptr = (uint_16_ptr)data_ptr;

   /* Setup select head */
   tmp = (uchar)((sector >> 24) & ATA_DRIVE_REG_ADDR_MASK);
   
   /* Wait for device to become ready */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return(IDE_DISK_READ_ERROR);
   } /* Endif */
   
   /* Select LBA mode, select head, Select the device
   ** and set obsolete bits (5, 7) 
   */
   disk_ptr->DEVICE = (info_ptr->DRIVE << 4) | ATA_DRIVE_REG_LBA | tmp | ATA_DRIVE_REG_OBSOLETE;

   /*
   ** Setup for Logical Block Addressing (LBA)
   ** A7 - A0 reside in the sector number register
   ** A15 - A8 reside in the cylinder low register
   ** A23 - A16 reside in the cylinder high register
   ** A27 - A24 reside in the drive/head register
   */
   disk_ptr->LBA_LOW = (uchar)(0x000000FF & sector);
   disk_ptr->LBA_MID = (uchar)((0x0000FF00 & sector) >> 8);
   disk_ptr->LBA_HIGH = (uchar)((0x00FF0000 & sector) >> 16);

   /* Set the sector count - read only one sector */
   disk_ptr->SECTOR_COUNT = (uchar)1;

   /* Wait for device to become ready */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return(IDE_DISK_READ_ERROR);
   } /* Endif */

   /* Send read command to device */
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_READ_SECTOR;

   /* Wait for device to become ready */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return(IDE_DISK_READ_ERROR);
   } /* Endif */

   /* Check for error condition */
   tmp = _io_disk_get_error(info_ptr);
   if (tmp != MQX_OK) {
      return(tmp);
   } /* Endif */

   /* Check if the data is ready */
   if (!_io_disk_data_rdy(info_ptr)) {   
      return(IDE_DISK_READ_ERROR);
   } /* Endif */
   
   /* Fill the sector buffer */
   for ( i = 0; i < data16_size; i++)  {
      *data16_ptr++ = (uint_16)(disk_ptr->DATA_PIO & 0x0000ffff);
   } /* Endfor */

   return(IDE_DISK_NO_ERROR);
   
} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_read_partial_sector
* Returned Value   : uint_32 - number of bytes read if successful,
*                    IO_ERROR otherwise
* Comments         : Read a partial sector  
*END*----------------------------------------------------------------------*/

int_32 _io_disk_read_partial_sector
   (
      /* [IN] The file handle for the device being closed */
      FILE_DEVICE_STRUCT_PTR fd_ptr,
      
      /* [IN] The sector number to read */
      uint_32                sector_number,
      
      /* [IN] The offset in the sector */
      uint_32                offset_in_sector,

      /* [IN/OUT] portion of a sector in bytes */
      uint_32                portion_of_sector,

      /* [IN] Location to read data into */
      uchar_ptr              dst_ptr
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR info_ptr   = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   uchar_ptr               tmp_ptr    = info_ptr->TEMP_BUFF_PTR;
   uint_32                 error;

   /* Check for zero size read */
   if (portion_of_sector == 0) {
      return portion_of_sector;   
   } /* Endif */

   /* Read the sector into temporary buffer */
   error = _io_disk_read_sector(info_ptr, sector_number, tmp_ptr);
   if (error != IDE_DISK_NO_ERROR) {
      return(IO_ERROR);
   } /* Endif */ 
   
   /* Copy only the part of the sector we want */
   _mem_copy(tmp_ptr + offset_in_sector, dst_ptr, portion_of_sector);

   return(portion_of_sector);   

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_write_sector
* Returned Value   : error code
* Comments         : 
*
* 
*END*----------------------------------------------------------------------*/

uint_32 _io_disk_write_sector
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr,

      /* [IN] The sector number to write */
      uint_32                  sector,

      /* [IN] Source data location */
      uchar_ptr                data_ptr 
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR      disk_ptr;
   volatile uint_16 _PTR_       data16_ptr;
   uint_32                      data16_size;
   uint_32                      i;
   uchar                        tmp;
   
   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;
   
   if (sector > info_ptr->NUM_SECTORS) {
      return(IDE_DISK_INVALID_SECTOR);
   } /* Endif */

   /* Check the source pointer */
   if (data_ptr == 0) {
      return(MQX_INVALID_POINTER);
   } /* Endif */            
   
   /* Each sector is 512 bytes */   
   data16_size = info_ptr->SECTOR_SIZE >> 1;
   data16_ptr = (uint_16_ptr)data_ptr;

   /* Setup select head */
   tmp = (uchar)((sector >> 24) & ATA_DRIVE_REG_ADDR_MASK);
   
   /* Wait for device to become ready */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return(IDE_DISK_READ_ERROR);
   } /* Endif */
   
   /* Select LBA mode, select head, Select the device
   ** and set obsolete bits (5, 7) 
   */
   disk_ptr->DEVICE = (info_ptr->DRIVE << 4) | ATA_DRIVE_REG_LBA | tmp | ATA_DRIVE_REG_OBSOLETE;

   /*
   ** Setup for Logical Block Addressing (LBA)
   ** A7 - A0 reside in the sector number register
   ** A15 - A8 reside in the cylinder low register
   ** A23 - A16 reside in the cylinder high register
   ** A27 - A24 reside in the drive/head register
   */
   disk_ptr->LBA_LOW = (uchar)(0x000000FF & sector);
   disk_ptr->LBA_MID = (uchar)((0x0000FF00 & sector) >> 8);
   disk_ptr->LBA_HIGH = (uchar)((0x00FF0000 & sector) >> 16);

   /* Set the sector count - write only one sector */
   disk_ptr->SECTOR_COUNT = (uchar)1;

   /* Wait for device to become free */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT) ) {
      return(IDE_DISK_WRITE_ERROR);
   } /* Endif */

   /* Send write command to the device */
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_WRITE_SECTOR;

   /* Wait for device to become ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Check for error condition */
   tmp = _io_disk_get_error(info_ptr);
   if (tmp != MQX_OK) {
      return(tmp);
   } /* Endif */
   
   /* Fill the sector buffer */
   for (i = 0; i < data16_size; i++)  {
      disk_ptr->DATA_PIO = *data16_ptr++;
   } /* Endfor */

   /* Wait for device to finish writing data */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT) ) {
      return(IDE_DISK_WRITE_ERROR);
   } /* Endif */

   return(IDE_DISK_NO_ERROR);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_write_partial_sector
* Returned Value   : uint_32 - number of bytes written if successful,
*                    IO_ERROR otherwise
* Comments         : Writes a partial sector while preserving contents of
*                    unused portions of the sector
* 
*END*----------------------------------------------------------------------*/

int_32 _io_disk_write_partial_sector
   (
      /* [IN] The file handle for the device being closed */
      FILE_DEVICE_STRUCT_PTR fd_ptr,
      
      /* [IN] The sector number to write */
      uint_32                sector_number,
      
      /* [IN] The offset in the sector */
      uint_32                offset_in_sector,

      /* [IN/OUT] Portion of the sector in bytes */
      uint_32                portion_of_sector,

      /* [IN] Location to write data from */
      uchar_ptr              src_ptr
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_DISK_INFO_STRUCT_PTR info_ptr   = (IO_DISK_INFO_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   uchar_ptr               tmp_ptr    = info_ptr->TEMP_BUFF_PTR;
   uint_32                 error;
         
   /* Check for zero bytes write */
   if (portion_of_sector == 0) {
      return portion_of_sector; 
   } /* Endif */     
         
   /* Read the sector into temporary buffer */
   error = _io_disk_read_sector(info_ptr, sector_number, tmp_ptr);
   if (error != IDE_DISK_NO_ERROR) {
      return(IO_ERROR);
   } /* Endif */
   
   /* Write new data into temporary buffer */
   _mem_copy(src_ptr, tmp_ptr + offset_in_sector, portion_of_sector);
   
   /* Write the newly modified buffer back into ide */
   if (_io_disk_write_sector(info_ptr, sector_number, tmp_ptr) == IDE_DISK_NO_ERROR) {
      return(portion_of_sector);
   } else {
      return(IO_ERROR);
   } /* Endif */

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_identify_device
* Returned Value   : error code
* Comments         : 
*
* 
*END*----------------------------------------------------------------------*/

uint_32 _io_disk_identify_device
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr,

      /* [IN] Location to read data into */
      uchar_ptr                data_ptr 
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR      disk_ptr;
   volatile uint_16 _PTR_       data16_ptr;
   uint_32                      data16_size;
   uint_32                      i;
   uchar                        tmp;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   data16_size = info_ptr->SECTOR_SIZE >> 1;
   data16_ptr = (uint_16_ptr)data_ptr;

   /* Make sure device isn't busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Send Command to the device */
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_IDENTIFY_DEVICE;

   /* Check if the device is ready */
   if (!_io_disk_data_rdy(info_ptr)) {   
      return(IDE_DISK_READ_ERROR);
   } /* Endif */

   /* Fill the sector buffer */
   for ( i = 0; i < data16_size; i++)  {
      *data16_ptr++ = (disk_ptr->DATA_PIO & 0x0000ffff);
   } /* Endfor */

   return(IDE_DISK_NO_ERROR);
   
} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_reset
* Returned Value   : error code
* Comments         : 
* 
*END*----------------------------------------------------------------------*/

uint_32 _io_disk_reset
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_16                  us, us_start;
   uint_16                  tmp;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;
 
   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_RST_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Perform soft reset */
   disk_ptr->DEVICECTRL_ALTSTAT_REG.DEVICE_CTRL = ATA_DEV_CNTL_SRST;

   /* Wait for 20 us */
   _int_disable();
   us_start = _time_get_microseconds();
   do {
      us = _time_get_microseconds();
   } while ((uint_32)(us - us_start) < 20000);
   _int_enable();

   disk_ptr->DEVICECTRL_ALTSTAT_REG.DEVICE_CTRL = 0;

   /* Wait for device to come out of reset */
   if (_io_disk_bsy_timeout(info_ptr, ATA_RST_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Set the device number and LBA mode */ 
   disk_ptr->DEVICE = (info_ptr->DRIVE << 4) | ATA_DRIVE_REG_LBA;
   
   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Wait for busy bit cleared */
   if (_io_disk_bsy_timeout(info_ptr, ATA_RST_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   return(IDE_DISK_NO_ERROR);

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_bsy_timeout
* Returned Value   : TRUE if timed out, FALSE otherwise
* Comments         : Wait until disk is  ot busy or timed out. 
* 
*END*----------------------------------------------------------------------*/

boolean _io_disk_bsy_timeout
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr,

      /* [IN] Number of seconds to timeout */
      uint_32              timeout_val
   )
{ /* Body */
   
   IDE_DISK_REG_STRUCT_PTR disk_ptr;
   TIME_STRUCT             time;
   uint_32                 seconds;
   uchar                   status;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;
      
   _time_get(&time);
   seconds = time.SECONDS;

   do {
	  status = disk_ptr->CMD_STAT_REG.STATUS;
      _time_get(&time);

      if ((time.SECONDS - seconds) >= timeout_val) {
         break;
      } /* Endif */

   } while(status & ATA_STAT_BSY);

   /* Get status again just in case... */
   status = disk_ptr->CMD_STAT_REG.STATUS;

   if (status & ATA_STAT_BSY) {
      /* We timed out */
      return(TRUE);
   } /* Endif */

   return(FALSE);

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_rdy_timeout
* Returned Value   : TRUE if timed out, FALSE otherwise
* Comments         : Wait until disk is ready or timed out. 
* 
*END*----------------------------------------------------------------------*/

boolean _io_disk_rdy_timeout
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr,

      /* [IN] Number of seconds to timeout */
      uint_32              timeout_val
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR disk_ptr;
   TIME_STRUCT             time;
   uint_32                 seconds;
   uchar                   status;
   boolean                 result = FALSE;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;
      
   _time_get(&time);
   seconds = time.SECONDS;

   do {
	  status = disk_ptr->CMD_STAT_REG.STATUS;
      _time_get(&time);

      if ((time.SECONDS - seconds) >= timeout_val) {
         result = TRUE;
         break;                           
      } /* Endif */

   } while(!(status & ATA_STAT_DRDY));
   
   return(result);

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_disk_data_rdy
* Returned Value   : FALSE on failer, TRUE otherwise
* Comments         : Waits until the DRQ bit is set                   
* 
*END*----------------------------------------------------------------------*/

boolean _io_disk_data_rdy
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR disk_ptr;
   uint_32 status;
    
   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (FALSE);
   } /* Endif */
    
   do {
      status = disk_ptr->CMD_STAT_REG.STATUS;
      
      if (status & ATA_STAT_ERR) {
         return (FALSE);
      } /* Endif */
      
   } while(!(status & ATA_STAT_DRQ));
    
   return (TRUE);    
    
} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_seek
* Returned Value   : uint_32
* Comments         : 
*    This command is obsolete. This function is only here for backward 
*    compatibility.
*
*END*********************************************************************/

uint_32 _io_disk_seek
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;
 
   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Send Command - this actually does nothing */
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_SEEK;

   return(MQX_OK);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_get_error
* Returned Value   : uint_32
* Comments         :
*    Check for error condition and return the error
*
*END*********************************************************************/

uint_32 _io_disk_get_error
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  error;   
   uint_32                  result = MQX_OK;   

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;
   
   /* Get value in error register */
   error = disk_ptr->ERR_FEAT_REG.ERROR;

   /* Check for error */
   if (!(error & ATA_ERR_MASK)) {
      return(result);
   } /* Endif */

   /* Find the error */
   if ((error & ATA_ERR_CRC) == ATA_ERR_CRC) { 
      result  |= ATA_ERR_CRC;
   } else if ((error & ATA_ERR_UNC) == ATA_ERR_UNC) {
      result  |= ATA_ERR_UNC;
   } else if ((error & ATA_ERR_IDNF) == ATA_ERR_IDNF) {
      result  |= ATA_ERR_IDNF;
   } else if ((error & ATA_ERR_ABRT) == ATA_ERR_ABRT) {
      result  |= ATA_ERR_ABRT;
   } else if ((error & ATA_ERR_TKONF) == ATA_ERR_TKONF) {
      result  |= ATA_ERR_TKONF;
   } else if ((error & ATA_ERR_AMNF) == ATA_ERR_AMNF) {
      result  |= ATA_ERR_AMNF;
   } /* Endif */
   
   return(result);   
   
} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_spin_down
* Returned Value   : uint_32
* Comments         :
*    This function stops the disk by setting standby immediate command
*
*END*********************************************************************/

uint_32 _io_disk_spin_down
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  status;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Send command */   
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_STANDBY_IMMEDIATE;

   /* Get device status */
   status = _io_disk_get_stats(info_ptr);   
   
   /* Check for device fault */   
   if ((status & ATA_STAT_DF) == ATA_STAT_DF) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check for error */
   if ((status & ATA_STAT_ERR) == ATA_STAT_ERR) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   return(MQX_OK);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_spin_up
* Returned Value   : uint_32
* Comments         :
*    This function starts the disk by setting idle immediate command
*
*END*********************************************************************/

uint_32 _io_disk_spin_up
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  status;   

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Send command */   
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_IDLE_IMMEDIATE;

   /* Get device status */
   status = _io_disk_get_stats(info_ptr);   
   
   /* Check for device fault */   
   if ((status & ATA_STAT_DF) == ATA_STAT_DF) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check for error */
   if ((status & ATA_STAT_ERR) == ATA_STAT_ERR) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   return(MQX_OK);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_idle
* Returned Value   : uint_32
* Comments         :
*    This function place the device in the Idle mode.
*    Automatic Standby timer period is disabled, that means timeout is 
*    disabled and device stays Idle until it receives another command.
*
*END*********************************************************************/

uint_32 _io_disk_idle
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  status;   

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Disable standby timeout */
   disk_ptr->SECTOR_COUNT = 0;
   
   /* Send command */      
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_IDLE;

   /* Get device status */
   status = _io_disk_get_stats(info_ptr);   
   
   /* Check for device fault */   
   if ((status & ATA_STAT_DF) == ATA_STAT_DF) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check for error */
   if ((status & ATA_STAT_ERR) == ATA_STAT_ERR) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   return(MQX_OK);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_standby
* Returned Value   : uint_32
* Comments         :
*    This function causes the device to enter the Standby mode.
*    Automatic Standby timer period is disabled, that means timeout is 
*    disabled and device stays in standby mode until it receives another 
*    command.
*
*END*********************************************************************/

uint_32 _io_disk_standby
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  status;   

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Disable standby timeout */
   disk_ptr->SECTOR_COUNT = 0;
   
   /* Send command */      
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_STANDBY;

   /* Get device status */
   status = _io_disk_get_stats(info_ptr);   
   
   /* Check for device fault */   
   if ((status & ATA_STAT_DF) == ATA_STAT_DF) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check for error */
   if ((status & ATA_STAT_ERR) == ATA_STAT_ERR) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   return(MQX_OK);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_get_stats
* Returned Value   : uint_32
* Comments         :
*    This function returns the status of the disk drive by reading status
*    register of the disk drive.
*
*END*********************************************************************/

uint_32 _io_disk_get_stats
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   return(disk_ptr->CMD_STAT_REG.STATUS);
   
} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_check_pwr_mode
* Returned Value   : uint_32
* Comments         :
*    This function allows the host to determine the current power mode of 
*    the device. It retuns the current mode of the device.
*
*END*********************************************************************/

uint_32 _io_disk_check_pwr_mode
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  status;      

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_BSY_DEFAULT_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Send command */      
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_CHECK_POWER_MODE;

   /* Get device status */
   status = _io_disk_get_stats(info_ptr);   
   
   /* Check for device fault */   
   if ((status & ATA_STAT_DF) == ATA_STAT_DF) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check for error */
   if ((status & ATA_STAT_ERR) == ATA_STAT_ERR) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Get the result value of the command and return it */
   return(disk_ptr->SECTOR_COUNT);
       
} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_flush_cache
* Returned Value   : uint_32
* Comments         :
*    This function allows the host to request the device to flush the write
*    cache.
*
*END*********************************************************************/

uint_32 _io_disk_flush_cache
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;
   uint_32                  status;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Send command */      
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_DEVICE_FLUSH_CACHE;

   /* Make sure device is not busy - command completion may take more than 30 s.*/
   if (_io_disk_bsy_timeout(info_ptr, ATA_RST_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Get device status */
   status = _io_disk_get_stats(info_ptr);   
   
   /* Check for device fault */   
   if ((status & ATA_STAT_DF) == ATA_STAT_DF) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check for error */
   if ((status & ATA_STAT_ERR) == ATA_STAT_ERR) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */
   
   /* Check if the device is ready */
   if (_io_disk_rdy_timeout(info_ptr, ATA_RTY_TIMEOUT)) {
      return(IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   return(MQX_OK);

} /* Endbody */

/*FUNCTION*****************************************************************
* 
* Function Name    : _io_disk_diagnos
* Returned Value   : uint_32
* Comments         :
*    This function causes the devices to perform the internal diagnostic
*    tests. Both devices, if present, shall execute this command regardless
*    of which device is selected.
*
*END*********************************************************************/

uint_32 _io_disk_diagnos
   (
      /* [IN] IDE state structure */
      IO_DISK_INFO_STRUCT_PTR  info_ptr,
      
      /* [OUT] the ioctl parameters */      
      uint_32_ptr             param_ptr
   )
{ /* Body */

   IDE_DISK_REG_STRUCT_PTR  disk_ptr;

   disk_ptr = (IDE_DISK_REG_STRUCT_PTR)info_ptr->INIT;

   /* Send command */      
   disk_ptr->CMD_STAT_REG.COMMAND = ATA_EXECUTE_DEVICE_DIAGNOSTIC;

   /* Make sure device is not busy */
   if (_io_disk_bsy_timeout(info_ptr, ATA_RST_TIMEOUT)) {
      return (IDE_DISK_HARDWARE_ERROR);
   } /* Endif */

   /* Get the device error code */
   *param_ptr = disk_ptr->ERR_FEAT_REG.ERROR;   
   
   /* Check for device 0 only */   
   if ((*param_ptr & IO_IDE_DISK_DEVICE0_FAILED_DEVICE1_PASSED) || 
       (*param_ptr & IO_IDE_DISK_DEVICE0_FAILED_DEVICE1_FAILED)) {
      return(IDE_DISK_DIAGNOSTIC_ERROR);
   } /* Endif */   
   
   return(MQX_OK);

} /* Endbody */

#endif

/* EOF */