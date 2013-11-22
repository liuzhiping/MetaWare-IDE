/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: kl_disp.c
***
*** Comments:      
***   This file contains the display function for the Kernel Data Logging
*** facility.  This function reads and displays one kernel log entry.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwlog.h"
#include "klog.h"
#include "fio.h"

extern char _PTR_ _klog_get_function_name_internal(uint_32);


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_display
* Returned Value   : boolean returns FALSE if log is empty TRUE otherwise
* Comments         :
*   This function prints out one kernel log entry
*
*END*----------------------------------------------------------------------*/

boolean _klog_display
   (
      void
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

   LWLOG_ENTRY_STRUCT      log_entry;
   LWLOG_ENTRY_STRUCT_PTR  log_ptr;
   _mqx_uint               result;
   _mqx_int                i;

   log_ptr = &log_entry;
   result = _lwlog_read(LOG_KERNEL_LOG_NUMBER, LOG_READ_OLDEST_AND_DELETE, log_ptr);
   if (result != MQX_OK) {
      return FALSE;
   } /* Endif */

#if MQX_LWLOG_TIME_STAMP_IN_TICKS == 0
   /* Normalize the time in the record */
   log_ptr->MILLISECONDS += log_ptr->MICROSECONDS / 1000;
   log_ptr->MICROSECONDS  = log_ptr->MICROSECONDS % 1000;
   log_ptr->SECONDS      += log_ptr->MILLISECONDS / 1000;
   log_ptr->MILLISECONDS  = log_ptr->MILLISECONDS % 1000;

   printf("%ld. %ld:%03ld%03ld -> ",
      (uint_32)log_ptr->SEQUENCE_NUMBER, 
      log_ptr->SECONDS,
      log_ptr->MILLISECONDS, 
      log_ptr->MICROSECONDS);
#else

   printf("%ld. ", (uint_32)log_ptr->SEQUENCE_NUMBER);

   PSP_PRINT_TICKS(&log_ptr->TIMESTAMP);

   printf(" -> ");
#endif

   switch (log_ptr->DATA[0]) {

      case KLOG_FUNCTION_ENTRY:
      case KLOG_FUNCTION_EXIT:
         printf("%s %22.22s ", 
            (log_ptr->DATA[0] == KLOG_FUNCTION_ENTRY) ? "FUN " : "XFUN",
            _klog_get_function_name_internal((uint_32)log_ptr->DATA[1]));
         /* Start CR 238 */
         /* for (i = 2; i < LWLOG_MAXIMUM_DATA_ENETRIES; ++i) { */
         for (i = 2; i < LWLOG_MAXIMUM_DATA_ENTRIES; ++i) {
         /* End CR 238 */
            printf("0x%lX ", (uint_32)log_ptr->DATA[i]);
         } /* Endfor */
         printf("\n");
         break;

      case KLOG_INTERRUPT:
         printf("INT   0x%lX\n", (uint_32)log_ptr->DATA[1]);
         break;

      case KLOG_INTERRUPT_END:
         printf("INT   0x%lX END\n",(uint_32)log_ptr->DATA[1]);
         break;

      case KLOG_CONTEXT_SWITCH:
         printf("NEW TASK TD 0x%lX ID 0x%lX STATE 0x%lX STACK 0x%lX\n",
            (uint_32)log_ptr->DATA[1], (uint_32)log_ptr->DATA[2], 
            (uint_32)log_ptr->DATA[3], (uint_32)log_ptr->DATA[4]);
            break;

      default:
         printf("USER ENTRY: 0x%lX:", (uint_32)log_ptr->DATA[0]);
         /* Start CR 238 */
         /* for (i = 1; i < LWLOG_MAXIMUM_DATA_ENETRIES; ++i) { */
         for (i = 1; i < LWLOG_MAXIMUM_DATA_ENTRIES; ++i) {
         /* End CR 238 */
            printf("0x%lX ", (uint_32)log_ptr->DATA[i]);
         } /* Endfor */
         printf("\n");
         break;
   } /* Endswitch */

   return TRUE;

/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */

/* EOF */
