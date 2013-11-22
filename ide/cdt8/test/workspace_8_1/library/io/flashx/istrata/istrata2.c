/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2006 ARC International. 
*** All rights reserved                                           
*** 
*** This software embodies materials and concepts which are       
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
*** 
*** File: istrata2.c
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
* Function Name    : _intel_strata_program_2byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function programs a sector of flash for 8 bit devices
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_program_2byte
   (  
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR  handle_ptr,

      /* [IN] where to copy data from */
      uint_16_ptr           from_ptr,
      
      /* [OUT} where to copy data to */
      uint_16_ptr           to_ptr,

      /* [IN] the sector size to copy */
      _mem_size             sector_size,

      /* [IN] the offset to start at */
      _mem_size             offset
   )
{ /* Body */
   boolean                 erase;
   boolean                 success;
   boolean                 timeout;
   _mqx_uint               j;
   MQX_TICK_STRUCT         start_ticks;
   _mem_size               size = sector_size >> 1;

   /* 
   ** Check to see if flash needs erasing.
   */ 
   erase    = FALSE;
   offset >>= 1;

   for (j = offset; !erase && (j < size); j++) {
      erase = ISTRATA_ERASE_UNIT(to_ptr[j], from_ptr[j]);
   } /* Endfor */

   if (erase) {
      success = _intel_strata_erase_2byte(handle_ptr, to_ptr, size, 
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

   if (handle_ptr->DEVICES == 1) {
      volatile uint_16 _PTR_  dest_ptr = (volatile uint_16 _PTR_)to_ptr;
      uint_16 _PTR_           src_ptr  = (uint_16 _PTR_)from_ptr;
      uint_16                 tmp, status;

      dest_ptr = dest_ptr + offset;
      src_ptr  = src_ptr  + offset;

      ISTRATA_PROGRAM(ISTRATA_READ_MODE_CMD_16X1, ISTRATA_WRITE_CMD_16X1, 
         ISTRATA_STATUS_BUSY_16X1);
   } else {
      volatile uint_32 _PTR_  dest_ptr = (volatile uint_32 _PTR_)((pointer)to_ptr);
      uint_32 _PTR_           src_ptr  = (uint_32 _PTR_)((pointer)from_ptr);
      uint_32                 tmp, status;

      size   >>= 1;
      offset >>= 1;
      dest_ptr = dest_ptr + offset;
      src_ptr  = src_ptr  + offset;

      ISTRATA_PROGRAM(ISTRATA_READ_MODE_CMD_16X2, ISTRATA_WRITE_CMD_16X2, 
         ISTRATA_STATUS_BUSY_16X2);
   } /* Endif */

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
* Function Name    : _intel_strata_erase_2byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function erases a sector
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_erase_2byte
   (
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR handle_ptr,

      /* [IN] the sector to erase */
      uint_16_ptr          input_sect_ptr,

      /* [IN] the size of the sector */
      _mem_size            sect_size,

      /* [IN/OUT] place to store start time */
      MQX_TICK_STRUCT_PTR  start_ticks_ptr
   )
{ /* Body */
   boolean timeout = FALSE;
   boolean success;

   if (handle_ptr->DEVICES == 1) {
      volatile uint_16 _PTR_  dest_ptr = (volatile uint_16 _PTR_)input_sect_ptr;
      uint_16                 status;

      ISTRATA_ERASE(ISTRATA_READ_MODE_CMD_16X1, ISTRATA_ERASE_CMD1_16X1, 
         ISTRATA_ERASE_CMD2_16X1, ISTRATA_STATUS_BUSY_16X1, 0xFFFF);

   } else if (handle_ptr->DEVICES == 2) {
      volatile uint_32 _PTR_  dest_ptr = (volatile uint_32 _PTR_)
         ((pointer)input_sect_ptr);
      uint_32                 status;

      ISTRATA_ERASE(ISTRATA_READ_MODE_CMD_16X2, ISTRATA_ERASE_CMD1_16X2, 
         ISTRATA_ERASE_CMD2_16X2, ISTRATA_STATUS_BUSY_16X2, 0xFFFFFFFF);

   } else {
      success = FALSE;
   } /* Endif */

   return success;

} /* Endbody */


/* Start CR 871 */
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_clearlockbits_2byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function unlocks all sectors
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_clearlockbits_2byte
   (
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR handle_ptr,

      /* [IN/OUT] place to store start time */
      MQX_TICK_STRUCT_PTR  start_ticks_ptr
   )
{ /* Body */
   /* Start CR 2077 */
   FLASHX_BLOCK_INFO_STRUCT_PTR   block_info_ptr;
   uint_32 block_size;
   uint_32 num_blocks;
   uint_32 i;
   boolean timeout = FALSE;
   boolean success;

   block_info_ptr = handle_ptr->BLOCK_INFO_PTR;

   /*
   ** All blocks default to the locked state after initial power-up or reset.
   ** The blocks are unlocked individually.
   */
   while (block_info_ptr->NUM_SECTORS) {
         
      num_blocks = block_info_ptr->NUM_SECTORS; /* sector == block */
      block_size = block_info_ptr->SECT_SIZE;      

   if (handle_ptr->DEVICES == 1) {
      volatile uint_16 _PTR_  dest_ptr;
      uint_16                 status;

      for(i = 0; i < num_blocks; i++) {
         dest_ptr = (volatile uint_16 _PTR_)(handle_ptr->BASE_ADDR + block_info_ptr->START_ADDR + (i * block_size));

         ISTRATA_CLEAR_LOCKBITS(ISTRATA_READ_MODE_CMD_16X1, ISTRATA_CLEAR_CMD1_16X1, 
            ISTRATA_CLEAR_CMD2_16X1, ISTRATA_STATUS_BUSY_16X1);
      } /* Endfor */
      
   } else if (handle_ptr->DEVICES == 2) {
      volatile uint_32 _PTR_  dest_ptr;
      uint_32                 status;
      
      for(i = 0; i < num_blocks; i++) {
         dest_ptr = (volatile uint_32 _PTR_)(handle_ptr->BASE_ADDR + (i * block_size));
         
         ISTRATA_CLEAR_LOCKBITS(ISTRATA_READ_MODE_CMD_16X2, ISTRATA_CLEAR_CMD1_16X2, 
            ISTRATA_CLEAR_CMD2_16X2, ISTRATA_STATUS_BUSY_16X2);
      } /* Endfor */

   } else {
      success = FALSE;
   } /* Endif */
      
      block_info_ptr++;      
   } /* Endwhile */
   /* End CR 2077 */

   return success;

} /* Endbody */
/* End CR 871 */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_setlockbits_2byte
* Returned Value   : TRUE if successful
* Comments         : 
*    This function locks all sectors
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_setlockbits_2byte
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
      volatile uint_16 _PTR_  dest_ptr;
      uint_16                 status;

      for(i = 0; i < num_blocks; i++) {
         dest_ptr = (volatile uint_16 _PTR_)(handle_ptr->BASE_ADDR + (i * block_size));
         
         ISTRATA_SET_LOCKBITS(ISTRATA_READ_MODE_CMD_16X1, ISTRATA_SET_CMD1_16X1, 
            ISTRATA_SET_CMD2_16X1, ISTRATA_STATUS_BUSY_16X1);
      } /* Endfor */
      
   } else if (handle_ptr->DEVICES == 2) {
      volatile uint_32 _PTR_  dest_ptr;
      uint_32                 status;
      
      for(i = 0; i < num_blocks; i++) {
         dest_ptr = (volatile uint_32 _PTR_)(handle_ptr->BASE_ADDR + (i * block_size));

         ISTRATA_SET_LOCKBITS(ISTRATA_READ_MODE_CMD_16X2, ISTRATA_SET_CMD1_16X2, 
            ISTRATA_SET_CMD2_16X2, ISTRATA_STATUS_BUSY_16X2);
      } /* Endfor */

   } else {
      success = FALSE;
   } /* Endif */

   return success;

} /* Endbody */

/* EOF */