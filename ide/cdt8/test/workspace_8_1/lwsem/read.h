#ifndef __read_h__
#define __read_h__
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
*** File: read.h
***
*** Comments: 
***    This file contains the definitions for this example.
*** 
***************************************************************************
*END**********************************************************************/


/* Number of Writer Tasks */
#define NUM_WRITERS  3

/* Task IDs */
#define WRITE_TASK   5
#define READ_TASK    6

/* 
** Global data structure accessible by read and write tasks. 
** It contains two lightweight semaphores that govern access to 
** the data variable.
*/  
typedef struct sw_fifo
{
   LWSEM_STRUCT   READ_SEM;
   LWSEM_STRUCT   WRITE_SEM;
   uchar          DATA;
} SW_FIFO, _PTR_ SW_FIFO_PTR;


/* Function prototypes */
extern void write_task(uint_32 initial_data);
extern void read_task(uint_32 initial_data);

extern SW_FIFO fifo;

#endif
/* EOF */
