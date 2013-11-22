VR_OS = no_os
ifeq (,$(findstring \,$(SHELL)))
HOST = unix
else
HOST = windows
endif
VDK_INSTALL_DIR = /home/chuckj/vdkinstalldir
VR_INSTALL_LIB_DIR = $(VDK_INSTALL_DIR)/lib/runtime/arc7xx/$(VR_OS)/release
VR_INSTALL_INCLUDE_DIR = $(VDK_INSTALL_DIR)/include/runtime/arc7xx/$(VR_OS)
VR_INSTALLED_HEADERS = $(wildcard $(VR_INSTALL_INCLUDE_DIR)/*)

VR_UP = arc7xx
VR_NP = 
HW_SDK_INSTALL_DIR = 

HW_FILES_C = 
HW_CFLAGS = -mARC700 -D_ARC7xx -mlittle-endian -msoft-float
HW_CFLAGS += -I/home/chuckj/vdk/trunk/mcsg/examples/arc700_demos/graphics_pipeline/.
HW_LFLAGS = -mARC700 -T arcelf.x
HW_LIBS = 
USR_C = 
VR_EXTRA_C_FILES =   $(HW_FILES_C) $(USR_C) 
C = _rendermain.c _plot3d.c _plot.c _screen.c  vr_application_channel_init.c  vr_application_memory_manager_init.c vr_application_mutex_init.c vr_application_queue_init.c vr_application_scheduler_init.c 

O = $(C:.c=.o) 
ifneq ($(strip$(VR_EXTRA_C_FILES)),)
O += vr_extra_files.o 
endif
.SUFFIXES: .c .o


DEPRECATED_API = on
MAIN = on
DEBUG = off
OPTIMIZATION = on

VR_LFLAGS = -L$(VR_INSTALL_LIB_DIR)

USR_CFLAGS = 
USR_LFLAGS = 
USR_LIBS = 
USR_LWRAP = 
CC = arc-elf32-gcc
LD = $(CC)
VR_LIB = libvr_e_c.a
VR_SW_LIB = libvr_sw_e_c.a
VR_HW_LIB = libvr_hw_np_e_c.a
ifeq ($(MAIN), on)
VR_MAIN_FLAG = -DVR_MAIN 
endif
ifeq ($(DEPRECATED_API), on)
VR_DEPRECATED_FLAG = -DVR_DEPRECATED_API_ON 
endif

VR_CFLAGS = -Wall -c -I. -I$(VR_INSTALL_INCLUDE_DIR) -I$(VDK_INSTALL_DIR)/include $(VR_MAIN_FLAG) $(VR_DEPRECATED_FLAG) -DVR_IS_GENERATED_CODE -DVR_EMBEDDED 
VR_DFLAGS = -DVR_ARG_CHECKING=0 

VR_OPTFLAGS = -O3 

VR_XTRA_LIBS = -lm -Wl,--whole-archive $(VDK_INSTALL_DIR)/lib/libhlt.a -Wl,--no-whole-archive

ifneq (, $(EXTERNAL_DEFINES))
  include $(EXTERNAL_DEFINES)
endif


rendermain0: arcelf.x $(O)  $(VR_INSTALL_LIB_DIR)/$(VR_LIB) $(VR_INSTALL_LIB_DIR)/$(VR_SW_LIB)
	@echo 
	@echo Linking rendermain0 of default for $(VR_OS) $(VR_NP) $(VR_UP)
	@echo 
	@echo 
	@echo "Compiling rendermain0 for no-os $(VR_NP) $(VR_UP)"
	@echo 
	$(LD) -o rendermain0 $(VR_LFLAGS) $(HW_LFLAGS) $(HW_LIBS) $(USR_LFLAGS) $(O) $(USR_LIBS) $(VR_INSTALL_LIB_DIR)/$(VR_LIB) $(VR_INSTALL_LIB_DIR)/$(VR_SW_LIB) $(VR_INSTALL_LIB_DIR)/$(VR_HW_LIB)  $(VR_XTRA_LIBS) $(HW_LIBS) $(USR_LWRAP)


_rendermain.o: _rendermain.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _rendermain.c -o _rendermain.o
_plot3d.o: _plot3d.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _plot3d.c -o _plot3d.o
_plot.o: _plot.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _plot.c -o _plot.o
_screen.o: _screen.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _screen.c -o _screen.o
vr_application_channel_init.o: vr_application_channel_init.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_channel_init.c -o vr_application_channel_init.o
vr_application_memory_manager_init.o: vr_application_memory_manager_init.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_memory_manager_init.c -o vr_application_memory_manager_init.o
vr_application_mutex_init.o: vr_application_mutex_init.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_mutex_init.c -o vr_application_mutex_init.o
vr_application_queue_init.o: vr_application_queue_init.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_queue_init.c -o vr_application_queue_init.o
vr_application_scheduler_init.o: vr_application_scheduler_init.c
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_scheduler_init.c -o vr_application_scheduler_init.o
vr_extra_files.o: $(VR_EXTRA_C_FILES) 
	$(CC) $(VR_CFLAGS) -Winline $(HW_CFLAGS) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  $(VR_EXTRA_C_FILES) -o vr_extra_files.o



clean:
ifeq ($(HOST), unix)
	-rm -f $(O) rendermain0
else
	-del $(subst /,\,$(O)) rendermain0
endif
