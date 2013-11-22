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

#ifndef NULL
#define NULL ((void *) 0)
#endif

#define SLEEPTM 160

typedef struct _display_msg {

  int frame_number;   /* unique frame number */

  void *frame;        /* pointer to the frame */

} display_msg;


