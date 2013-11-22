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




#ifndef _GRAPI_TYPES_H
#define _GRAPI_TYPES_H 1

#include "common_types.h"

/* graphics pipeline message types */
#define GR_CMD_NEW_FRAME          1
#define GR_CMD_SHOW               2
#define GR_CMD_OBSERVATION_WINDOW 3
#define GR_CMD_OBSERVATION_POINT  4
#define GR_CMD_ZPOINT             5
#define GR_CMD_ZMOVETO            6
#define GR_CMD_ZLINETO            7
#define GR_CMD_ZRMOVETO           8
#define GR_CMD_ZRLINETO           9
#define GR_CMD_SET_DRAW_CHAR      10
#define GR_CMD_PUT_STRING         11

typedef struct _newframe {
  int frame_number;
} newframe;

typedef struct _point2d
{
  int x;
  int y;
} point2d;

typedef struct _point3d
{
  real x;
  real y;
  real z;
} point3d;

typedef struct _bbox   
{
  real l;
  real r;
  real b;
  real t;
} bbox;

#define MAXSTRLN 80

typedef struct _posit_string {
  point2d p;
  char str[MAXSTRLN];
} posit_string;


typedef struct _gr_message {

  int reserved;

  int gr_cmd;   /* graphics pipeline message type */

  union {

    newframe f;

    point3d point;

    bbox    box;

    posit_string s;

  } msg;

} gr_message;


typedef struct _msghldr {
  void *msg;
} msghldr;

#endif /* _GRAPI_TYPES_H */

