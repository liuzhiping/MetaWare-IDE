/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International.
***
*** File: io_ide.c
***
*** Comments:
***    This file contains the IDE initialization functions. This file is 
***    added to address CR 2283
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "bsp_prv.h"
#include "io_prv.h"
#include "io_disk.h"
#include "io_ideprv.h"

#if BSP_USE_IDE

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_ide_install
* Returned Value   : MQX_OK or error code
* Comments         :
*    Install IDE associated with hard disk.
*
*END*----------------------------------------------------------------------*/

int_32 _io_ide_install
(
      /* [IN] A string that identifies the device for fopen */
      char_ptr identifier,

      /* [IN] Device number */
      uint_32  device,

      /* [IN] Interrupt vector to use */
      uint_32  vector,

      /* [IN] Interrupt level to use */
      uint_32  level

   )
{ /* Body */

   IO_IDE_INIT_STRUCT_PTR  ide_init_ptr;
   uint_32                 reg, i;
  
   /* Only device 0 and 1 is accepted */   
   if(device >= IDE_MAX_DRIVE) {
      return(IDE_ERR_DEVICE_NOT_VALID);
   } /* Endif */
   
   /* Allocate memory for the IDE initilization struct */
   ide_init_ptr = (IO_IDE_INIT_STRUCT_PTR)_mem_alloc_system_zero((uint_32)sizeof(IO_IDE_INIT_STRUCT));

#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (ide_init_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   /* Initialize the init structure */
   /* Device number */
   ide_init_ptr->DRIVE = device;
   /* Pointer to controller base address */
   ide_init_ptr->IDE_CTRL_STRUCT_PTR = (volatile pointer)BSP_IDE_CONTROLLER_BASE;
   /* Interrupt vector number */
   ide_init_ptr->INTERRUPT_VECTOR = vector;
   /* Default PIO mode */
   ide_init_ptr->IDE_PIO_MODE = BSP_IDE_DEFAULT_PIO_MODE;

   return (_io_dev_install(identifier,
      _io_ide_open,
      _io_ide_close,
      NULL,
      NULL,
      _io_ide_ioctl,
      (pointer)ide_init_ptr)); 

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_ide_open
* Returned Value   : MQX_OK or error code
* Comments         :
*    
*
*END*----------------------------------------------------------------------*/

int_32 _io_ide_open
   (
      /* [IN] the file handle for the device being opened */
      FILE_DEVICE_STRUCT_PTR   fd_ptr,
      
      /* [IN] the remaining portion of the name of the device (not used) */
      char_ptr                 open_name_ptr,

      /* [IN] the flags to be used during operation (not used) */
      char_ptr                 flags
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_IDE_INIT_STRUCT_PTR  ide_init_ptr;
                           
   ide_init_ptr = (IO_IDE_INIT_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;

   /* Disable the controller interrupt to the CPU */
   _bsp_ide_disable_int(ide_init_ptr->INTERRUPT_VECTOR);
      
   /* Reset the controller */
   _bsp_ide_reset();

   /* Set up PIO mode */
   _bsp_ide_pio_mode(ide_init_ptr->IDE_PIO_MODE);

   /* Poll mode is supported for now */
#if BSP_IDE_INTERRUPT_SUPPORTED   
   /* Install the IDE interrupt handler */
   ide_init_ptr->OLD_ISR_DATA = _int_get_isr_data(ide_init_ptr->INTERRUPT_VECTOR);
   ide_init_ptr->OLD_ISR_EXCEPTION_HANDLER = (void (_CODE_PTR_)(void))
      _int_get_exception_handler(ide_init_ptr->INTERRUPT_VECTOR);   
   ide_init_ptr->OLD_ISR = (void (_CODE_PTR_)(void))		  
      _int_install_isr(ide_init_ptr->INTERRUPT_VECTOR, _bsp_ide_isr, NULL);

   /* Enable the controller interrupt */
   _bsp_ide_enable_int(ide_init_ptr->INTERRUPT_VECTOR);
#endif

   return(MQX_OK);

} /* Endbody */

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _io_ide_close
* Returned Value   : 
* Comments         :
*
*END*-------------------------------------------------------------------------*/

int_32 _io_ide_close
   (
      /* [IN] the file handle for the device being closed */
      FILE_DEVICE_STRUCT_PTR fd_ptr
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_IDE_INIT_STRUCT_PTR  ide_init_ptr;
   _mqx_uint               result = MQX_OK;

   ide_init_ptr = (IO_IDE_INIT_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;

   result = _mem_free(ide_init_ptr);
   if(result == MQX_OK ) {
      fd_ptr->DEV_PTR = NULL;
   } /* Endif */
                           
#if BSP_IDE_INTERRUPT_SUPPORTED
   /* Uninstall the interrupt */
   _int_install_isr(ide_init_ptr->INTERRUPT_VECTOR, (pointer)ide_init_ptr->OLD_ISR, 
                    ide_init_ptr->OLD_ISR_DATA);

   _bsp_ide_disable_int(ide_init_ptr->INTERRUPT_VECTOR);
#endif   

   return MQX_OK;

} /* Endbody */

/*FUNCTION*---------------------------------------------------------------------
* 
* Function Name    : _io_ide_ioctl
* Returned Value   : int_32
* Comments         :
*    Returns result of ioctl operation.
*
*END*-------------------------------------------------------------------------*/

int_32 _io_ide_ioctl
   (
      /* [IN] the file handle for the device */
      FILE_DEVICE_STRUCT_PTR fd_ptr,

      /* [IN] the ioctl command */
      uint_32                cmd,

      /* [IN/OUT] the ioctl parameters */
      uint_32_ptr            param_ptr
   )
{ /* Body */

   IO_DEVICE_STRUCT_PTR    io_dev_ptr = fd_ptr->DEV_PTR;
   IO_IDE_INIT_STRUCT_PTR  ide_init_ptr;
   IDE_CONTROL_STRUCT_PTR  ide_ptr;
   int_32                  result = MQX_OK;
                           
   ide_init_ptr = (IO_IDE_INIT_STRUCT_PTR)io_dev_ptr->DRIVER_INIT_PTR;
   ide_ptr      = (IDE_CONTROL_STRUCT_PTR)ide_init_ptr->IDE_CTRL_STRUCT_PTR;   
   
   switch (cmd) {
      case IO_IDE_IOCTL_GET_BASE_ADDRESS:
         *param_ptr = (uint_32)ide_init_ptr->IDE_CTRL_STRUCT_PTR;
         break;
      case IO_IDE_IOCTL_GET_ID:
         *param_ptr = (uint_32)ide_ptr->ID;
         break;
      case IO_IDE_IOCTL_GET_PIO_TIMING:
         *param_ptr = (uint_32)ide_ptr->PIO_SETUP;
         break;                        
      case IO_IDE_IOCTL_GET_PIO_MODE:
         *param_ptr = (uint_32)ide_init_ptr->IDE_PIO_MODE;
         break;               
      case IO_IDE_IOCTL_SET_PIO_MODE:
         _bsp_ide_pio_mode((uint_32)*param_ptr);
         ide_init_ptr->IDE_PIO_MODE = (uint_32)*param_ptr;                  
         break;               
      default:
         result = IO_ERROR_INVALID_IOCTL_CMD;
         break;
   } /* Endswitch */

   return result;

} /* Endbody */


#endif /* BSP_USE_IDE */

/* EOF */