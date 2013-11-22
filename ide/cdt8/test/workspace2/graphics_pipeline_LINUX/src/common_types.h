/************************************************************************/
/************************************************************************/
/**                                                                     */
/** Copyright (c) ARC International 2008.                               */
/** All rights reserved                                                 */
/**                                                                     */
/** This software embodies materials and concepts which are             */
/** confidential to ARC International and is made                       */
/** available solely pursuant to the terms of a written license         */
/** agreement with ARC International                                    */
/**                                                                     */
/** For more information, contact support@arc.com                       */
/**                                                                     */
/**                                                                     */
/************************************************************************/
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

