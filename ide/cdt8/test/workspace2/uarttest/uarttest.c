/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2003 ARC International.
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
***    This file contains the source for the hello example program.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

/* Task IDs */
#define HELLO_TASK 5

extern void echo_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{ 
    {HELLO_TASK, echo_task, 1500, 5, "hello", MQX_AUTO_START_TASK, 0, 0},
    {0,          0,          0,   0, 0,       0,                   0, 0}
};

/*TASK*-----------------------------------------------------
* 
* Task Name    : echo_task
* Comments     :
*    This task prints " Hello World "
*
*END*-----------------------------------------------------*/
void echo_task
   (
      uint_32 initial_data
   )
{
   printf("\nEnter a string:\n");
   char buf[200];
   scanf("%s",buf);
   printf("String entered: \x1B[34;1m%s\x1B[0m\n",buf);
   _mqx_exit(0);

}

/* EOF */
