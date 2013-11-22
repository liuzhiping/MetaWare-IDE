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
*** File: ttl.c
***
*** Comments: 
*** This file contains the task template list for the example.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include "main.h"

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   {MAIN_TASK,  main_task,  2000, 5, "main", 
      MQX_AUTO_START_TASK, 0, 0},
   {WRITE_TASK, write_task, 2000,  5, "write",
      0,                   0, 0},
   {READ_TASK,  read_task,  2000, 5, "read",
      0,                   0, 0},
   {0,          0,          0,    0, 0,
      0,                   0, 0}
};

/* EOF */
