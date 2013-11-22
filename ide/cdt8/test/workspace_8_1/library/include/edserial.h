#ifndef __edserial_h__
#define __edserial_h__
/*HEADER************************************************************************
********************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: edserial.h
***
*** Comments:  
***   Serial Embedded Debug Server header file
***
********************************************************************************
*END***************************************************************************/

/* Error codes */
#define EDS_SERIAL_CANT_CREATE     (EDS_SERIAL_ERROR_BASE | 0x01)
#define EDS_SERIAL_ALREADY_CREATED (EDS_SERIAL_ERROR_BASE | 0x02)
#define EDS_SERIAL_CANT_DESTROY    (EDS_SERIAL_ERROR_BASE | 0x03)
#define EDS_SERIAL_IO_FAILED       (EDS_SERIAL_ERROR_BASE | 0x04)

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint ED_Serial_add(char_ptr, uint_32, _mqx_uint);
extern _mqx_uint _eds_serial_create_component(char_ptr, uint_32, _mqx_uint);
#endif

#ifdef __cplusplus
}
#endif


#endif

/* EOF */
