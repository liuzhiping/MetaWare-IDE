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
#include <assert.h>
#include "vr_software_architecture.h"
#include "vr_mapping.h"

#include "common.h"
#include "config.h"


int
mapping (void)
{
  vr_thread_t th;
  char name[40];
  char process_name[40];
  char entry_point[40];
  int i;

  vr_memory_t dram = vr_component_get_memory (NULL, "board.dram");
  vr_memory_t sram = vr_component_get_memory (NULL, "board.sram");

  for (i = 0; i < NUMBER_OF_CPU_ISLANDS; ++i)
    {
      /* --------------- map for one cpu island -------------------- */

      if (i == GR_THD) {

        strcpy(process_name, "grmain");
        strcpy(entry_point, "_grmain");

      } else if (i == DISPLAY_THD) {

        strcpy(process_name, "displaymain");
        strcpy(entry_point, "_displaymain");

      } else {

        sprintf(process_name, "%s%d", "rendermain", i-RENDER_THD);
        strcpy(entry_point, "_rendermain");

      }

      sprintf (name, "thd_%s", process_name);

      th = vr_lookup_thread (name);
      assert (th != (vr_thread_t)NULL);
      vr_map_thread_entry_function (entry_point, th);

      vr_process_t process = vr_lookup_process (process_name);
      assert (process != (vr_process_t)NULL);

      if (i == DISPLAY_THD)
        vr_map_section_to_memory (".frame", process, sram);

#if 0
      vr_map_section_to_memory (".shared1", process, dram);
      vr_map_section_to_memory (".shared2", process, dram);
#endif
    }

  /* ----------------------------------------- */

  /*
     Alias the shared sections
   */

#if 0
  vr_process process0 = vr_lookup_process("main0");
  for (i = 1; i < NUMBER_OF_CPU_ISLANDS; ++i)
    {
      sprintf (name, "main%d", i);
      vr_process_t process = vr_lookup_process(name);
      vr_alias_section (".shared1", process0, ".shared1", process);
      vr_alias_section (".shared2", process0, ".shared2", process);
    }
#endif

  /*
     Map Memory Pool(s)
   */
  vr_memory_pool_t memory_pool = vr_lookup_memory_pool ("raster_pool");
  assert (memory_pool != (vr_memory_pool_t)NULL);
  vr_map_memory_pool (memory_pool, dram);

  memory_pool = vr_lookup_memory_pool ("grcmd_pool");
  assert (memory_pool != (vr_memory_pool_t)NULL);
  vr_map_memory_pool (memory_pool, dram);

  return 0;
}

