/*HEADER****************************************************************** ************************************************************************** 
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                            
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made 
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: vmdio.c
***
*** Comments:
***   This file contains the source functions for 
***
************************************************************************** 
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "enetprv.h"


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : vmac_mdio_write
*  Returned Value : boolean TRUE is successful otherwise FALSE
*  Comments       :
*        Writes to MDIO registers.
*
*END*-----------------------------------------------------------------*/

boolean _VMAC_mdio_write
   (
      /* [IN] the device physical address */
      VMAC_REG_STRUCT_PTR  dev_ptr,

      /* [IN] The device physical address */
      uint_32              phy_addr,

      /* [IN] the register index (0-31)*/
      uint_32              reg_index,
      
      /* [IN] The data to be written to register */
      uint_32              data
   )
{ /* Body */
   uint_32_ptr      vmac_mdio_data_ptr;
   uint_32          value;
   
   vmac_mdio_data_ptr = (uint_32_ptr)&dev_ptr->MDIO_DATA;

   /* Set SFD of "01"*/
   value = (VMAC_MDIO_SFD | VMAC_MDIO_OP_WRITE | (phy_addr << 23) | 
      (reg_index << 18) | VMAC_MDIO_TA | (uint_16)data);
   /* Set MDIO_DATA REG */
   _BSP_WRITE_VMAC(vmac_mdio_data_ptr,value);
   
   /* Wait for interrupt flag to be set */
   /* Start CR 873 */
   /* while (!(dev_ptr->INT_STATUS & VMAC_ISR_MDIO)) { */
   value = _BSP_READ_VMAC(&dev_ptr->INT_STATUS);
   while (!(value & VMAC_ISR_MDIO)) {      
      value = _BSP_READ_VMAC(&dev_ptr->INT_STATUS);
   } /* Endwhile */
   /* End CR 873 */
   
   /* Clear the status bit */
   /* Start CR 873 */
   /* dev_ptr->INT_STATUS = VMAC_ISR_MDIO; */
   _BSP_WRITE_VMAC(&dev_ptr->INT_STATUS, VMAC_ISR_MDIO);
   /* End CR 873 */

   return TRUE;
      
} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : vmac_mdio_read
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Writes to MDIO registers.
*
*END*-----------------------------------------------------------------*/

uint_32 _VMAC_mdio_read
   (
      /* [IN] the device physical address */
      VMAC_REG_STRUCT_PTR    dev_ptr,

      /* [IN] The device physical address */
      uint_32                phy_addr,

      /* [IN] the register index (0-31)*/
      uint_32                reg_index,

      /* [IN]/[OUT] The data pointer */
      uint_32_ptr            data_ptr
   )
{ /* Body */
   uint_32_ptr      vmac_mdio_data_ptr;
   uint_32          value;
       
   vmac_mdio_data_ptr = (uint_32_ptr)&dev_ptr->MDIO_DATA;

   /* Set SFD of "01"*/
   value = (VMAC_MDIO_SFD | VMAC_MDIO_OP_READ | (phy_addr << 23) | 
      (reg_index << 18) | VMAC_MDIO_TA);

   /* Set MDIO_DATA REG */
   _BSP_WRITE_VMAC(vmac_mdio_data_ptr, value);
   /* Wait for interrupt flag to be set */
   /* Start CR 873 */
   /* while (!(dev_ptr->INT_STATUS & VMAC_ISR_MDIO)) { */
   value = _BSP_READ_VMAC(&dev_ptr->INT_STATUS);
   while (!(value & VMAC_ISR_MDIO)) {      
      value = _BSP_READ_VMAC(&dev_ptr->INT_STATUS);
   } /* Endwhile */
   /* End CR 873 */
   
   /* Clear the status bit */
   /* Start CR 873 */
   /* dev_ptr->INT_STATUS = VMAC_ISR_MDIO; */
   _BSP_WRITE_VMAC(&dev_ptr->INT_STATUS, VMAC_ISR_MDIO);
   /* End CR 873 */
   
   *data_ptr = _BSP_READ_VMAC(vmac_mdio_data_ptr);
   
   return(ENET_OK);
  
} /* Endbody */

/* EOF */
