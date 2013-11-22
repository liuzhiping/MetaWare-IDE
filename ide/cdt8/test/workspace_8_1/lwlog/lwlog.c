/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: lwlog.c
***
*** Comments: 
***    This example crates a lightweight log to record 10 keystroke entries 
***    then prints out the log.
***
***    When using the simulator, data entry must be set to read from a file
***    using the Terminal/COM Simulator options
***
*** Expected Output:
*** please type in 10 characters:
*** 1234567890
*** Log contains:
*** Time: 0x00000000:5703, c = 1, i=0
*** Time: 0x00000000:58FD, c = 2, i=1
*** Time: 0x00000000:5AF7, c = 3, i=2
*** Time: 0x00000000:5CF1, c = 4, i=3
*** Time: 0x00000000:5EEB, c = 5, i=4
*** Time: 0x00000000:60E5, c = 6, i=5
*** Time: 0x00000000:62DF, c = 7, i=6
*** Time: 0x00000000:64D9, c = 8, i=7
*** Time: 0x00000000:66D3, c = 9, i=8
*** Time: 0x00000000:68CD, c = 0, i=9
***
*** Times may vary slightly, data depends on input
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <lwlog.h>

#define MAIN_TASK  10
#define MY_LOG     1

extern void main_task(uint_32 initial_data);


TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { MAIN_TASK, main_task, 2000, 8, "Main",
      MQX_AUTO_START_TASK, 0, 0},
   { 0,         0,         0,    0, 0,
      0,                   0, 0}
};

/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  :
*   This task logs 10 keystroke entries in a lightweight log,
*   then prints out the log.
*END*-----------------------------------------------------------*/

void main_task 
   (
      uint_32 initial_data
   )
{
   LWLOG_ENTRY_STRUCT entry;
   _mqx_uint          result;
   _mqx_uint          i;
   uchar              c;

   /* Create the lightweight log component */
   result = _lwlog_create_component();
   if (result != MQX_OK) {
      printf("Main task: _lwlog_create_component failed.");
      _mqx_exit(0);  
   }

   /* Create a log */
   result = _lwlog_create(MY_LOG, 10, 0);
   if (result != MQX_OK) {
      printf("Main task: _lwlog_create failed.");   
      _mqx_exit(0);  
   }

   /* Write data to the log */   
   printf("Enter 10 characters:\n");
   for (i = 0; i < 10; i++) {
      c = getchar();
      result = _lwlog_write(MY_LOG, (_mqx_max_type)c,
         (_mqx_max_type)i, 0, 0, 0, 0, 0);
      if (result != MQX_OK) {
         printf("Main task: _lwlog_write failed.");   
      }
   }

   /* Read data from the log */
   printf("\nLog contains:\n");
   while (_lwlog_read(MY_LOG, LOG_READ_OLDEST_AND_DELETE,
      &entry) == MQX_OK)
   {
      printf("Time: ");
#if MQX_LWLOG_TIME_STAMP_IN_TICKS
      _psp_print_ticks((PSP_TICK_STRUCT_PTR)&entry.TIMESTAMP);
#else
      printf("%ld.%03ld%03ld", entry.SECONDS, entry.MILLISECONDS,
         entry.MICROSECONDS);
#endif
      printf(", c = %c, i=%d\n", (uchar)entry.DATA[0] & 0xff,
         (_mqx_uint)entry.DATA[1]);
   }
   
   /* Destroy the log */
   _log_destroy(MY_LOG);

   _mqx_exit(0);

}

/* EOF */
