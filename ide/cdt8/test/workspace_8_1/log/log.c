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
*** File: log.c
***
*** Comments: 
***    This example crates a log to record 10 keystroke entries 
***    then prints out the log.
***
***    When using the simulator, data entry must be set to read from a file
***    using the Terminal/COM Simulator options
***
*** Expected Output:
*** please type in 10 characters:
*** 1234567890
*** Log contains:
*** Time: 0.000241, c=1, i=0
*** Time: 0.000251, c=2, i=1
*** Time: 0.000261, c=3, i=2
*** Time: 0.000271, c=4, i=3
*** Time: 0.000281, c=5, i=4
*** Time: 0.000291, c=6, i=5
*** Time: 0.000300, c=7, i=6
*** Time: 0.000310, c=8, i=7
*** Time: 0.000320, c=9, i=8
*** Time: 0.000330, c=0, i=9
***
*** Times may vary slightly, data depends on input
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <log.h>

#define MAIN_TASK  10
#define MY_LOG     1
extern void main_task(uint_32 initial_data);
 
TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { MAIN_TASK, main_task, 2000, 8, "Main",
      MQX_AUTO_START_TASK, 0 ,0},
   { 0,         0,         0,    0, 0,
      0, 0, 0}
};

typedef struct entry_struct
{
   LOG_ENTRY_STRUCT   HEADER;
   _mqx_uint          C;
   _mqx_uint          I;
} ENTRY_STRUCT, _PTR_ ENTRY_STRUCT_PTR;


/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  :
*   This task logs 10 keystroke entries then prints out the log.
*END*-----------------------------------------------------------*/

void main_task
   (
      uint_32 initial_data
   )
{
   ENTRY_STRUCT entry;
   _mqx_uint    result;
   _mqx_uint    i;
   uchar        c;

   /* Create the log component */
   result = _log_create_component();
   if (result != MQX_OK) {
      printf("Main task: _log_create_component failed");
      _mqx_exit(0);  
   }

   /* Create a log */
   result = _log_create(MY_LOG,
      10 * (sizeof(ENTRY_STRUCT)/sizeof(_mqx_uint)), 0);
   if (result != MQX_OK) {
      printf("Main task: _log_create failed");   
      _mqx_exit(0);  
   }

   /* Write the data to the log */   
   printf("Please type in 10 characters:\n");
   for (i = 0; i < 10; i++) {
      c = getchar();
      result = _log_write(MY_LOG, 2, (_mqx_uint)c, i);
      if (result != MQX_OK) {
         printf("Main task: _log_write failed");   
      }
   }

   /* Read data from the log */
   printf("\nLog contains:\n");
   while (_log_read(MY_LOG, LOG_READ_OLDEST_AND_DELETE, 2,
      (LOG_ENTRY_STRUCT_PTR)&entry) == MQX_OK)
   {
      printf("Time: %ld.%03d%03d, c=%c, i=%d\n",
         entry.HEADER.SECONDS,
         (_mqx_uint)entry.HEADER.MILLISECONDS,
         (_mqx_uint)entry.HEADER.MICROSECONDS,
         (uchar)entry.C & 0xff,
         entry.I);
   }

   /* Destroy the log */
   _log_destroy(MY_LOG);

   _mqx_exit(0);

}

/* EOF */
