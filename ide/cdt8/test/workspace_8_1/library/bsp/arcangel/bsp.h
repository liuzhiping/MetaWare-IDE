#ifndef __bsp_h__
#define __bsp_h__ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: bsp.h
***
*** Comments: 
***   This file includes all include files specific to this Board Support
*** Package
***
***************************************************************************
*END**********************************************************************/

#include <arc_cnfg.h>
#include <psp.h>
#include <pcb.h>
#include <fio.h>
#include <io.h>
#include <io_mem.h>
#include <io_null.h>
#include <serial.h>
#include <arcangel.h>
#include <mw_uart.h>
#include <vuart.h>
#include <svuart.h>
#ifdef BSP_SIM_MODE
#    include <emwsim.h>
#else
#    include <vmac.h>
#    include <lxt970a.h>
#endif
#include <enet.h>
#include <flashx.h>
#include <istrata.h>
#include <io_pipe.h>
/* Start CR 2283 */
#if BSP_USE_IDE
#include <io_ide.h>
#include <io_disk.h>
#endif
/* End CR 2283 */

#endif /* __bsp_h__ */
/* EOF */
