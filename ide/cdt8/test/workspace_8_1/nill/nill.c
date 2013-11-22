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
*** File: nill.c
***
*** Comments: 
***    This is an example of an empty task.
***
*** Expected Output: None
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

/* Task IDs */
#define NILL_TASK 5

extern void nill_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{ 
    {NILL_TASK, nill_task, 1024, 5, "nill", MQX_AUTO_START_TASK, 0, 0},
    {0,         0,         0,    0, 0,      0,                   0, 0}
};

/*TASK*-----------------------------------------------------
* 
* Task Name    : nill_task
* Comments     :
*    This task does nothing
*
*END*-----------------------------------------------------*/
void nill_task
   (
      uint_32 initial_data
   )
{

   _mqx_exit(0);

}

/* EOF */
