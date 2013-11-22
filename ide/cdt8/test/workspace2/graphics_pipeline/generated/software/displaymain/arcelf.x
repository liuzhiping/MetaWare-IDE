
/* generated Bare Hardware Linker Script for process "displaymain" */


MEMORY
{
              vtable:  origin = 0x00000000, length = 0x00000800
  OS_1_imemory_model:  origin = 0x03300000, length = 0x00300000
       imemory_model:  origin = 0x03c00000, length = 0x0c400000
}

SECTIONS
{
  /* memory bank "imemory_model" */

  GROUP : {
    .vectors: {}
  } > vtable

  GROUP : {
    * (TEXT): {}
    * (LIT): {}
  } > OS_1_imemory_model

  GROUP : {
    .data ALIGN(8) BLOCK(8):
    {
      . = ALIGN(8);
      _SDA_BASE_ = .;
      * (.rosdata, .sdata, .sbss)
      . = ALIGN(8);
      _SDA2_BASE_ = .;
      * (.rosdata2, .sdata2, .sbss2)
      * (TYPE data)
    }
    .bss:
    {
       * (TYPE bss)
    }
    .heap (BSS) ALIGN(8) BLOCK(8):
    {
      ___h1 = .;
      * (.heap)
      ___h2 = .;
      . += 262144;
    }
    .stack (BSS) ALIGN(8) BLOCK(8):
    {
      ___s1 = .;
      * (.stack)
      ___s2 = .;
      . += 16384;
    }
  } > OS_1_imemory_model

   /* user section ".aframe" */
   GROUP : {
     .aframe (NOLOAD) : {}
   } > imemory_model

   /* user section ".display_channel0" */
   GROUP : {
     .display_channel0 (NOLOAD) : {}
   } > imemory_model

   /* user section ".display_channel1" */
   GROUP : {
     .display_channel1 (NOLOAD) : {}
   } > imemory_model

   /* user section ".grcmd_pool" */
   GROUP : {
     .grcmd_pool (NOLOAD) : {}
   } > imemory_model

   /* user section ".raster_pool" */
   GROUP : {
     .raster_pool (NOLOAD) : {}
   } > imemory_model

   GROUP ALIGN(4) SIZE(528): { 
     .render_channel0_for_grmain (NOLOAD) :{}
   } > imemory_model

   GROUP ALIGN(4) SIZE(528): { 
     .render_channel1_for_grmain (NOLOAD) :{}
   } > imemory_model

   /* user section ".zbarrier" */
   GROUP : {
     .zbarrier (NOLOAD) : {}
   } > imemory_model

}

__FREE_MEM = ADDR(.stack) + SIZEOF(.stack);
__FREE_MEM_END = ADDR(OS_1_imemory_model) + SIZEOF(OS_1_imemory_model);
/* EOF */
