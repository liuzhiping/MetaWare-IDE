#ifndef __io_diskprv_h__
#define __io_diskprv_h__
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
*** File: io_diskprv.h
***
*** Comments: The file contains functions prototype, defines, structure 
***           definitions private to a hard disk. This file is added to
***           address CR 2283
***
**************************************************************************
*END*********************************************************************/

/*-----------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/

/*
Addr.       Read                  	 Write
------	  --------				   ----------
    ****** Control block registers ******
0b0xx    Data bus high imped*1  	Not used 
0b10x    Data bus high imped*1      Not used
0b110    Alternate Status           Device Control
0b111    Device Address             Not used
 
     ****** Command block registers ******
0b000    Data                       Data 
0b001    Error Register             Features
0b010    Sector Count               Sector Count 
0b011    Sector Number              Sector Number     --> CHS mode 
0b011    *2 LBA bits 0-7            *2 LBA bits 0-7   --> LBA mode
0b100    Cylinder Low               Cylinder Low      --> CHS mode
0b100    *2 LBA bits 8-15           *2 LBA bits 8-15  --> LBA mode
0b101    Cylinder High              Cylinder High     --> CHS mode
0b101    *2 LBA bits 16-23          *2 LBA bits 16-23 --> LBA mode
0b110    Device/Head                Device/Head       --> CHS mode
0b110    *2 LBA bits 24-27          *2 LBA bits 24-27 --> LBA mode
0b111    Status                     Command 
0bxxx    Invalid address            Invalid address 

NOTE:
1)    *1 "imped" stands for "impedance".
2)    *2 Mapping of registers in LBA mode

Logic conventions : 
x = does not matter which it is
*/

/* ARC's IDE Disk registers in LBA mode */
/*
** Audio/Video Peripheral System Architecture user's guild:
** "All hard disk IDE resgisters are mapped into the register space 0x80 to 0xFC. 
**  Each 16 bit register space in the IDE hard disk is mapped to a 32bit register 
**  space in this area."
** All registers assumed to be uint32 access.
*/


typedef volatile struct ide_disk_reg_struct
{

   /* Data register for PIO access only */
   uint_32   DATA_PIO;

   /* Error or Feature register */
   union {
      uint_32   ERROR;
      uint_32   FEATURE;
   } ERR_FEAT_REG;

   /* Sector Count register */
   uint_32   SECTOR_COUNT;

   /* LBA Low register, bit 0-7 */
   uint_32   LBA_LOW;

   /* LBA Mid register, bits 8-15 */
   uint_32   LBA_MID;

   /* LBA High register, bits 16-23 */
   uint_32   LBA_HIGH;

   /* Device register, in LBA mode: device - LBA bits 24-27 */
   uint_32   DEVICE;

   /* Command or Status register */
   union {
      uint_32 COMMAND;
      uint_32 STATUS;
   } CMD_STAT_REG;

   /* */
   uint_32   RESERVED[6];

   /* Device Control or Alternate Status register */
   union {
      uint_32 DEVICE_CTRL;
      uint_32 ALT_STATUS;
   } DEVICECTRL_ALTSTAT_REG;

} IDE_DISK_REG_STRUCT, _PTR_ IDE_DISK_REG_STRUCT_PTR; 


/*-----------------------------------------------------------------------*/
/*
**                          PROTOTYPE DECLARATIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

extern uint_32  _io_disk_read_sector(IO_DISK_INFO_STRUCT_PTR, uint_32, uchar_ptr);
extern _mqx_int _io_disk_read_partial_sector(FILE_DEVICE_STRUCT_PTR, uint_32,
                                            uint_32, uint_32, uchar_ptr);

extern uint_32  _io_disk_write_sector(IO_DISK_INFO_STRUCT_PTR, uint_32, uchar_ptr);
extern _mqx_int _io_disk_write_partial_sector(FILE_DEVICE_STRUCT_PTR, uint_32, 
                                             uint_32, uint_32, uchar_ptr);

extern uint_32  _io_disk_identify_device(IO_DISK_INFO_STRUCT_PTR, uchar_ptr);
extern uint_32  _io_disk_reset(IO_DISK_INFO_STRUCT_PTR);
extern boolean  _io_disk_data_rdy(IO_DISK_INFO_STRUCT_PTR);
extern boolean  _io_disk_bsy_timeout(IO_DISK_INFO_STRUCT_PTR, uint_32);
extern boolean  _io_disk_rdy_timeout(IO_DISK_INFO_STRUCT_PTR, uint_32);

extern uint_32  _io_disk_seek(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_get_error(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_spin_down(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_spin_up(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_idle(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_standby(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_get_stats(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_check_pwr_mode(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_flush_cache(IO_DISK_INFO_STRUCT_PTR);
extern uint_32  _io_disk_diagnos(IO_DISK_INFO_STRUCT_PTR, uint_32_ptr);

#ifdef __cplusplus
}
#endif

#endif
