#ifndef __mqx_ioc_h__
#define __mqx_ioc_h__ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: mqx_ioc.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
***  support functions for MQX I/O Components.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */


/* 
** The IO component indexes, used to index into the component
** arrays to access component specific data
*/
#define IO_SUBSYSTEM_COMPONENT        (0)
#define IO_RTCS_COMPONENT             (1)
#define IO_LAPB_COMPONENT             (2)
#define IO_LAPD_COMPONENT             (3)
#define IO_SDLC_COMPONENT             (4)
#define IO_HDLC_COMPONENT             (5)
#define IO_MFS_COMPONENT              (6)
#define IO_CAN_COMPONENT              (7)
#define IO_PPP_COMPONENT              (8)
#define IO_SNMP_COMPONENT             (9)
#define IO_EDS_COMPONENT              (10)
/* Start CR 175 */
#define IO_USB_COMPONENT              (11)
/* End CR 175 */

/* The maximum number of IO components */
#define MAX_IO_COMPONENTS                  (16)

/*--------------------------------------------------------------------------*/
/*                        DATATYPE DECLARATIONS                             */


/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void (_CODE_PTR_ _mqx_get_io_component_cleanup(_mqx_uint))(pointer);
extern void (_CODE_PTR_ _mqx_set_io_component_cleanup(_mqx_uint,
   void (_CODE_PTR_)(pointer) ))(pointer);

extern pointer _mqx_get_io_component_handle(_mqx_uint);
extern pointer _mqx_set_io_component_handle(_mqx_uint, pointer);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
