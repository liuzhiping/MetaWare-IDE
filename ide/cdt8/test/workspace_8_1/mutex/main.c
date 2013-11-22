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
*** File: mutex.c
***
*** Comments: 
***    This example demonstrates the use of mutexes. The program
***    creates 2 tasks that contend for serial output. The use of
***    a mutex insures that each task's message is printed in order.
***
*** Expected Output:
*** Hello from Print task 1
*** Hello from Print task 1
*** Hello from Print task 1
*** Hello from Print task 1
*** Hello from Print task 1
*** Print task 2 is alive
*** Hello from Print task 1
*** Print task 2 is alive
*** Hello from Print task 1
*** Print task 2 is alive
*** Hello from Print task 1
*** Print task 2 is alive
*** Hello from Print task 1
*** 
*** Pattern Repeats
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <mutex.h>

/* Task IDs */
#define MAIN_TASK     5
#define PRINT_TASK    6

extern void main_task(uint_32 initial_data);
extern void print_task(uint_32 initial_data);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   {MAIN_TASK,  main_task,  600, 5, "main",  
      MQX_AUTO_START_TASK, 0, 0},
   {PRINT_TASK, print_task, 600, 6, "print", 
      MQX_TIME_SLICE_TASK, 0, 3},
   {0,          0,          0,   0, 0,
      0,                   0, 0}
};

MUTEX_STRUCT   print_mutex;

/*TASK*--------------------------------------------------------
*
* Task Name : main_task
* Comments  : This task creates a mutex and then two 
*             instances of the print task.
*END*--------------------------------------------------------*/

void main_task
   (
      uint_32 initial_data
   )
{
   MUTEX_ATTR_STRUCT mutexattr;
   char*             string1 = "Hello from Print task 1\n";
   char*             string2 = "Print task 2 is alive\n";

   /* Initialize mutex attributes */
   if (_mutatr_init(&mutexattr) != MQX_OK) {
      printf("Initialize mutex attributes failed.\n");
      _mqx_exit(0);
   }
   
   /* Initialize the mutex */ 
   if (_mutex_init(&print_mutex, &mutexattr) != MQX_OK) {
      printf("Initialize print mutex failed.\n");
      _mqx_exit(0);
   }
   /* Create the print tasks */
   _task_create(0, PRINT_TASK, (uint_32)string1);
   _task_create(0, PRINT_TASK, (uint_32)string2);

   _task_block();
}   

/*TASK*--------------------------------------------------------
*
* Task Name : print_task
* Comments  : This task prints a message. It uses a mutex to 
*             ensure I/O is not interleaved.
*END*--------------------------------------------------------*/

void print_task
   (
      uint_32 initial_data
   )
{

   while(TRUE) {
      if (_mutex_lock(&print_mutex) != MQX_OK) {
         printf("Mutex lock failed.\n");
         _mqx_exit(0);
      }
      _io_puts((char *)initial_data);
      _mutex_unlock(&print_mutex);
   }
}

/* EOF */
