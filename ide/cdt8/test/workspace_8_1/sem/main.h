#ifndef __main_h__
#define __main_h__
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
*** File: main.h
***
*** Comments: 
*** This file contains definitions for the semaphore example.
*** 
***************************************************************************
*END**********************************************************************/

#define MAIN_TASK     5
#define WRITE_TASK    6
#define READ_TASK     7
#define ARRAY_SIZE    5
#define NUM_WRITERS   2

/* 
** Global data structure that is accessible by read and write tasks.
** Contains a data array that simulates a fifo. The read_index
** and write_index mark the location in the array that the read
** and write tasks are accessing. All data is protected by
** semaphores.
*/ 

typedef struct sw_fifo
{
   _task_id  DATA[ARRAY_SIZE];
   uint_32   READ_INDEX;
   uint_32   WRITE_INDEX; 
} SW_FIFO, _PTR_ SW_FIFO_PTR;

/* Funtion prototypes */
extern void main_task(uint_32 initial_data);
extern void write_task(uint_32 initial_data);
extern void read_task(uint_32 initial_data);

extern    SW_FIFO fifo;

#endif
/* EOF */
