/************************************************************************/
/*                                                                      */
/* Copyright (C) ARC International, Inc. 2007.                          */
/*                                                                      */
/* All Rights Reserved.                                                 */
/*                                                                      */
/* This software is the property of ARC International, Inc.  It is      */
/* furnished under a specific licensing agreement.  It may be used or   */
/* copied only under terms of the licensing agreement.                  */
/*                                                                      */
/* For more information, contact support@arc.com                        */
/*                                                                      */
/************************************************************************/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include "vr_software_architecture.h"
#include "vr_property.h"

#include "common.h"
#include "config.h"
#include "plot.h"
#include "grapi_types.h"


#define SHARED_DRAM_SIZE (2*1024*1024)  /* 2Mb */

#define OS_SIZE ((TOTAL_DRAM_SIZE-SHARED_DRAM_SIZE)/NUMBER_OF_CPU_ISLANDS)


static void
declare_channel (const char *channel_name,
                 vr_thread_t producer_thd,
                 vr_thread_t consumer_thd,
                 int depth)
{
#ifdef USE_ARC_CHANNEL
  #define CHTYPE VR_ARC_CHANNEL
#else
  #define CHTYPE VR_ARC_MEMORY_CHANNEL
#endif
  vr_channel_t channel;
  vr_property_t p;

  /*
    Create Output Channel 
   */
  channel = vr_channel_new (channel_name, CHTYPE);
  assert(channel != (vr_channel_t)NULL);

#ifdef USE_ARC_CHANNEL
  p = vr_channel_get_property (channel, VR_ARC_CHANNEL_HW_NAME);
  assert (p != NULL);
  vr_property_set_value (p, channel_name); /* same name for now */

   /* NOTE: depth argument ignored for now with ARCChannels */

#else
  {
  char valstr[40];
  /* set channel properties */
  p = vr_channel_get_property (channel, "memory_bank");
  assert (p != NULL);
  vr_property_set_value (p, "dram");
  p = vr_channel_get_property (channel, VR_PROPERTY_CHANNEL_BUFFER_SIZE);
  assert (p != NULL);
  sprintf(valstr, "%d", depth);
  vr_property_set_value (p, valstr);
  }
#endif

  vr_channel_add_producer (channel, producer_thd);
  vr_channel_add_consumer (channel, consumer_thd);
}


#ifdef USE_ARC_CHANNEL
static void
declare_multiproducer_channel (const char *channel_name,
                               int num_producers,
                               vr_thread_t producer_thd[],
                               vr_thread_t consumer_thd)
{
  vr_channel_t channel;
  vr_property_t p;
  int i;

  /*
    Create Output Channel
   */
  channel = vr_channel_new (channel_name, VR_ARC_CHANNEL);
  assert(channel != (vr_channel_t)NULL);

  p = vr_channel_get_property (channel, VR_ARC_CHANNEL_HW_NAME);
  assert (p != NULL);
  vr_property_set_value (p, channel_name); /* same name for now */

  for (i=0;i<num_producers;++i)
    vr_channel_add_producer (channel, producer_thd[i]);

  vr_channel_add_consumer (channel, consumer_thd);
}
#endif


