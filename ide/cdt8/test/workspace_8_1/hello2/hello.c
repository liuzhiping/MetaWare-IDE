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
***    A multi task version of Hello World. Has 2 tasks.
***    Hello and World. World starts first and then creates Hello
***    World then prints World, but since Hello is a higher priority
***    task, it prints Hello first.
***
*** Expected Output:
***
*** Hello 
*** World
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

/* Task IDs */
#define HELLO_TASK  5
#define WORLD_TASK  6

extern void hello_task(uint_32);
extern void world_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{  
    {WORLD_TASK, world_task, 600, 5, "world",
       MQX_AUTO_START_TASK, 0, 0},
    {HELLO_TASK, hello_task, 600, 4, "hello",
       0,                   0, 0},
    {0,          0,          0,   0, 0,
       0,                   0, 0}
};

/*TASK*-----------------------------------------------------
* 
* Task Name    : world_task
* Comments     :
*    This task creates hello_task and then prints " World ".
*
*END*-----------------------------------------------------*/

void world_task
   (
      uint_32 initial_data
   )
{
   _task_id hello_task_id;

   hello_task_id = _task_create(0, HELLO_TASK, 0);
   if (hello_task_id == MQX_NULL_TASK_ID) {
      printf ("\n Could not create hello_task\n");
   } else {
      printf(" World \n");
   }
   _mqx_exit(0);

}


/*TASK*-----------------------------------------------------
* 
* Task Name    : hello_task
* Comments     :
*    This task prints " Hello".
*
*END*-----------------------------------------------------*/

void hello_task
   (
      uint_32 initial_data
   )
{

   printf("\n Hello\n");
   _task_block();

}

/* EOF */
