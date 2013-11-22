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
*** File: mqx_init.c
***
*** Comments:      
***   This file contains the source for the default MQX INITIALIZATION
*** STRUCTURE
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"

extern TASK_TEMPLATE_STRUCT MQX_template_list[];

MQX_INITIALIZATION_STRUCT  MQX_init_struct =
{
   /* PROCESSOR_NUMBER                */  BSP_DEFAULT_PROCESSOR_NUMBER,
   /* START_OF_KERNEL_MEMORY          */  BSP_DEFAULT_START_OF_KERNEL_MEMORY,
   /* END_OF_KERNEL_MEMORY            */  BSP_DEFAULT_END_OF_KERNEL_MEMORY,
   /* INTERRUPT_STACK_SIZE            */  BSP_DEFAULT_INTERRUPT_STACK_SIZE,
   /* TASK_TEMPLATE_LIST              */  (pointer)MQX_template_list,
   /* MQX_HARDWARE_INTERRUPT_LEVEL_MAX*/  BSP_DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX,
   /* MAX_MSGPOOLS                    */  BSP_DEFAULT_MAX_MSGPOOLS,
   /* MAX_MSGQS                       */  BSP_DEFAULT_MAX_MSGQS,
   /* IO_CHANNEL                      */  BSP_DEFAULT_IO_CHANNEL,
   /* IO_OPEN_MODE                    */  BSP_DEFAULT_IO_OPEN_MODE
};

/* EOF */
