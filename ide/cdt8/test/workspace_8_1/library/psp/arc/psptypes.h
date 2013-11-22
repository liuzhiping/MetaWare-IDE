#ifndef __psptypes_h__
#define __psptypes_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: psptypes.h
***
*** Comments: 
***    This file contains the definitions of the basic MQX types.
*** 
***************************************************************************
*END**********************************************************************/


/*--------------------------------------------------------------------------*/
/*
**                            STANDARD TYPES
*/

/*
**  The following typedefs allow us to minimize portability problems
**  due to the various C compilers (even for the same processor) not
**  agreeing on the sizes of "int"s and "short int"s and "longs".
*/

#define _PTR_      *
#define _CODE_PTR_ *

typedef char _PTR_                    char_ptr;    /* signed character       */
typedef unsigned char  uchar, _PTR_   uchar_ptr;   /* unsigned character     */

typedef signed   char   int_8, _PTR_   int_8_ptr;   /* 8-bit signed integer   */
typedef unsigned char  uint_8, _PTR_  uint_8_ptr;  /* 8-bit signed integer   */

typedef          short int_16, _PTR_  int_16_ptr;  /* 16-bit signed integer  */
typedef unsigned short uint_16, _PTR_ uint_16_ptr; /* 16-bit unsigned integer*/

typedef          long  int_32, _PTR_  int_32_ptr;  /* 32-bit signed integer  */
typedef unsigned long  uint_32, _PTR_ uint_32_ptr; /* 32-bit unsigned integer*/

typedef    long  long  int_64, _PTR_  int_64_ptr;       /* 64-bit signed   */
typedef unsigned long long  uint_64, _PTR_ uint_64_ptr; /* 64-bit unsigned */

typedef unsigned long  boolean;  /* Machine representation of a boolean */

typedef void _PTR_     pointer;  /* Machine representation of a pointer */

/* IEEE single precision floating point number (32 bits, 8 exponent bits) */
typedef float          ieee_single;

/* IEEE double precision floating point number (64 bits, 11 exponent bits) */
typedef double         ieee_double;

/* Type for the CPU's natural size */
typedef uint_32  _mqx_uint, _PTR_ _mqx_uint_ptr;
typedef int_32   _mqx_int,  _PTR_ _mqx_int_ptr;

/* How big a data pointer is on this processor */
typedef uint_32  _psp_data_addr, _PTR_ _psp_data_addr_ptr;

/* How big a code pointer is on this processor */
typedef uint_32  _psp_code_addr, _PTR_ _psp_code_addr_ptr;

/* Maximum type */
typedef uint_32  _mqx_max_type, _PTR_ _mqx_max_type_ptr;

/* _mem_size is equated to the a type that can hold the maximum data address */
typedef uint_32 _mem_size, _PTR_ _mem_size_ptr;

/* Used for file sizes. */
typedef uint_32       _file_size;
typedef int_32        _file_offset;

/*--------------------------------------------------------------------------*/
/*
**                         DATATYPE VALUE RANGES
*/

#define MAX_CHAR      (0x7F)
#define MAX_UCHAR     (0xFF)
#define MAX_INT_8     (0x7F)
#define MAX_UINT_8    (0xFF)
#define MAX_INT_16    (0x7FFF)
#define MAX_UINT_16   (0xFFFF)
#define MAX_INT_32    (0x7FFFFFFFL)
#define MAX_UINT_32   (0xFFFFFFFFUL)
#define MAX_INT_64    (0x7FFFFFFFFFFFFFFFLL)
#define MAX_UINT_64   (0xFFFFFFFFFFFFFFFFULL)

#define MIN_FLOAT     (8.43E-37)
#define MAX_FLOAT     (3.37E+38)

#define MIN_DOUBLE    (2.225074E-308)
#define MAX_DOUBLE    (1.797693E+308)

#define MAX_MQX_UINT         (MAX_UINT_32)
#define MAX_MQX_INT          (MAX_INT_32)
#define MAX_FILE_SIZE        (MAX_UINT_32)
#define MAX_MEM_SIZE         (MAX_UINT_32)
#define MAX_MQX_MAX_TYPE     (MAX_UINT_32)
#define MQX_INT_SIZE_IN_BITS (32)

#endif
/* EOF */
