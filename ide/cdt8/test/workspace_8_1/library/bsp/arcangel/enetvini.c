/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: enetvini.c
***
*** Comments:      
***   This file contains the functions used during enet driver
*** initialization
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_init
*  Returned Value : void
*  Comments       :
*
*END*-----------------------------------------------------------------*/

void _bsp_enet_init
   (
      /* [IN] the device number */
      pointer     handle,

      /* [IN] 0=before, 1=after ENET_initialize_xxx(), 2=shutdown */
      uint_32     when,

      /* [IN] optional initialization flags */
      uint_32     flags
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR   enet_ptr;
   VMAC_REG_STRUCT_PTR   dev_ptr;
   uint_32               temp;

   enet_ptr = (ENET_CFG_STRUCT_PTR)handle;

   dev_ptr = enet_ptr->DEV_PTR;

   if (when == 0) {

      /* Reset the tranceiver */
      _VMAC_mdio_write(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), LXT970A_CTRL_REG, 
         LXT970A_CTRL_RESET);
      
      do {
         _VMAC_mdio_read(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), LXT970A_CTRL_REG, 
            &temp);
      } while(temp & LXT970A_CTRL_RESET);

      /* Advertise capabilities */
      temp = LXT970A_AUTONEG_ADV_10BTX_FULL | LXT970A_AUTONEG_ADV_10BT | 
         AUTONEG_ADV_IEEE_8023; 

      /* VMAC is not capable of 100mbit if system clock is less than 25 MHz */
      if (BSP_SYSTEM_CLOCK > 25000000) {
         temp |= LXT970A_AUTONEG_ADV_100BTX_FULL | LXT970A_AUTONEG_ADV_100BTX;
      } /* Endif */
      
      _VMAC_mdio_write(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
         LXT970A_AUTONEG_ADV_REG, temp);
      
      /* Begin Auto-negotiation */
      _VMAC_mdio_write(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), LXT970A_CTRL_REG, 
         (LXT970A_CTRL_AUTONEG | LXT970A_CTRL_RESTART_AUTO));

      /* Wait for Auto-negotiation to complete */      
      do {
         _VMAC_mdio_read(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
            LXT970A_STATUS2_REG, &temp);
      } while(!(temp & LXT970A_STATUS2_COMPLETE));
            
   } else if (when == 1)  {
      /* Check peer to match duplex */
      _VMAC_mdio_read(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
         LXT970A_STATUS2_REG, &temp);

      if (temp & LXT970A_STATUS2_FULL) {
         if (!enet_ptr->FULL_DUPLEX) {
            temp = _BSP_READ_VMAC(&dev_ptr->CONTROL);
            temp |= VMAC_CTRL_ENBFULL;
            _BSP_WRITE_VMAC(&dev_ptr->CONTROL, temp);
         
            /* Erata #4: Read Control register on LXT971A */
            _VMAC_mdio_read(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
               LXT970A_CTRL_REG, &temp);        
            temp |= LXT970A_CTRL_DUPLEX_MODE;
            /* Update the control register bit 8 */
            _VMAC_mdio_write(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
               LXT970A_CTRL_REG, temp);
            enet_ptr->FULL_DUPLEX = TRUE;
         } /* Endif */
      } else { 
         if (enet_ptr->FULL_DUPLEX) {
            temp = _BSP_READ_VMAC(&dev_ptr->CONTROL);
            temp &= ~VMAC_CTRL_ENBFULL;
            _BSP_WRITE_VMAC(&dev_ptr->CONTROL, temp);
            /* Erata #4: Read Control register on LXT971A */
            _VMAC_mdio_read(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
               LXT970A_CTRL_REG, &temp);        
            temp &= (~LXT970A_CTRL_DUPLEX_MODE);
            /* Update the control register bit 8 */
            _VMAC_mdio_write(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
               LXT970A_CTRL_REG, temp);
            enet_ptr->FULL_DUPLEX = FALSE;
         } /* Endif */
      } /* Endif */
      
   } else if (when == 2)  {
   } /* Endif */                 

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_getbase
*  Returned Value : Ethernet base
*  Comments       :
*
*END*-----------------------------------------------------------------*/

VMAC_REG_STRUCT_PTR _bsp_enet_getbase
   (
      /* [IN] the device number */
      uint_32     devnum
   )
{ /* Body */

  switch (devnum) {
     case 0:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC1_ENET_BASE);
     case 1:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC2_ENET_BASE);
     case 2:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC3_ENET_BASE);
     case 3:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC4_ENET_BASE);
     case 4:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC5_ENET_BASE);
     case 5:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC6_ENET_BASE);
     case 6:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC7_ENET_BASE);
     case 7:
        return((VMAC_REG_STRUCT_PTR)BSP_VMAC8_ENET_BASE);
     default:
        return((VMAC_REG_STRUCT_PTR)0);
  } /* Endswitch */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_getvec
*  Returned Value : interrupt vector
*  Comments       :
*
*END*-----------------------------------------------------------------*/

