/*HEADER****************************************************************
************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: enpoll.c
***
*** Comments:  This file contains the Ethernet duplex mode polling 
***            
***
************************************************************************
*END*******************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "enetprv.h"


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _VMAC_init_poll_mode
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Initializes the hot swap mechanism.
*
*END*-----------------------------------------------------------------*/

uint_32 _VMAC_init_poll_mode
   (
      /* [IN] the Ethernet state structure */
      pointer     handle
   )
{ /* Body */
   TASK_TEMPLATE_STRUCT          task_template;
   _task_id                      tid; 
   ENET_CFG_STRUCT_PTR           enet_ptr;
   
   enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   
   if (!BSP_ENABLE_VMAC1_POLLING && (enet_ptr->DEV_NUM == 0)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC2_POLLING && (enet_ptr->DEV_NUM == 1)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC3_POLLING && (enet_ptr->DEV_NUM == 2)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC4_POLLING && (enet_ptr->DEV_NUM == 3)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC5_POLLING && (enet_ptr->DEV_NUM == 4)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC6_POLLING && (enet_ptr->DEV_NUM == 5)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC7_POLLING && (enet_ptr->DEV_NUM == 6)) {
      return ENET_OK;
   } /* Endif */

   if (!BSP_ENABLE_VMAC8_POLLING && (enet_ptr->DEV_NUM == 7)) {
      return ENET_OK;
   } /* Endif */

   /* Initialize the task template */
   _mem_zero((pointer)&task_template, (_mem_size)sizeof(task_template));
   task_template.TASK_ADDRESS    = VMAC_poll_task;
   task_template.TASK_STACKSIZE  = VMAC_POLL_TASK_STACK_SIZE;
   task_template.TASK_PRIORITY   = _bsp_enet_get_poll_priority(enet_ptr->DEV_NUM);
   task_template.TASK_NAME       = (char_ptr)VMAC_POLL_TASK_NAME;
   task_template.CREATION_PARAMETER = (uint_32)enet_ptr;
   
   tid = _task_create(0, 0, (uint_32)&task_template);
   if (tid == MQX_NULL_TASK_ID) {
      return MQX_NULL_TASK_ID;
   } /* Endif */    
   
   return ENET_OK;

} /* Endbody */

/*TASK*-------------------------------------------------------------------------
*
* Function Name  : VMAC_poll_task
* Returned Value : void
* Comments       : Monitors the status of the connection  
*
*END--------------------------------------------------------------------------*/

void VMAC_poll_task
   (
      uint_32    param
   )
{ /* Body */      
   ENET_CFG_STRUCT_PTR         enet_ptr;
   VMAC_REG_STRUCT_PTR         dev_ptr;
   uint_32                     delay; 
    
   enet_ptr = (ENET_CFG_STRUCT_PTR)param;
   dev_ptr = enet_ptr->DEV_PTR;

   delay = _bsp_enet_get_poll_period(enet_ptr->DEV_NUM);
   
   ENET_lock();

   while (TRUE) {
      /* Check peer to match duplex */
      _bsp_enet_init((pointer)enet_ptr, 1, 0);
      _time_delay(delay);
   } /* Endwhile */
   
} /* Endbody */



/* EOF */
