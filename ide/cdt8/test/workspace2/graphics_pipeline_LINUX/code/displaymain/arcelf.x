
/* Generated arc-elf-32 Linker Script for process "displaymain" */


/* Default linker script, for normal executables */
 OUTPUT_FORMAT("elf32-littlearc", "elf32-bigarc",
"elf32-littlearc")
 OUTPUT_ARCH(arc)
 ENTRY(__start)
 SEARCH_DIR("/opt/arc-elf_c6_bta_fix/arc-elf32/lib");
 /* Do we need any of these for elf?
    __DYNAMIC = 0;    */
 SECTIONS
 {
   /* Read-only sections, merged into text segment: */
   PROVIDE (__executable_start = 0x00380100); . = 0x00380100 + SIZEOF_HEADERS;
   .interp         : { *(.interp) }
   .hash           : { *(.hash) }
   .dynsym         : { *(.dynsym) }
   .dynstr         : { *(.dynstr) }
   .gnu.version    : { *(.gnu.version) }
   .gnu.version_d  : { *(.gnu.version_d) }
   .gnu.version_r  : { *(.gnu.version_r) }
   .rel.init       : { *(.rel.init) }
   .rela.init      : { *(.rela.init) }
   .rel.text       : { *(.rel.text .rel.text.* .rel.gnu.linkonce.t.*) }
   .rela.text      : { *(.rela.text .rela.text.* .rela.gnu.linkonce.t.*) }
   .rel.fini       : { *(.rel.fini) }
   .rela.fini      : { *(.rela.fini) }
   .rel.rodata     : { *(.rel.rodata .rel.rodata.* .rel.gnu.linkonce.r.*) }
   .rela.rodata    : { *(.rela.rodata .rela.rodata.* .rela.gnu.linkonce.r.*) }
   .rel.data       : { *(.rel.data .rel.data.* .rel.gnu.linkonce.d.*) }
   .rela.data      : { *(.rela.data .rela.data.* .rela.gnu.linkonce.d.*) }
   .rel.tdata	  : { *(.rel.tdata .rel.tdata.* .rel.gnu.linkonce.td.*) }
   .rela.tdata	  : { *(.rela.tdata .rela.tdata.* .rela.gnu.linkonce.td.*) }
   .rel.tbss	      : { *(.rel.tbss .rel.tbss.* .rel.gnu.linkonce.tb.*) }
   .rela.tbss	  : { *(.rela.tbss .rela.tbss.* .rela.gnu.linkonce.tb.*) }
   .rel.ctors      : { *(.rel.ctors) }
   .rela.ctors     : { *(.rela.ctors) }
   .rel.dtors      : { *(.rel.dtors) }
   .rela.dtors     : { *(.rela.dtors) }
   .rel.got        : { *(.rel.got) }
   .rela.got       : { *(.rela.got) }
   .rel.sdata      : { *(.rel.sdata .rel.sdata.* .rel.gnu.linkonce.s.*) }
   .rela.sdata     : { *(.rela.sdata .rela.sdata.* .rela.gnu.linkonce.s.*) }
   .rel.sbss       : { *(.rel.sbss .rel.sbss.* .rel.gnu.linkonce.sb.*) }
   .rela.sbss      : { *(.rela.sbss .rela.sbss.* .rela.gnu.linkonce.sb.*) }
   .rel.sdata2     : { *(.rel.sdata2 .rel.sdata2.* .rel.gnu.linkonce.s2.*) }
   .rela.sdata2    : { *(.rela.sdata2 .rela.sdata2.* .rela.gnu.linkonce.s2.*) }
   .rel.sbss2      : { *(.rel.sbss2 .rel.sbss2.* .rel.gnu.linkonce.sb2.*) }
   .rela.sbss2     : { *(.rela.sbss2 .rela.sbss2.* .rela.gnu.linkonce.sb2.*) }
   .rel.bss        : { *(.rel.bss .rel.bss.* .rel.gnu.linkonce.b.*) }
   .rela.bss       : { *(.rela.bss .rela.bss.* .rela.gnu.linkonce.b.*) }
   .rel.plt        : { *(.rel.plt) }
   .rela.plt       : { *(.rela.plt) }
   .init           :
   {
     KEEP (*(.init))
   } =0
   .plt            : { *(.plt) }
   .text           :
   {
     *(.text .stub .text.* .gnu.linkonce.t.*)
     /* .gnu.warning sections are handled specially by elf32.em.  */
     *(.gnu.warning)
   } =0
   .text.init   :
   {
     *(.text.init)
   } =0
   .fini           :
   {
     KEEP (*(.fini))
   } =0
   PROVIDE (__etext = .);
   PROVIDE (_etext = .);
   PROVIDE (etext = .);
   .rodata         : { *(.rodata .rodata.* .gnu.linkonce.r.*) }
   .rodata1        : { *(.rodata1) }
   .sdata2         : { *(.sdata2 .sdata2.* .gnu.linkonce.s2.*) }
   .sbss2          : { *(.sbss2 .sbss2.* .gnu.linkonce.sb2.*) }
   .eh_frame_hdr : { *(.eh_frame_hdr) }
   /* Adjust the address for the data segment.  We want to adjust up to
      the same address within the page on the next page up.  */
   . = ALIGN(CONSTANT (MAXPAGESIZE)) + (. & (CONSTANT (MAXPAGESIZE) - 1));
   /* Ensure the __preinit_array_start label is properly aligned.  We
      could instead move the label definition inside the section, but
      the linker would then create the section even if it turns out to
      be empty, which isn't pretty.  */
   . = ALIGN(32 / 8);
   PROVIDE (__preinit_array_start = .);
   .preinit_array     : { *(.preinit_array) }
   PROVIDE (__preinit_array_end = .);
   PROVIDE (__init_array_start = .);
   .init_array     : { *(.init_array) }
   PROVIDE (__init_array_end = .);
   PROVIDE (__fini_array_start = .);
   .fini_array     : { *(.fini_array) }
   PROVIDE (__fini_array_end = .);
   .data           :
   {
     *(.data .data.* .gnu.linkonce.d.*)
     SORT(CONSTRUCTORS)
   }
   .data.init    :
   {
     *(.data.init)
   }
   .data1          : { *(.data1) }
   .tdata	  : { *(.tdata .tdata.* .gnu.linkonce.td.*) }
   .tbss		  : { *(.tbss .tbss.* .gnu.linkonce.tb.*) *(.tcommon) }
   .eh_frame       : { KEEP (*(.eh_frame)) }
   .gcc_except_table   : { *(.gcc_except_table) }
   .dynamic        : { *(.dynamic) }
   .ctors          :
   {
     /* gcc uses crtbegin.o to find the start of
        the constructors, so we make sure it is
        first.  Because this is a wildcard, it
        doesn't matter if the user does not
        actually link against crtbegin.o; the
        linker won't look for a file to match a
        wildcard.  The wildcard also means that it
        doesn't matter which directory crtbegin.o
        is in.  */
     KEEP (*crtbegin*.o(.ctors))
     /* We don't want to include the .ctor section from
        from the crtend.o file until after the sorted ctors.
        The .ctor section from the crtend file contains the
        end of ctors marker and it must be last */
     KEEP (*(EXCLUDE_FILE (*crtend*.o ) .ctors))
     KEEP (*(SORT(.ctors.*)))
     KEEP (*(.ctors))
   }
   .dtors          :
   {
     KEEP (*crtbegin*.o(.dtors))
     KEEP (*(EXCLUDE_FILE (*crtend*.o ) .dtors))
     KEEP (*(SORT(.dtors.*)))
     KEEP (*(.dtors))
   }
   .jcr            : { KEEP (*(.jcr)) }
   .got            : { *(.got.plt) *(.got) }
   /* We want the small data sections together, so single-instruction offsets
      can access them all, and initialized data all before uninitialized, so
      we can shorten the on-disk segment size.  */
   .sdata          :
   {
     __SDATA_BEGIN__ = .;
     *(.sdata .sdata.* .gnu.linkonce.s.*)
   }
   _edata = .;
   PROVIDE (edata = .);
   __bss_start = .;
   .sbss           :
   {
     PROVIDE (__sbss_start = .);
     PROVIDE (___sbss_start = .);
     *(.dynsbss)
     *(.sbss .sbss.* .gnu.linkonce.sb.*)
     *(.scommon)
     PROVIDE (__sbss_end = .);
     PROVIDE (___sbss_end = .);
   }
   .bss            :
   {
    *(.dynbss)
    *(.bss .bss.* .gnu.linkonce.b.*)
    *(COMMON)
    /* Align here to ensure that the .bss section occupies space up to
       _end.  Align after .bss to ensure correct alignment even if the
       .bss section disappears because there are no input sections.  */
    . = ALIGN(32 / 8);
   }
   . = ALIGN(32 / 8);
   _end = .;
   PROVIDE (end = .);
   .heap   :
   {
          __start_heap = . ;
          . = . + 262144;
          __end_heap = . ;
   }
   . = ALIGN(0x8);
   .stack   :
   {
          __stack = . ;
          . = . + 16384;
          __stack_top = . ;
   }
   /* Stabs debugging sections.  */
   .stab          0 : { *(.stab) }
   .stabstr       0 : { *(.stabstr) }
   .stab.excl     0 : { *(.stab.excl) }
   .stab.exclstr  0 : { *(.stab.exclstr) }
   .stab.index    0 : { *(.stab.index) }
   .stab.indexstr 0 : { *(.stab.indexstr) }
   .comment       0 : { *(.comment) }
   .arcextmap 0 : { *(.arcextmap) }
   /* DWARF debug sections.
      Symbols in the DWARF debugging sections are relative to the beginning
      of the section so we begin them at 0.  */
   /* DWARF 1 */
   .debug          0 : { *(.debug) }
   .line           0 : { *(.line) }
   /* GNU DWARF 1 extensions */
   .debug_srcinfo  0 : { *(.debug_srcinfo) }
   .debug_sfnames  0 : { *(.debug_sfnames) }
   /* DWARF 1.1 and DWARF 2 */
   .debug_aranges  0 : { *(.debug_aranges) }
   .debug_pubnames 0 : { *(.debug_pubnames) }
   /* DWARF 2 */
   .debug_info     0 : { *(.debug_info .gnu.linkonce.wi.*) }
   .debug_abbrev   0 : { *(.debug_abbrev) }
   .debug_line     0 : { *(.debug_line) }
   .debug_frame    0 : { *(.debug_frame) }
   .debug_str      0 : { *(.debug_str) }
   .debug_loc      0 : { *(.debug_loc) }
   .debug_macinfo  0 : { *(.debug_macinfo) }
   /* SGI/MIPS DWARF 2 extensions */
   .debug_weaknames 0 : { *(.debug_weaknames) }
   .debug_funcnames 0 : { *(.debug_funcnames) }
   .debug_typenames 0 : { *(.debug_typenames) }
   .debug_varnames  0 : { *(.debug_varnames) }
   /* ARC Extension Sections */
   .arcextmap 0 :
   {
     *(.gnu.linkonce.arcextmap.*)
   }

  /* memory bank "ssramInst_mmap_bank1_ram0" */
  /*  physical address: 0x00e00000 */
   . =  0x00e00000;

   /* user section ".frame" */
   .frame (NOLOAD) : { *(.frame) } 

   /* user section ".grcmd_pool" */
   .grcmd_pool (NOLOAD) : { *(.grcmd_pool) } 

   /* user section ".raster_pool" */
   .raster_pool (NOLOAD) : { *(.raster_pool) } 

   /* user section ".zbarrier" */
   .zbarrier (NOLOAD) : { *(.zbarrier) } 



  /* memory bank "ssramInst" */
  /*  physical address: 0x02000000 */
   . =  0x02000000;

   /* user section ".display_channel0" */
   .display_channel0 (NOLOAD) : { *(.display_channel0) } 

   /* user section ".display_channel1" */
   .display_channel1 (NOLOAD) : { *(.display_channel1) } 

   . = ALIGN(4) + 528; 

   . = ALIGN(4) + 528; 


  /DISCARD/ : { *(.note.GNU-stack) }
}
