#ifndef __io_dun_h__
#define __io_dun_h__
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
*** File: io_dun.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions specific for the DUN and RAS drivers
***
**************************************************************************
*END*********************************************************************/

/*----------------------------------------------------------------------*/
/*
**                    TYPE DEFINITIONS
*/

/*
** RAS DEVICE STRUCT
** marks@11/10/03 Must be kept in sync with rtcs iodun.h
*/
typedef struct {
   char_ptr     RECV;
   char_ptr     SEND;
   _mqx_uint    RECV_SIZE;
   _mqx_uint    SEND_SIZE;
} IODUN_DEV_STRUCT, _PTR_ IODUN_DEV_STRUCT_PTR;

/*
** RAS FILE STRUCT
** marks@11/10/03 Must be kept in sync with rtcs iodun.h
*/
typedef struct {
   FILE_PTR       F_PTR;
   _mqx_uint      STATE;
   LWSEM_STRUCT   LWSEM;
   char_ptr       PARSE;
   char           C;
} IODUN_STRUCT, _PTR_ IODUN_STRUCT_PTR;


/*----------------------------------------------------------------------*/
/*
**                    FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif
 
 
_mqx_uint _io_ras_install(char_ptr);
_mqx_uint _io_dun_install(char_ptr);

extern _mqx_int _io_dun_open      (FILE_PTR, char_ptr, char_ptr);
extern _mqx_int _io_dun_close     (FILE_PTR);
extern _mqx_int _io_dun_write     (FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_dun_read      (FILE_PTR, char_ptr, _mqx_int);
extern _mqx_int _io_dun_ioctl     (FILE_PTR, _mqx_uint, pointer);
extern char     _io_dun_read_char (FILE_PTR);

#ifdef __cplusplus
}
#endif


#endif
/* EOF */
