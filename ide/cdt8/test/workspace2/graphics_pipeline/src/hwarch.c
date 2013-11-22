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

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "vr_hardware_architecture.h"
#include "common.h"
#include "config.h"


#define CORE_FREQUENCY "94"     /* 94Mhz */
/* NOTE: Change CORE_FREQUENCY to match the frequency of the cpu
   on your board.
 */


extern vr_architecture_t
create_ARC700_CPU_Island (vr_architecture_t container, const char *name);
 /* create_ARC700_CPU_Island() is found in the vrcc deliverable */


#ifdef USE_ARC_CHANNEL
void
wire_up_channels(vr_architecture_t soc)
{
  vr_architecture_t gr_island;
  vr_architecture_t display_island;
  vr_architecture_t render_island;
  vr_bus_t ch_bus;
  vr_bus_t display_ch_bus;
  char str[40];
  int i;

  sprintf(str, "island%d", GR_THD);
  gr_island = vr_lookup_architecture(soc, str);
  assert(gr_island != NULL);

  sprintf(str, "island%d", DISPLAY_THD);
  display_island = vr_lookup_architecture(soc, str);
  assert(display_island != NULL);
  sprintf(str, "display_channel");
  display_ch_bus = vr_bus_create (soc,
                                  str,
                                  VR_BUS_TYPE_ARCCHANNEL,
                                  VR_INTERNAL_BUS);
  vr_architecture_connect(display_island, "channel_in_bus", display_ch_bus);

  for (i=0;i<NUMBER_OF_RENDER_CPUS;++i) {

    sprintf(str, "island%d", RENDER_THD+i);
    render_island = vr_lookup_architecture(soc, str);
    assert(render_island != NULL);

    sprintf(str, "render_channel%d", i);
    ch_bus = vr_bus_create (soc,
                            str,
                            VR_BUS_TYPE_ARCCHANNEL,
                            VR_INTERNAL_BUS);

    /* wire up channel from graphics cpu to render cpu */
    sprintf(str, "channel_out_bus%d", i);
    vr_architecture_connect(gr_island, str, ch_bus);
    vr_architecture_connect(render_island, "channel_in_bus", ch_bus);
 
    vr_architecture_connect(render_island, "channel_out_bus0", display_ch_bus);
  }
}
#endif /* USE_ARC_CHANNEL */


vr_architecture_t
create_SOC_architecture (vr_architecture_t container,
                         const char *name,
                         int num_cpu_islands)
{
  vr_bus_t bvci_bus;
  vr_architecture_t island;
  char island_name[40];
  int i;
  vr_architecture_t SoC =
    vr_architecture_create (container, name,
                              VR_ARCHITECTURE_TYPE_ARC_CHIP);

  vr_architecture_set_property (SoC, "clock_frequency",
                                  CORE_FREQUENCY);

  bvci_bus =
    vr_bus_create (SoC, "bvci_bus", VR_BUS_TYPE_BVCI,
                     VR_EXPORTED_BUS);

  for (i = 0; i < num_cpu_islands; ++i) {

    sprintf(island_name, "island%d", i);

    island = create_ARC700_CPU_Island(SoC, island_name);

    vr_architecture_connect(island, "bvci_bus", bvci_bus);
    /* NOTE: All cpu islands are wired to common bvci_bus */

    vr_processor_t proc = vr_lookup_processor(island, "ARC700");
    assert(proc != NULL);
    vr_processor_set_property(proc, "multiply", "false");
    vr_processor_set_property(proc, "mmu", "false");

  }

  return SoC;
}


vr_architecture_t
create_board_architecture (vr_architecture_t container,
                                   const char *name)
{
  vr_architecture_t board, SoC;
  vr_memory_t dram;
  vr_memory_t sram;
  vr_bus_t bvci_bus;
  char dram_size[40];

  board = vr_architecture_create (container, name, "user_defined");

  SoC = create_SOC_architecture (board, "SoC", NUMBER_OF_CPU_ISLANDS);

#ifdef USE_ARC_CHANNEL
  wire_up_channels(SoC);
#endif

  bvci_bus =
    vr_bus_create (board, "bvci_bus", VR_BUS_TYPE_BVCI,
                     VR_INTERNAL_BUS);

  /*
    Create the DRAM Memory Bank
   */
  dram = vr_memory_create (board, "dram", VR_MEMORY_TYPE_MEMORY);
  vr_memory_connect (dram, NULL, bvci_bus);

  vr_memory_set_property(dram, VR_PROPERTY_MEMORY_PHYSICAL_ADDRESS,
                                                          "0x00000000");
  vr_memory_set_property(dram, VR_PROPERTY_MEMORY_OFFSET, "0x00000000");
  sprintf(dram_size,"0x%08x",TOTAL_DRAM_SIZE);
  vr_memory_set_property(dram, VR_PROPERTY_MEMORY_SIZE,   dram_size);
  vr_memory_set_property(dram, VR_PROPERTY_MEMORY_BIT_ALIGNMENT, "64");

  /*
    Create the SRAM Memory Bank (optional)
   */
  sram = vr_memory_create (board, "sram", VR_MEMORY_TYPE_MEMORY);
  vr_memory_connect (sram, NULL, bvci_bus);

  vr_memory_set_property(sram, VR_PROPERTY_MEMORY_PHYSICAL_ADDRESS,
                                                          "0x10000000");
  vr_memory_set_property(sram, VR_PROPERTY_MEMORY_OFFSET, "0x00000000");
  vr_memory_set_property(sram, VR_PROPERTY_MEMORY_SIZE,   "0x00200000");
  vr_memory_set_property(sram, VR_PROPERTY_MEMORY_BIT_ALIGNMENT, "32");

  vr_architecture_connect (SoC, "bvci_bus", bvci_bus);

  return board;
}


int
hwarch (void)
{
  vr_architecture_t board;

  board = create_board_architecture(NULL, (char *)BOARD_NAME);

  return 0;

}

