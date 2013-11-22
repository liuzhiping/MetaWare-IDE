/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International. 
*** All rights reserved                                           
*** 
*** This software embodies materials and concepts which are       
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
*** 
*** File: istrata.c
*** 
*** Comments: The file contains functions to program Intel's StrataFlash 
***   devices
***           While this driver is generic to Intel's StrataFlash family of
***   products it has only been tested on the following parts:
***     28F128J3A
***
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
* Function Name    : _intel_strata_program
* Returned Value   : TRUE if successful
* Comments         : 
*    This function programs a sector of flash
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_program
   (  
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR  handle_ptr,

      /* [IN] where to copy data from */
      uchar_ptr             from_ptr,
      
      /* [OUT} where to copy data to */
      uchar_ptr             to_ptr,

      /* [IN] the sector size to copy */
      _mem_size             sector_size
   )
{ /* Body */
   _mqx_uint  total_width;
   _mqx_uint  i = 0;
   boolean    program = FALSE;
   boolean    result;

   total_width = handle_ptr->DEVICES * handle_ptr->WIDTH;

   /* First make sure we actually need to program the sector */
   if (total_width == 1) {
      uint_8_ptr  from8_ptr = (uint_8_ptr)((pointer)from_ptr);
      uint_8_ptr  to8_ptr   = (uint_8_ptr)((pointer)to_ptr);

      while( !program && (i < sector_size) ) {
         program = *from8_ptr++ != *to8_ptr++;
         i++;
      } /* Endwhile */

      i--;
   } else if (total_width == 2) {
      uint_16_ptr  from16_ptr = (uint_16_ptr)((pointer)from_ptr);
      uint_16_ptr  to16_ptr   = (uint_16_ptr)((pointer)to_ptr);

      while( !program && (i < sector_size) ) {
         program = *from16_ptr++ != *to16_ptr++;
         i += 2;
      } /* Endwhile */

      i -= 2;
   } else if (total_width == 4) {
      uint_32_ptr  from32_ptr = (uint_32_ptr)((pointer)from_ptr);
      uint_32_ptr  to32_ptr   = (uint_32_ptr)((pointer)to_ptr);

      while( !program && (i < sector_size) ) {
         program = *from32_ptr++ != *to32_ptr++;
         i += 4;
      } /* Endwhile */

      i -= 4;
   } else {
      /* Unsupported configuration */
      return FALSE;
   } /* Endif */

   if (!program) {
      return !program;
   } /* Endif */

   switch (handle_ptr->WIDTH) {
      case 1:
         result = _intel_strata_program_1byte(handle_ptr, from_ptr, to_ptr, 
            sector_size, i);
         break;
      case 2:
         result = _intel_strata_program_2byte(handle_ptr, (uint_16_ptr)from_ptr,
            (uint_16_ptr)to_ptr, sector_size, i);
         break;
      case 4:
         result = _intel_strata_program_4byte(handle_ptr, (uint_32_ptr)from_ptr,
            (uint_32_ptr)to_ptr, sector_size, i);
         break;
   } /* Endswitch */

   return result;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_erase
* Returned Value   : TRUE if successful
* Comments         : 
*    This function erases a sector of flash
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_erase
   (  
      /* [IN] device info */
      IO_FLASHX_STRUCT_PTR  handle_ptr,

      /* [IN] the sector to erase */
      uchar_ptr            input_sect_ptr,
      
      /* [IN] the number of bytes to erase */
      _mem_size            bytes
   )
{ /* Body */
   _mqx_uint  total_width;
   _mqx_uint  i = 0;
   boolean    result = TRUE;
   boolean    erase = FALSE;
   MQX_TICK_STRUCT  tmp_ticks;

   
   if (handle_ptr->WRITE_PROTECT) {
      (*handle_ptr->WRITE_PROTECT)(handle_ptr, FALSE);
   }/* Endif */

   total_width = handle_ptr->DEVICES * handle_ptr->WIDTH;

   /* First make sure we actually need to erase the sector */
   if (total_width == 1) {
      uint_8_ptr  to8_ptr   = (uint_8_ptr)((pointer)input_sect_ptr);

      while( !erase && (i < bytes) ) {
         erase = 0xff != *to8_ptr++;
         i++;
      } /* Endwhile */

   } else if (total_width == 2) {
      uint_16_ptr  to16_ptr   = (uint_16_ptr)((pointer)input_sect_ptr);

      while( !erase && (i < bytes) ) {
         erase = 0xffff != *to16_ptr++;
         i += 2;
      } /* Endwhile */

   } else if (total_width == 4) {
      uint_32_ptr  to32_ptr   = (uint_32_ptr)((pointer)input_sect_ptr);

      while( !erase && (i < bytes) ) {
         erase = 0xffffffff != *to32_ptr++;
         i += 4;
      } /* Endwhile */

   } else {
      /* Unsupported configuration */
      return FALSE;
   } /* Endif */

   if (!erase) {
      return !erase;
   } /* Endif */

   switch (handle_ptr->WIDTH) {
      case 1:
         result = _intel_strata_erase_1byte(handle_ptr, input_sect_ptr, 
            bytes, &tmp_ticks);
         break;
      case 2:
         result = _intel_strata_erase_2byte(handle_ptr, (uint_16_ptr)input_sect_ptr,
            bytes, &tmp_ticks);
         break;
      case 4:
         result = _intel_strata_erase_4byte(handle_ptr, (uint_32_ptr)input_sect_ptr,
            bytes, &tmp_ticks);
         break;
   } /* Endswitch */

   return result;

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_check_timeout
* Returned Value   : TRUE if timeout occurs
* Comments         : 
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_check_timeout
   (  
      /* [IN] Time operation started in ticks */
      MQX_TICK_STRUCT_PTR   start_tick_ptr,

      /* [IN] The number of ticks the operation is expected to take */
      _mqx_uint             period
   )
{ /* Body */
   MQX_TICK_STRUCT  end_ticks;
   MQX_TICK_STRUCT  current_ticks;
   _mqx_int         result;

   PSP_ADD_TICKS_TO_TICK_STRUCT(start_tick_ptr, period, &end_ticks);
   _time_get_elapsed_ticks(&current_ticks);

   result = PSP_CMP_TICKS(&current_ticks, &end_ticks);

   if (result >= 0) {
      printf("\n-->timeout");
      return TRUE;
   } else {
      return FALSE;
   } /* Endif */

} /* Endbody */


/* Start CR 871 */
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_clear_lock_bits
* Returned Value   : TRUE if successful
* Comments         : 
*    This function clears the lock bits on all sectors
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_clear_lock_bits
   (  
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR  handle_ptr
   )
{ /* Body */
   boolean          result;
   MQX_TICK_STRUCT  start_ticks;

   switch (handle_ptr->WIDTH) {
      case 1:
         result = _intel_strata_clearlockbits_1byte(handle_ptr, &start_ticks);
         break;
      case 2:
         result = _intel_strata_clearlockbits_2byte(handle_ptr, &start_ticks);
         break;
      case 4:
         result = _intel_strata_clearlockbits_4byte(handle_ptr, &start_ticks);
         break;
   } /* Endswitch */

   return result;

} /* Endbody */
/* End CR 871 */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _intel_strata_set_lock_bits
* Returned Value   : TRUE if successful
* Comments         : 
*    This function sets the lock bits on all sectors
* 
*END*----------------------------------------------------------------------*/

boolean _intel_strata_set_lock_bits
   (  
      /* [IN] the base address of the device */
      IO_FLASHX_STRUCT_PTR  handle_ptr
   )
{ /* Body */
   boolean          result;
   MQX_TICK_STRUCT  start_ticks;

   switch (handle_ptr->WIDTH) {
      case 1:
         result = _intel_strata_setlockbits_1byte(handle_ptr, &start_ticks);
         break;
      case 2:
         result = _intel_strata_setlockbits_2byte(handle_ptr, &start_ticks);
         break;
      case 4:
         result = _intel_strata_setlockbits_4byte(handle_ptr, &start_ticks);
         break;
   } /* Endswitch */

   return result;

} /* Endbody */

/* EOF */
