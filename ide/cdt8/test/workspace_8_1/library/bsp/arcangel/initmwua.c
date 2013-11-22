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
*** File: initmwua.c
***
*** Comments:      
***   This file contains the default initialization record for the
*** uart device.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"

#ifndef BSP_SIMUART_QUEUE_SIZE
#   define BSP_SIMUART_QUEUE_SIZE 128
#endif

MW_SIMUART_INIT_STRUCT  _bsp_uart_init =
{
   /* QUEUE SIZE           */   BSP_SIMUART_QUEUE_SIZE
};

/* EOF */
