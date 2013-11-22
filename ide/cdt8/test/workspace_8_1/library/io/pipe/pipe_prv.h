#ifndef _pipe_prv_h_
#define _pipe_prv_h_
/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
*** 
*** File: pipe_prv.h
*** 
*** Comments: The file contains the private internal functions prototype, defines, 
***           structure	definitions private to the Pipe device
*** 
***
***
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                          STRUCTURE DEFINITIONS
*/


/*
** IO_PIPE_INIT_STRUCT
**
** This structure defines the initialization parameters to be used
** when a Pipe is initialized.
*/

typedef struct io_pipe_init_struct
{
  
   /* The pipe character queue size */
   uint_32 QUEUE_SIZE;

   /* Initialization parameters for the pipe, currently not used*/
   uint_32 FLAGS;

 
} IO_PIPE_INIT_STRUCT, _PTR_ IO_PIPE_INIT_STRUCT_PTR;


/*
** IO_PIPE_INFO_STRUCT
**
** This structure defines the current parameters and status for a Pipe  
**
*/

typedef struct io_pipe_info_struct
{

   CHARQ_STRUCT_PTR     QUEUE;
  
   /* The serial pipe queue size */
   uint_32    QUEUE_SIZE;

   /* Mutext to protect against simulateous reads from the Pipe */ 
   MUTEX_STRUCT READ_MUTEX;

   /* Mutext to protect against simulateous writes to the Pipe */ 
   MUTEX_STRUCT WRITE_MUTEX;

   /* Mutext to protect against access to the Pipe data structures */ 
   MUTEX_STRUCT ACCESS_MUTEX;

   /* Semaphore used to block when pipe is full  */
   LWSEM_STRUCT FULL_SEM; 

   /* Semaphore used to block when pipe is empty */
   LWSEM_STRUCT EMPTY_SEM; 

   /* Flags to define options for the Pipe, currently not used */
   uint_32 FLAGS;

 
} IO_PIPE_INFO_STRUCT, _PTR_ IO_PIPE_INFO_STRUCT_PTR;


/*----------------------------------------------------------------------*/
/*
**                          EXTERNAL FUCTION DEFINITIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

extern _mqx_int _io_pipe_open(FILE_DEVICE_STRUCT_PTR, char _PTR_, char_ptr);
extern _mqx_int _io_pipe_close(FILE_DEVICE_STRUCT_PTR);
extern _mqx_int _io_pipe_read(FILE_DEVICE_STRUCT_PTR, char _PTR_, _mqx_int);
extern _mqx_int _io_pipe_write(FILE_DEVICE_STRUCT_PTR, char _PTR_, _mqx_int);
extern _mqx_int _io_pipe_ioctl(FILE_DEVICE_STRUCT_PTR, uint_32, pointer);
extern _mqx_int _io_pipe_uninstall(IO_DEVICE_STRUCT_PTR);

#ifdef __cplusplus
}
#endif

#endif

/* EOF */
