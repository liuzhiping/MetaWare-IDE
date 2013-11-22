
/**************************************************************************
 *
 * Generated by mcsg
 * Version 1.0.0
 * Build trunk
 * Thu May 29 15:34:38 PDT 2008
 * ARC International (c) 2008
 * This is a generated file. All manual edits will be lost.
 *
 **************************************************************************/


#ifndef VR_APPLICATION_DEFINES_H
#define VR_APPLICATION_DEFINES_H


/* Memory bank type indices */
#define generic_OSBasedMemoryBank_membank_type_id 0
#define genericmulticore_GenericMultiCoreMemory_membank_type_id 1
#define vr_undefined_membank_type_id 2

/* Memory bank indices */
#define hwarch_SoC_ssramInst_membank_id 16777216
#define hwarch_SoC_ssramInst_mmap_bank1_ram0_membank_id 16777218

/* Processor memory bank indices */
#define hwarch_SoC_cpu0_membank_id 4
#define hwarch_SoC_cpu1_membank_id 6
#define hwarch_SoC_cpu2_membank_id 8
#define hwarch_SoC_cpu3_membank_id 10

/* Processor indices */
#define hwarch_SoC_cpu0_processor_id 0
#define hwarch_SoC_cpu1_processor_id 1
#define hwarch_SoC_cpu2_processor_id 2
#define hwarch_SoC_cpu3_processor_id 3

/* Channel indices */
#define render_channel0_id 1
#define display_channel0_id 2
#define render_channel1_id 3
#define display_channel1_id 4

/* Thread indices */
#define swarch_thd_grmain_thread_id 0
#define swarch_thd_grmain_chip_local_thread_id 0
#define swarch_thd_displaymain_thread_id 1
#define swarch_thd_displaymain_chip_local_thread_id 1
#define swarch_thd_rendermain0_thread_id 2
#define swarch_thd_rendermain0_chip_local_thread_id 2
#define swarch_thd_rendermain1_thread_id 3
#define swarch_thd_rendermain1_chip_local_thread_id 3

/* Memory pool indices */
#define raster_pool_id 0
#define grcmd_pool_id 1

#endif /* VR_APPLICATION_DEFINES_H */