int
swarch (void)
{
  vr_os_t os;
  vr_process_t main_process[NUMBER_OF_CPU_ISLANDS];
  vr_thread_t thd[NUMBER_OF_CPU_ISLANDS];
  char process_name[80];
  char name[40];
  char procname[40];
  char value[40];
  unsigned int base_addr = 0;
  int i;

  for (i = 0; i < NUMBER_OF_CPU_ISLANDS; ++i)
    {
      /* 
         -------------------- SWARCH for One CPU ----------------
       */

      /*
         Create the source file set
       */
      vr_source_set_t ss = vr_source_set_new ("sset");

      if (i == GR_THD) {

        vr_source_set_add_file (ss, "src/grmain.c");
        vr_source_set_add_file (ss, "src/grapi.c");
        vr_source_set_add_file (ss, "src/screen.c");

        strcpy(process_name, "grmain");

      } else if (i == DISPLAY_THD) {

        vr_source_set_add_file (ss, "src/displaymain.c");
        vr_source_set_add_file (ss, "src/plot.c");
        vr_source_set_add_file (ss, "src/screen.c");

        strcpy(process_name, "displaymain");

      } else {

        vr_source_set_add_file (ss, "src/rendermain.c");
        vr_source_set_add_file (ss, "src/plot3d.c");
        vr_source_set_add_file (ss, "src/plot.c");
        vr_source_set_add_file (ss, "src/screen.c");
            /* TODO: render shouldn't need screen... remove dependency */

        sprintf(process_name, "%s%d", "rendermain", i-RENDER_THD);

      }

      /*
         Instantiate the OS
       */
#ifdef VR_MQX
      sprintf (name, "MQX%d", i);
      os = vr_os_new (name, VR_OS_TYPE_MQX);
#elif defined VR_RAW
      sprintf (name, "bare_hardware%d", i);
      os = vr_os_new (name, VR_OS_TYPE_RAW);
#endif

      /*
         Add processors to the OS
       */
      sprintf (procname, "board.SoC.island%d.ARC700", i);
      vr_os_add_processor (os, vr_component_get_processor (NULL, procname));


      /*
         Instantiate the main process
       */
      main_process[i] = vr_process_new (process_name);

#if 0
      /*
        Pass along symbol(s) to the application
       */
      sprintf(value, "%d", NUMBER_OF_CPU_ISLANDS);
      vr_process_add_preprocessor_symbol(main_process[i],
                                         "NUMBER_OF_CPU_ISLANDS",
                                          value);
#endif

      /*
         Elect only one CPU to be the master
       */
      vr_property_t p_prop = vr_process_get_property (main_process[i], "master");
      assert (p_prop != NULL);
      if (i == 0)
        vr_property_set_value (p_prop, "true");
      else
        vr_property_set_value (p_prop, "false");

      /*
         Attach the process to the OS
       */
      vr_process_set_os (main_process[i], os);

      /*
         Set OS properties
       */
      vr_property_t os_prop;
      os_prop = vr_os_get_property (os, "physical_base_address");
      assert (os_prop != NULL);
      sprintf (value, "0x%08x", base_addr);
      vr_property_set_value (os_prop, value);

      os_prop = vr_os_get_property (os, "size");
      assert (os_prop != NULL);
      sprintf (value, "0x%08x", OS_SIZE);
      vr_property_set_value (os_prop, value);

      base_addr += OS_SIZE;

      if (1 /*strcmp (...os..., VR_OS_TYPE_MQX) == 0 */ )
        {

          os_prop = vr_os_get_property (os, "system_stack_size");
          assert (os_prop != NULL);
          vr_property_set_value (os_prop, "16384");     /* 16Kb */

#ifdef VR_MQX
          os_prop = vr_os_get_property (os, "kernel_data_size");
          assert (os_prop != NULL);
          vr_property_set_value (os_prop, "524288");    /* .5Mb */
#endif

          os_prop = vr_os_get_property (os, "bsp_dir");
          assert (os_prop != NULL);
          vr_property_set_value (os_prop, "C:/ARC/mqx_rtos2.51_arc700");
        }

#if 0
      {
        vr_iterator_t it = vr_os_get_property_iterator (os);
        while (vr_iterator_has_next (it))
          {
            char name[30];
            char val[30];
            vr_property_t pr = (vr_property_t) vr_iterator_get_next (it);
            vr_property_get_name (pr, name, sizeof (name));
            vr_property_get_value (pr, val, sizeof (val));
            printf ("%s = %s\n", name, val);
            vr_property_t os_prop = vr_os_get_property (os, name);
            if (os_prop == NULL)
              {
                printf ("property %s not found in os\n", name);
              }
          }
      }
#endif

#if 0
      {
        vr_iterator_t it = vr_process_get_property_iterator (main_process[i]);
        while (vr_iterator_has_next (it))
          {
            char name[30];
            char val[30];
            vr_property_t pr = (vr_property_t) vr_iterator_get_next (it);
            vr_property_get_name (pr, name, sizeof (name));
            vr_property_get_value (pr, val, sizeof (val));
            printf ("%s = %s\n", name, val);
            vr_property_t p = vr_process_get_property (main_process[i], name);
            if (p == NULL)
              {
                printf ("property %s not found in process\n", name);
              }
          }
      }
#endif

      /*
         Add the source set to the main process
       */
      vr_process_add_source_set (main_process[i], ss);

      /*
         Thread creation for process main
       */
      sprintf (name, "thd_%s", process_name);
      thd[i] = vr_thread_new (name);
      vr_thread_set_process (thd[i], main_process[i]);

    }

/* --- Shared Late Binding Objects ----- */

  /*
     Instantiate Shared Memory Pools
   */
  vr_memory_pool_t raster_pool =
    vr_memory_pool_new ("raster_pool",
                        VR_ARC_MEMORY_POOL,
                        8, /* num_nodes */
                        MAXC*MAXR); /* node_size */
  for (i = 0; i < NUMBER_OF_RENDER_CPUS; ++i)
    vr_memory_pool_add_getter (raster_pool, thd[RENDER_THD+i]);
  vr_memory_pool_add_putter (raster_pool, thd[DISPLAY_THD]);

  vr_memory_pool_t grcmd_pool =
    vr_memory_pool_new ("grcmd_pool",
                        VR_ARC_MEMORY_POOL,
                        128*NUMBER_OF_RENDER_CPUS, /* num_nodes */
                        128 /*sizeof(gr_message)*/); /* node_size */
                        /* TODO: ^^^user power-of-two node_size */
  vr_memory_pool_add_getter (grcmd_pool, thd[GR_THD]);
  for (i = 0; i < NUMBER_OF_RENDER_CPUS; ++i)
    vr_memory_pool_add_putter (grcmd_pool, thd[RENDER_THD+i]);


   /*
    Instantiate Channels
  */

    for (i = 0; i < NUMBER_OF_RENDER_CPUS; ++i)
    {
      sprintf(name, "render_channel%d", i);
      declare_channel (name,
                       thd[GR_THD],
                       thd[RENDER_THD+i],
                       128*sizeof(msghldr));

      vr_process_add_preprocessor_symbol(main_process[RENDER_THD+i],
                                     "IN_CHANNEL",
                                     name);

#ifndef USE_ARC_CHANNEL
      sprintf(name, "display_channel%d", i);
      declare_channel (name,
                       thd[RENDER_THD+i],
                       thd[DISPLAY_THD],
                       32*sizeof(display_msg));

      vr_process_add_preprocessor_symbol(main_process[RENDER_THD+i],
                                     "OUT_CHANNEL",
                                     name);
#endif
    }

#ifdef USE_ARC_CHANNEL
    declare_multiproducer_channel ("display_channel",
                                   NUMBER_OF_RENDER_CPUS,
                                   &thd[RENDER_THD],
                                   thd[DISPLAY_THD]);

    for (i=0;i<NUMBER_OF_RENDER_CPUS;++i) 
      vr_process_add_preprocessor_symbol(main_process[RENDER_THD+i],
                                         "OUT_CHANNEL",
                                         "display_channel");
#endif

    /* build up list of out-going channel list for graphics stage to use */
    strcpy(name, "{ ");
    for (i = 0; i < NUMBER_OF_RENDER_CPUS; ++i)
    {
      sprintf(value, "render_channel%d", i);
      strcat(name, value);
      if (i != (NUMBER_OF_RENDER_CPUS-1))
        strcat(name, ", ");
    }
    strcat(name, " }");
    vr_process_add_preprocessor_symbol(main_process[GR_THD],
                                       "OUT_CHANNEL_LIST",
                                       name);

#ifndef USE_ARC_CHANNEL
    /* build up list of incomming channel list for display stage to use */
    strcpy(name, "{ ");
    for (i = 0; i < NUMBER_OF_RENDER_CPUS; ++i)
    {
      sprintf(value, "display_channel%d", i);
      strcat(name, value);
      if (i != (NUMBER_OF_RENDER_CPUS-1))
        strcat(name, ", ");
    }
    strcat(name, " }");
    vr_process_add_preprocessor_symbol(main_process[DISPLAY_THD],
                                       "IN_CHANNEL_LIST",
                                       name);
#else
    vr_process_add_preprocessor_symbol(main_process[DISPLAY_THD],
                                       "USE_ARC_CHANNEL",
                                       "1");
#endif
 

  /*
     Target specific initialization
   */
  target_specific_configuration ();

  return 0;

}

