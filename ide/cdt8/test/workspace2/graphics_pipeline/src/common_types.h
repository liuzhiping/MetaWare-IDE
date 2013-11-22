/************************************************************************/
/*                                                                      */
/* Copyright (C) ARC International, Inc. 2007.                          */
/*                                                                      */
/* All Rights Reserved.                                                 */
/*                                                                      */
/* This software is the property of ARC International, Inc.  It is      */
/* furnished under a specific licensing agreement.  It may be used or   */
/* copied only under terms of the licensing agreement.                  */
/*                                                                      */
/* For more information, contact support@arc.com                        */
/*                                                                      */
/************************************************************************/


#ifndef _BASIC_TYPES_H
#define _BASIC_TYPES_H 1

#define PI    3.14159265
#define TPI   (2.0*PI)

typedef unsigned char byte;

#if 1
typedef float real;
#else
typedef double real;
#endif


#define MP_FAIL_CHAR      '~'
#define CHANNEL_FULL_CHAR '|'

#endif /* _BASIC_TYPES_H */

