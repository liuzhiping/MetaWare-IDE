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
*** File: hello.c
***
*** Comments: 
***    A simple, single auto-start task that prints Hello World.
***
*** Expected Output:
***
*** Hello World
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

/* Task IDs */
#define HELLO_TASK 5

extern void hello_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{ 
    {HELLO_TASK, hello_task, 1500, 5, "hello", MQX_AUTO_START_TASK, 0, 0},
    {0,          0,          0,   0, 0,       0,                   0, 0}
};

/*TASK*-----------------------------------------------------
* 
* Task Name    : hello_task
* Comments     :
*    This task prints " Hello World "
*
*END*-----------------------------------------------------*/
void hello_task
   (
      uint_32 initial_data
   )
{

   printf("\n Hello World \n"); 
   _mqx_exit(0);

}

/* EOF */
