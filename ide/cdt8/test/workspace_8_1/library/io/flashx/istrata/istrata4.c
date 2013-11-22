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
*** File: istrata4.c
*** 
*** Comments: The file contains functions to program Intel's StrataFlash 
***   devices
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h" 
#include "bsp.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"
#include "flashx.h"
#include "flashxprv.h"
#include "istrata.h"
#include "istrataprv.h"

                                          
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_program_4byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function programs a sector of flash for 8 bit devices
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_program_4byte
   (  
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR  handle_ptr,

      /* [IN] where to copy data from */
      uint_32_ptr           from_ptr,
      
      /* [OUT} where to copy data to */
      uint_32_ptr           to_ptr,

      /* [IN] the sector size to copy */
      _mem_size             sector_size,

      /* [IN] the offset to start at */
      _mem_size             offset
   )
{ /* Body */
   volatile uint_32 _PTR_  dest_ptr = (volatile uint_32 _PTR_)to_ptr;
   uint_32 _PTR_           src_ptr  = (uint_32 _PTR_)from_ptr;
   boolean                 erase;
   boolean                 success;
   boolean                 timeout;
   _mqx_uint               j;
   _mem_size               size = sector_size >> 2;
   MQX_TICK_STRUCT         start_ticks;
   uint_32                 tmp, status;

   erase    = FALSE;
   offset >>= 2;

   /* 
   ** Check to see if flash needs erasing.
   */ 
   for (j = offset; !erase && (j < size); j++) {
      erase = ISTRATA_ERASE_UNIT(to_ptr[j], from_ptr[j]);
   } /* Endfor */

   if (erase) {
      success = _intel_strata_erase_4byte(handle_ptr, to_ptr, size, 
         &start_ticks);
      if (!success) {
         return success;
      } /* Endif */
      /* 
      ** We erased the whole sector so we must program from the beginning of
      ** the sector
      */
      offset = 0;
   } else {
      success = TRUE;
   } /* Endif */

   dest_ptr = dest_ptr + offset;
   src_ptr  = src_ptr  + offset;

   ISTRATA_PROGRAM(ISTRATA_READ_MODE_CMD_32X1, ISTRATA_WRITE_CMD_32X1, 
      ISTRATA_STATUS_BUSY_32X1);

   if ( success ) {
      /* Verify sector is written correctly */
      for( j = 0; j < size; j++ ) {
         if ( from_ptr[j] != to_ptr[j] ) {
            success = FALSE;
            break;
         } /* Endif */
      } /* Endfor */
   } /* Endif */

   return success;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_erase_4byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function erases a sector
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_erase_4byte
   (
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR handle_ptr,

      /* [IN] the sector to erase */
      uint_32_ptr          input_sect_ptr,

      /* [IN] the size of the sector */
      _mem_size            sect_size,

      /* [IN/OUT] place to store start time */
      MQX_TICK_STRUCT_PTR  start_ticks_ptr
   )
{ /* Body */
   boolean timeout = FALSE;
   boolean success;

   if (handle_ptr->DEVICES == 1) {
      volatile uint_32 _PTR_  dest_ptr = (volatile uint_32 _PTR_)input_sect_ptr;
      uint_16                 status;

      ISTRATA_ERASE(ISTRATA_READ_MODE_CMD_32X1, ISTRATA_ERASE_CMD1_32X1, 
         ISTRATA_ERASE_CMD2_32X1, ISTRATA_STATUS_BUSY_32X1, 0xFFFFFFFF);

   } else {
      success = FALSE;
   } /* Endif */

   return success;

} /* Endbody */


/* Start CR 871 */
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_clearlockbits_4byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function unlocks all sectors
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_clearlockbits_4byte
   (
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR handle_ptr,

      /* [IN/OUT] place to store start time */
      MQX_TICK_STRUCT_PTR  start_ticks_ptr
   )
{ /* Body */
   uint_32 block_size;
   uint_32 num_blocks;
   uint_32 i;
   boolean timeout = FALSE;
   boolean success;

   num_blocks = handle_ptr->BLOCK_INFO_PTR->NUM_SECTORS; /* sector == block */
   block_size = handle_ptr->BLOCK_INFO_PTR->SECT_SIZE;

   /*
   ** All blocks default to the locked state after initial power-up or reset.
   ** The blocks are unlocked individually.
   */
   if (handle_ptr->DEVICES == 1) {
      volatile uint_32 _PTR_  dest_ptr;
      uint_32                 status;

      for(i = 0; i < num_blocks; i++) {
         dest_ptr = (volatile uint_32 _PTR_)(handle_ptr->BASE_ADDR + (i * block_size));

         ISTRATA_CLEAR_LOCKBITS(ISTRATA_READ_MODE_CMD_32X1, ISTRATA_CLEAR_CMD1_32X1, 
            ISTRATA_CLEAR_CMD2_32X1, ISTRATA_STATUS_BUSY_32X1);
      } /* Endfor */
      
   } else {
      success = FALSE;
   } /* Endif */

   return success;

} /* Endbody */
/* End CR 871 */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_setlockbits_4byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function locks all sectors
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_setlockbits_4byte
   (
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR handle_ptr,

      /* [IN/OUT] place to store start time */
      MQX_TICK_STRUCT_PTR  start_ticks_ptr
   )
{ /* Body */
   uint_32 block_size;
   uint_32 num_blocks;
   uint_32 i;
   boolean timeout = FALSE;
   boolean success;

   num_blocks = handle_ptr->BLOCK_INFO_PTR->NUM_SECTORS; /* sector == block */
   block_size = handle_ptr->BLOCK_INFO_PTR->SECT_SIZE;

   /*
   ** The blocks are locked individually.
   */
   if (handle_ptr->DEVICES == 1) {
      volatile uint_32 _PTR_  dest_ptr;
      uint_32                 status;

      for(i = 0; i < num_blocks; i++) {
         dest_ptr = (volatile uint_32 _PTR_)(handle_ptr->BASE_ADDR + (i * block_size));

         ISTRATA_SET_LOCKBITS(ISTRATA_READ_MODE_CMD_32X1, ISTRATA_SET_CMD1_32X1, 
            ISTRATA_SET_CMD2_32X1, ISTRATA_STATUS_BUSY_32X1);
      } /* Endfor */
      
   } else {
      success = FALSE;
   } /* Endif */

   return success;

} /* Endbody */

/* EOF */