uint_32 _bsp_enet_getvec
   (
      /* [IN] the device number */
      uint_32     devnum
   )
{ /* Body */

  switch (devnum) {
     case 0:
         return BSP_VMAC1_INTERRUPT_VECTOR;
     case 1:
         return BSP_VMAC2_INTERRUPT_VECTOR;
     case 2:
         return BSP_VMAC3_INTERRUPT_VECTOR;
     case 3:
         return BSP_VMAC4_INTERRUPT_VECTOR;
     case 4:
         return BSP_VMAC5_INTERRUPT_VECTOR;
     case 5:
         return BSP_VMAC6_INTERRUPT_VECTOR;
     case 6:
         return BSP_VMAC7_INTERRUPT_VECTOR;
     case 7:
         return BSP_VMAC8_INTERRUPT_VECTOR;
     /* Start CR 2249 */
     default:
         return BSP_VMAC_ERR_INVALID_DEVICE;
     /* End CR 2249 */
  } /* Endswitch */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_get_phyid
*  Returned Value : physid number
*  Comments       :
*
*END*-----------------------------------------------------------------*/

uint_32 _bsp_enet_get_phyid
   (
      /* [IN] the device number */
      uint_32     devnum
   )
{ /* Body */

  switch (devnum) {
     case 0:
         return BSP_VMAC1_PHY_ID;
     case 1:
         return BSP_VMAC2_PHY_ID;
     case 2:
         return BSP_VMAC3_PHY_ID;
     case 3:
         return BSP_VMAC4_PHY_ID;
     case 4:
         return BSP_VMAC5_PHY_ID;
     case 5:
         return BSP_VMAC6_PHY_ID;
     case 6:
         return BSP_VMAC7_PHY_ID;
     case 7:
         return BSP_VMAC8_PHY_ID;
     /* Start CR 2249 */
     default:
         return BSP_VMAC_ERR_INVALID_DEVICE;
     /* End CR 2249 */
  } /* Endswitch */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_get_speed
*  Returned Value : uint_32 - connection speed
*  Comments       :
*
*END*-----------------------------------------------------------------*/

uint_32 _bsp_enet_get_speed
   (
      /* [IN] the device number */
      pointer     handle
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR   enet_ptr;
   VMAC_REG_STRUCT_PTR   dev_ptr;
   uint_32               result;

   enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   dev_ptr  = enet_ptr->DEV_PTR;

   _VMAC_mdio_read(dev_ptr, _bsp_enet_get_phyid(enet_ptr->DEV_NUM), 
      LXT970A_STATUS2_REG, &result);

   if (result & LXT970A_STATUS2_100) {
      result = 100;
   } else {
      result = 10;
   } /* Endif */

   return result;

} /* Endbody */

#ifdef BSP_ENABLE_VMAC_POLLING
/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_get_poll_period
*  Returned Value : polling period for connection status
*  Comments       :
*
*END*-----------------------------------------------------------------*/

uint_32 _bsp_enet_get_poll_period
   (
      /* [IN] the device number */
      uint_32     devnum
   )
{ /* Body */

  switch (devnum) {
     case 0:
         return BSP_VMAC1_POLL_PERIOD;
     case 1:
         return BSP_VMAC2_POLL_PERIOD;
     case 2:
         return BSP_VMAC3_POLL_PERIOD;
     case 3:
         return BSP_VMAC4_POLL_PERIOD;
     case 4:
         return BSP_VMAC5_POLL_PERIOD;
     case 5:
         return BSP_VMAC6_POLL_PERIOD;
     case 6:
         return BSP_VMAC7_POLL_PERIOD;
     case 7:
         return BSP_VMAC8_POLL_PERIOD;
     /* Start CR 2249 */
     default:
         return BSP_VMAC_ERR_INVALID_DEVICE;
     /* End CR 2249 */
  } /* Endswitch */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_get_poll_priority
*  Returned Value : Polling task priority
*  Comments       :
*
*END*-----------------------------------------------------------------*/

uint_32 _bsp_enet_get_poll_priority
   (
      /* [IN] the device number */
      uint_32     devnum
   )
{ /* Body */

  switch (devnum) {
     case 0:
         return BSP_VMAC1_POLL_PRIORITY;
     case 1:
         return BSP_VMAC2_POLL_PRIORITY;
     case 2:
         return BSP_VMAC3_POLL_PRIORITY;
     case 3:
         return BSP_VMAC4_POLL_PRIORITY;
     case 4:
         return BSP_VMAC5_POLL_PRIORITY;
     case 5:
         return BSP_VMAC6_POLL_PRIORITY;
     case 6:
         return BSP_VMAC7_POLL_PRIORITY;
     case 7:
         return BSP_VMAC8_POLL_PRIORITY;
     /* Start CR 2249 */
     default:
         return BSP_VMAC_ERR_INVALID_DEVICE;
     /* End CR 2249 */
  } /* Endswitch */

} /* Endbody */

#endif /* BSP_ENABLE_VMAC_POLLING */
   
/* EOF */
