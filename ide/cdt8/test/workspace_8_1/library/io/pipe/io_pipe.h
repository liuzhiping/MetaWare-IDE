#ifndef _io_pipe_h_
#define _io_pipe_h_
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
*** File: io_pipe.h
*** 
*** Comments: The file contains the public functions prototype, defines, 
***           structure definitions to the Pipe IO device
*** 
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                          CONSTANT DEFINITIONS
*/


/*
** PIPE IOCTL calls
*/
#define PIPE_IOCTL_GET_SIZE                  (0x0101)
#define PIPE_IOCTL_FULL                      (0x0102)
#define PIPE_IOCTL_EMPTY                     (0x0103)
#define PIPE_IOCTL_RE_INIT                   (0x0104)
#define PIPE_IOCTL_CHAR_AVAIL                (0x0105)
#define PIPE_IOCTL_NUM_CHARS_FULL            (0x0106)
#define PIPE_IOCTL_NUM_CHARS_FREE            (0x0107)



/*----------------------------------------------------------------------*/
/*
**                          EXTERN FUNCTION DEFINITIONS
*/




#ifdef __cplusplus
extern "C" {
#endif

extern uint_32  _io_pipe_install(char_ptr, uint_32, uint_32);

#ifdef __cplusplus
}
#endif

#endif

/* EOF */
