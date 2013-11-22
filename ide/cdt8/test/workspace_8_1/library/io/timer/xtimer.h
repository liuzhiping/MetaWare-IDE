#ifndef __xtimer_h__
#define __xtimer_h__ 1
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
*** File: xtimer.h
***
*** Comments:      
***   This include file is used to provide information needed by
***   an application program using the generic xtimer DLL
***   simulated timer for the SDS simulator
***                                                               
**************************************************************************
*END*********************************************************************/

/* Control register values */
#define XTIMER_CONTROL_RESET        0x80 /* Reset the timer    */
#define XTIMER_CONTROL_ENABLE       0x40 /* Allow timer to run */
#define XTIMER_CONTROL_RESTART      0x20 /* Restart timer when count==0  */
#define XTIMER_CONTROL_SOFTWARE_ACK 0x10 /* Ack interrupt when stat read */
/* Bits 0-2 are the timer's interrupt level for 68xxx */

/* Status register values */
#define XTIMER_STATUS_TRIGGER       0x80 /* Timer has hit 0   */
#define XTIMER_STATUS_STOP          0x01 /* Timer has stopped */

/*--------------------------------------------------------------------------*/
/*
**                        DATATYPE DEFINTIONS
*/

/* How many bytes has the hardware implementation placed between registers */
#ifndef XTIMER_STRIDE_SIZE
#define XTIMER_STRIDE_SIZE 0
#endif


/*
** XTIMER STRUCT
**
** This structure is used to access the xtimer device registers
*/
typedef struct xtimer_struct
{
   uchar   CONTROL;
#if XTIMER_STRIDE_SIZE > 0
   uchar   FILLER1[XTIMER_STRIDE_SIZE];
#endif
   uchar   IRQVECT;
#if XTIMER_STRIDE_SIZE > 0
   uchar   FILLER2[XTIMER_STRIDE_SIZE];
#endif
   uchar   STATUS;
#if XTIMER_STRIDE_SIZE > 0
   uchar   FILLER3[XTIMER_STRIDE_SIZE];
#endif
   uchar   PRESCALE; /* prescales input 1 us timer count */
#if XTIMER_STRIDE_SIZE > 0
   uchar   FILLER4[XTIMER_STRIDE_SIZE];
#endif
   uint_16 PRELOAD;
#if XTIMER_STRIDE_SIZE > 0
   uchar   FILLER5[XTIMER_STRIDE_SIZE];
#endif
   uint_16 COUNT;
} XTIMER_STRUCT, _PTR_ XTIMER_STRUCT_PTR;

#ifdef __cplusplus
extern "C" {
#endif

extern uint_32 _xtimer_init(volatile XTIMER_STRUCT _PTR_, uint_32, uint_32);
extern boolean _xtimer_int_available(volatile XTIMER_STRUCT _PTR_);
extern uint_32 _xtimer_get_microseconds(volatile XTIMER_STRUCT _PTR_,
   uint_32, uint_32);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
