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



#ifndef NULL
#define NULL ((void *) 0)
#endif

#define SLEEPTM 160

typedef struct _display_msg {

  int frame_number;   /* unique frame number */

  void *frame;        /* pointer to the frame */

} display_msg;


