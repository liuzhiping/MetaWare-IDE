/************************************************************************/
/*                                                                      */
/* Copyright (C) ARC International, Inc. 2007.                          */
/*                                                                      */
/* All Rights Reserved.                                                 */
/*                                                                      */
/* This software is the property of Arc International.  It is           */
/* furnished under a specific licensing agreement.  It may be used or   */
/* copied only under terms of the licensing agreement.                  */
/*                                                                      */
/* For more information, contact info@arc.com                           */
/*                                                                      */
/************************************************************************/

#ifndef _TRACK_STATE_H
#define _TRACK_STATE_H 1


/*
   For demonstration purposes, the state that any given cpu
   is in can be reflected in an auxiliary register (0x4000).
   This makes it possible for debugging tools to capture this
   information to analyze this program.
*/


/* State values */
#define STATE_INITIALIZING        0
#define STATE_IDLE                1
#define STATE_WAITING_FOR_POOL    2
#define STATE_WAITING_FOR_CHANNEL 3
#define STATE_PROCESSING          4


#define state_change(state,frame)  { \
                                   _sr((state), 0x4000); \
                                   _sr((frame), 0x4001); \
                                   }

#define state_change_d(state,d,frame) { \
                                      unsigned int t; \
                                      t = ((d) << 16) | (state); \
                                      state_change(t,frame); \
                                      }


#endif /* _TRACK_STATE_H */
