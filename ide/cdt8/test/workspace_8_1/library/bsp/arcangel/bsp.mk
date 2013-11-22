#
# Define the following variables:
#
#     srcs          list of source files without extensions
#     srcdirs       list of directories containing source files
#     mibdirs       list of directories containing MIB definitions
#
#     dups          list of files to be copied to the output directory
#     dupdirs       list of directories containing the 'dups' files
#

## Start CR 2398
## Moved these includes to mqx.mk They are now needed by examples too
# add more directories to search for header files here:
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)enet
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)serial
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)timer
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)io_mem
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)io_dun
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)io_null
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)flashx
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)pipe
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)io_ide
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)io_ide$(PS)disk_ST3120022A
## End CR 2398

dupdirs += bsp$(PS)$(MQX_BSP_SRC_DIR) io io$(PS)io_mem io$(PS)io_null io$(PS)io_dun
dupdirs += io$(PS)enet  io$(PS)enet$(PS)vmac io$(PS)serial  io$(PS)timer io$(PS)flashx io$(PS)flashx$(PS)istrata
dupdirs += io$(PS)pipe  io$(PS)io_ide  io$(PS)io_ide$(PS)disk_ST3120022A
## Start CR 2396  
dupdirs += include
## End CR 2396

dups    += bsp_rev.h io_rev.h  
dups    += serial.h  io_mem.h   io_null.h   io_dun.h
dups    += $(MQX_BSP).h  $(ARC_CONFIG).h

ifeq ($(MQX_PSP),arca7)
dups += acaa700.h
endif
ifeq ($(MQX_PSP),arca6)
dups += acaa600.h
endif

# Only include link.met and bsp.h if using older makefiles
# MQX Builder generates them
ifneq ($(MQX_CONFIG),)
dups += link.met link_flash.met bsp.h
endif

dups    += mw_uart.h   vuart.h   svuart.h
dups    += enet.h emwsim.h vmac.h lxt970a.h  
dups    += flashx.h istrata.h
dups    += io_pipe.h
dups    += io_ide.h io_disk.h ata.h
## Start CR 2396
dups    += pmu.h
## End CR 2396

srcdirs += bsp$(PS)$(MQX_BSP_SRC_DIR)
srcs    += \
   vectors \
   restart \
   bsp_clk get_nsec get_usec init_bsp initmwua initvart \
   $(MQX_COMPILER) mqx_init tls initide timer1 initpmu

srcdirs += io$(PS)io_dun
srcs    += io_dun

srcdirs += io$(PS)io_mem
srcs    += io_mem

srcdirs += io$(PS)io_null
srcs    += io_null

# IDE - Disk
srcdirs += io$(PS)io_ide
srcs    += io_ide  
srcdirs += io$(PS)io_ide$(PS)disk_ST3120022A
srcs    += io_diskprv io_disk  

srcdirs += io$(PS)pipe
srcs    += io_pipe

srcdirs += io$(PS)serial$(PS)int
srcs    += ivuart serl_int

srcdirs += io$(PS)serial$(PS)polled
srcs    += mw_uart pvuart serl_pol

# Flash
## Start CR 2398
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)flashx$(PS)istrata
## End CR 2398
srcs    += initflash

srcdirs += io$(PS)flashx
srcs    += flashx

srcdirs += io$(PS)flashx$(PS)istrata
srcs    += istrata istrata1 istrata2 istrata4

# Ethernet - Generic
srcdirs += io$(PS)enet
srcs    += \
   enaddr enclose enerr enjoin enleave enopen \
   enrecv ensend enstat  

ifeq ($(BSP_SIM_MODE),)
# VMAC
## Start CR 2398
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)enet$(PS)vmac
## End CR 2398
srcs    += enetvini
srcdirs += io$(PS)enet$(PS)vmac
srcs    += eninit enjoin0 enpoll enrejoin ensend0 enstop vmdio

else
# Simulator Ethernet
## Start CR 2398
#MQX_INCDIRS += $(MQX_ROOT)$(PS)library$(PS)io$(PS)enet$(PS)mwsim
## End CR 2398
CPPFLAGS_EXTRA+=-DBSP_SIM_MODE
srcs    += enetsini
srcdirs += io$(PS)enet$(PS)mwsim
srcs    +=  eninit enjoin0 enrejoin ensend0 enstop
endif


# if the C runtime is not required, compile in a minimal startup function
ifdef MQX_NO_C_COMPILER_RUNTIME
srcs += arc_main
endif

## Start CR 2396
srcdirs += pmu
srcs    += \
   ep_pmu  ep_dvfs  dvfs_time   pmu_wakeup
## End CR 2396 

# EOF
