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
***    This file contains the task template list.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include "read.h"

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   {WRITE_TASK, write_task, 800, 5, "write",
      0,                   0, 0},
   {READ_TASK,  read_task,  800, 5, "read",
      MQX_AUTO_START_TASK, 0, 0},
   {0,          0,          0,   0, 0,
      0,                   0, 0}
};

/* EOF */
