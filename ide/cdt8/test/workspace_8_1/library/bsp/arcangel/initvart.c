/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: initvart.c
***
*** Comments:      
***   This file contains the default initialization record for the
*** uart device.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"

#ifdef BSP_VUART1_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart1_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART1_BASE,
   /* QUEUE SIZE     */   BSP_VUART1_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART1_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART1_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART1_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART2_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart2_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART2_BASE,
   /* QUEUE SIZE     */   BSP_VUART2_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART2_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART2_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART2_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART3_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart3_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART3_BASE,
   /* QUEUE SIZE     */   BSP_VUART3_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART3_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART3_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART3_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART4_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart4_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART4_BASE,
   /* QUEUE SIZE     */   BSP_VUART4_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART4_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART4_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART4_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART5_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart5_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART5_BASE,
   /* QUEUE SIZE     */   BSP_VUART5_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART5_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART5_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART5_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART6_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart6_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART6_BASE,
   /* QUEUE SIZE     */   BSP_VUART6_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART6_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART6_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART6_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART7_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart7_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART7_BASE,
   /* QUEUE SIZE     */   BSP_VUART7_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART7_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART7_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART7_INTERRUPT_LEVEL,
};
#endif

#ifdef BSP_VUART8_BASE
VUART_SERIAL_INIT_STRUCT  _bsp_vuart8_init =
{
   /* DEVICE ADDRESS */   (pointer)BSP_VUART8_BASE,
   /* QUEUE SIZE     */   BSP_VUART8_QUEUE_SIZE,
   /* BAUD RATE      */   BSP_VUART8_BAUD,
   /* VECTOR NUMBER  */   BSP_VUART8_INTERRUPT_VECTOR,
   /* VECTOR LEVEL   */   BSP_VUART8_INTERRUPT_LEVEL,
};
#endif

/* EOF */
