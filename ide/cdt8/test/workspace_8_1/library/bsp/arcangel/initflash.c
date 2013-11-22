/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: initflash.c
***
*** Comments:      
***   This file contains the default initialization record for the
*** flash.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"

#if BSP_USE_FLASH

FLASHX_BLOCK_INFO_STRUCT _bsp_flashx_block_map[2] = 
{ 
   { BSP_NUM_FLASH_BLOCKS, 0, (BSP_FLASH_BLOCK_SIZE * BSP_FLASH_DEVICES)},
   { 0,                    0, 0                                         }
};

extern boolean _bsp_flash_init(IO_FLASHX_STRUCT_PTR);

FLASHX_INIT_STRUCT       _bsp_flashx_init =
{
   /* NAME           */   "flash:",
   /* SECTOR_ERASE   */   0,
   /* SECTOR_PROGRAM */   _intel_strata_program,
   /* CHP_ERASE      */   0,
   /* INIT           */   _bsp_flash_init,
   /* DEINIT         */   0,
   /* WRITE_PROTECT  */   0,
   /* MAP_PTR        */   _bsp_flashx_block_map,
   /* BASE_ADDR      */   BSP_FLASH_BASE,
   /* WIDTH          */   BSP_FLASH_WIDTH,
   /* DEVICES        */   BSP_FLASH_DEVICES
};

boolean _bsp_flash_init(IO_FLASHX_STRUCT_PTR handle) {

   return _intel_strata_clear_lock_bits(handle);
}

#endif

/* EOF */
