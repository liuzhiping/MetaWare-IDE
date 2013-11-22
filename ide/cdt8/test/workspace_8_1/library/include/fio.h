#ifndef __fio_h__
#define __fio_h__
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
*** File: fio.h
***
*** Comments:      
***   This file is the header file for the standard formatted I/O library 
*** provided with mqx.
***
*** $Header:fio.h, 8, 5/14/2004 4:00:24 PM, $
***
*** $NoKeywords$
**************************************************************************
*END*********************************************************************/

/* Include for variable length argument functions */
#include <stdarg.h>

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/

/* Maximum line size for scanf */
#define IO_MAXLINE  (256)

/* map function names to mqx function names */
/* CR1343 & CR1455 #ifndef __cplusplus */
#ifndef MQX_SUPPRESS_STDIO_MACROS
   #define  clearerr   _io_clearerr
   #define  fclose     _io_fclose
   #define  feof       _io_feof
   #define  ferror     _io_ferror
   #define  fflush     _io_fflush
   #define  fgetc      _io_fgetc
   #define  fgetline   _io_fgetline
   #define  fgets      _io_fgets
   #define  fopen      _io_fopen
   #define  fprintf    _io_fprintf
   #define  fputc      _io_fputc
   #define  fputs      _io_fputs
   #define  fscanf     _io_fscanf
   #define  fseek      _io_fseek
   #define  fstatus    _io_fstatus
   #define  ftell      _io_ftell
   #define  fungetc    _io_fungetc
   #define  ioctl      _io_ioctl 
   #define  printf     _io_printf
   #define  putc       _io_fputc
   #define  read       _io_read
   #define  scanf      _io_scanf
   #define  sprintf    _io_sprintf
   #define  sscanf     _io_sscanf
   #define  vprintf    _io_vprintf
   #define  vfprintf   _io_vfprintf
   #define  vsprintf   _io_vsprintf
   #define  write      _io_write
   /* fread and fwrite do not read/write chars but objects */
   #define  fread(ptr,so,no,f)  (_io_read(f,ptr,(so)*(no))/(so))
   #define  fwrite(ptr,so,no,f) (_io_write(f,ptr,(so)*(no))/(so))
#endif

/*--------------------------------------------------------------------------*/
/*
**                        MACRO DECLARATIONS
*/

#define stdin     (FILE_PTR)_io_get_handle(IO_STDIN)
#define stdout    (FILE_PTR)_io_get_handle(IO_STDOUT)
#define stderr    (FILE_PTR)_io_get_handle(IO_STDERR)

#define getchar()    _io_fgetc(stdin)
#define getline(x,y) _io_fgetline(stdin, (x), (y))
#define gets(x)      _io_fgets((x), 0, stdin)
#define putchar(c)   _io_fputc((c), stdout)
#define puts(s)      _io_fputs((s), stdout)
#define status()     _io_fstatus(stdin)
#define ungetc(c)    _io_fungetc(c, stdin)

/*--------------------------------------------------------------------------*/
/*
**                            DATATYPE DECLARATIONS
*/

/*
** FILE STRUCTURE
**
** This structure defines the information kept in order to implement
** ANSI 'C' standard I/O stream.
*/
typedef struct file_struct
{
    
    /* The address of the Device for this stream */
    struct io_device_struct _PTR_ DEV_PTR;

    /* Device Driver specific information */
    pointer       DEV_DATA_PTR;

    /* General control flags for this stream */
    _mqx_uint     FLAGS;
    
    /* The current error for this stream */
    _mqx_uint     ERROR;

    /* The current position in the stream */
    _file_size    LOCATION;

    /* The current size of the file */
    _file_size    SIZE;

    /* The following 2 implement undelete */
    boolean       HAVE_UNGOT_CHARACTER;
    _mqx_int      UNGOT_CHARACTER;

} FILE, _PTR_ FILE_PTR;

#define _FILE_PTR_DEFINED

/*--------------------------------------------------------------------------*/
/*
**                      FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
/* ANSI 'C' library function prototypes */
extern void        _io_clearerr(FILE_PTR);
extern _mqx_int    _io_fclose(FILE_PTR);
extern _mqx_int    _io_feof(FILE_PTR);
extern _mqx_int    _io_ferror(FILE_PTR);
extern _mqx_int    _io_fflush(FILE_PTR);
extern _mqx_int    _io_fgetc(FILE_PTR);
extern _mqx_int    _io_fgetline(FILE_PTR, char _PTR_, _mqx_int);
extern char _PTR_  _io_fgets(char _PTR_, _mqx_int, FILE_PTR);
extern FILE_PTR    _io_fopen(const char _PTR_, const char _PTR_);
extern _mqx_int    _io_fprintf(FILE_PTR, const char _PTR_, ... );
extern _mqx_int    _io_fputc(_mqx_int, FILE_PTR);
extern _mqx_int    _io_fputs(const char _PTR_, FILE_PTR);
extern _mqx_int    _io_fscanf(FILE_PTR, const char _PTR_, ... );
extern _mqx_int    _io_fseek(FILE_PTR, _file_offset, _mqx_uint);
extern boolean     _io_fstatus(FILE_PTR);
extern _mqx_int    _io_ftell(FILE_PTR);
extern _mqx_int    _io_fungetc(_mqx_int, FILE_PTR);
extern _mqx_int    _io_ioctl(FILE_PTR, _mqx_uint, pointer);
extern double      _io_modf(double, double _PTR_);
extern _mqx_int    _io_printf(const char _PTR_ , ... );
extern _mqx_int    _io_read(FILE_PTR, pointer, _mqx_int);
extern _mqx_int    _io_scanf(const char _PTR_ , ... );
extern _mqx_int    _io_sprintf(char _PTR_ , const char _PTR_ , ... );
extern _mqx_int    _io_sscanf(char _PTR_ , char _PTR_ , ... );
extern double      _io_strtod(char _PTR_, char _PTR_ _PTR_);
extern _mqx_int    _io_vprintf(const char _PTR_, va_list);
extern _mqx_int    _io_vfprintf(FILE_PTR, const char _PTR_, va_list);
extern _mqx_int    _io_vsprintf(char _PTR_, const char _PTR_, va_list);
extern _mqx_int    _io_write(FILE_PTR, pointer, _mqx_int);

/* 
** functions mapped out as macros in 'C' but provided for assembler functions
*/
extern _mqx_int    _io_getchar(void);
extern _mqx_int    _io_getline(char _PTR_, _mqx_int);
extern char _PTR_  _io_gets(char _PTR_);
extern _mqx_int    _io_putchar(_mqx_int);
extern _mqx_int    _io_puts(char _PTR_);
extern boolean     _io_status(void);
extern _mqx_int    _io_ungetc(_mqx_int);
#endif

/*==========================================================================*/

#ifdef __cplusplus
}
#endif

/* Include for I/O sub-system */
#include <io.h>

#endif
/* EOF */
