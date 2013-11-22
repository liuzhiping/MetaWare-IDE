/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2007 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: taskat.c
***
*** Comments:
***   This file contains a demo for creating a task AT a specified
***   address (using the specified memory for the task stack and TD
***   structure).
***
*** Expected Output:
*** main_task: Task create at 0x00081738..0x00081f08
*** test_task: Local var at 0x00081edc and param 0x12345678
*** PASSED
***
*** Addresses may vary.
***
**************************************************************************
*END*********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

#define MAIN_TASK               10
#define TEST_TASK1              11
#define TEST_TASK1_PARAM        0x12345678

extern void main_task(uint_32);
extern void test_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] =
{
{ MAIN_TASK,  main_task, 4000,  9, "Main",  MQX_AUTO_START_TASK,  0},
{ TEST_TASK1, test_task, 0,     8, "test",  0,                    0},
{ 0,          0,         0,     0, 0,       0,                    0}
};

#define TEST_STACK_SIZE 2000
// Should be ABI aligned ... cannot do so portably....
double test_task_stack[TEST_STACK_SIZE/sizeof(double)];

volatile uint_32 test_task_val = 0;

/*TASK*-------------------------------------------------------------------
*
* Task Name    : test_task
* Comments     :
*   created task
*
*END*----------------------------------------------------------------------*/

void test_task
   (
      uint_32 param
   )
{/* Body */
   char buf[8];

   test_task_val = 1;
   printf("test_task: Local var at 0x%08x and param 0x%08x\n", buf, param);
}/* Endbody */

/*TASK*-------------------------------------------------------------------
*
* Task Name    : main_task
* Comments     :
*   starts up the watchdog and tests it.
*
*END*----------------------------------------------------------------------*/

void main_task
   (
      uint_32 param
   )
{/* Body */
   _task_id tid;

   printf("main_task: Task create at 0x%08x..0x%08x\n",
      test_task_stack, (uchar_ptr)test_task_stack + TEST_STACK_SIZE);

   tid = _task_create_at(0, TEST_TASK1, TEST_TASK1_PARAM, test_task_stack,
      TEST_STACK_SIZE);

   if (tid == MQX_NULL_TASK_ID) {
      printf("task create at failed");
      _mqx_fatal_error(MQX_INVALID_TASK_ID);
   } /* Endif */

   if (test_task_val != 1) {
      printf("ERROR: test_task_val != 1\n");
   } else {
      printf("PASSED\n");
   }/* Endif */

   _mqx_exit(0);

} /* Endbody */

/* EOF */
