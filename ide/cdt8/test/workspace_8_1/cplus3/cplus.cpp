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
*** File: cplus.cpp
***
*** Comments:
***    This examples recursively tests C++ exceptions.
***     
*** Expected Output:
***
*** Testing C++ exceptions
*** Caught 1
*** Caught 1
*** Caught 2
*** Caught 2
*** Caught 3
*** Caught 3
*** ** Passed **
***
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>
#include "cplus.h"

/* Task IDs */
#define CPLUS_TASK  5
#define EXCEPT_TASK 6

extern void cplus_task(uint_32);
extern void except_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] =
{
#ifdef MCF5213_PROCESSOR
    {CPLUS_TASK,  cplus_task,  800, 8, "cplus",  MQX_AUTO_START_TASK, },
    {EXCEPT_TASK, except_task, 1500, 9, "except", },
#else
    {CPLUS_TASK,  cplus_task,  4000, 8, "cplus",  MQX_AUTO_START_TASK, },
    {EXCEPT_TASK, except_task, 4000, 9, "except", },
#endif
    {0, }
};

static uint_32 result = 0;

/*Function*-------------------------------------------------
*
* Task Name    : recursive_try
* Comments     :
*    This function tests C++ exceptions
*
*END*-----------------------------------------------------*/
static void recursive_try
   (
      int pass
   )
{
   try {
      throw_func(pass);
   }
   catch(const eclass1*) {
      result += 1;
      printf("Caught 1\n");
      _sched_yield();
      recursive_try(pass+1);
   }
   catch(const eclass2*) {
      result += 4;
      printf("Caught 2\n");
      _sched_yield();
      recursive_try(pass+1);
   }
   catch(const eclass3*) {
      result += 16;
      printf("Caught 3\n");
   }
}

/*TASK*-----------------------------------------------------
*
* Task Name    : cplus_task
* Comments     :
*    This task prints the state of each object
*
*END*-----------------------------------------------------*/
void cplus_task
   (
      uint_32 initial_data
   )
{
   printf("\nTesting C++ exceptions\n");
   _task_create(0, EXCEPT_TASK, 0);
   _task_create(0, EXCEPT_TASK, 0);
   _time_delay(200);
   if (result == 0x2A) {
      printf("** Passed **\n");
   } else {
      printf("** FAILED **\n");
   } /* Endif */
   fflush(stdout);
    
   _mqx_exit(0);
}

/*TASK*-----------------------------------------------------
*
* Task Name    : cplus_task
* Comments     :
*    This task prints the state of each object
*
*END*-----------------------------------------------------*/
void except_task
   (
      uint_32 initial_data
   )
{
   recursive_try(1);
}

/* EOF */
