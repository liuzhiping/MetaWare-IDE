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


#ifdef VR_MQX
#define state_change(state,frame)  { \
                                   _sr((state), 0x4000); \
                                   _sr((frame), 0x4001); \
                                   }
#else
#define state_change(state,frame)  ;
#endif

#define state_change_d(state,d,frame) { \
                                      unsigned int t; \
                                      t = ((d) << 16) | (state); \
                                      state_change(t,frame); \
                                      }


#endif /* _TRACK_STATE_H */
