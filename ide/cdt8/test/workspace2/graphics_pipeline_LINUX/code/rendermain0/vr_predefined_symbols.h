/*
 * This file contains a list of symbols that are predefined by the
 * code generator when parsing the code for process rendermain0.
 * The file is not actually included by the user application and it is
 * generated only for documentation purposes.
 */
#define __VRC__
#define VR_PROCESS_RENDERMAIN0

/* symbols for software architecture late-binding objects */

#define thd_grmain                                              ((vr_thread_t)0)
#define thd_displaymain                                         ((vr_thread_t)1)
#define thd_rendermain0                                         ((vr_thread_t)2)
#define thd_rendermain1                                         ((vr_thread_t)3)
#define render_channel0                                         ((vr_channel_t)1)
#define display_channel0                                        ((vr_channel_t)2)
#define raster_pool                                             ((vr_memory_pool_t)0)
#define grcmd_pool                                              ((vr_memory_pool_t)1)

/* symbols attached to process rendermain0 in software architecture*/

#define NUMBER_OF_CPU_ISLANDS                                   4
#define IN_CHANNEL                                              render_channel0
#define OUT_CHANNEL                                             display_channel0