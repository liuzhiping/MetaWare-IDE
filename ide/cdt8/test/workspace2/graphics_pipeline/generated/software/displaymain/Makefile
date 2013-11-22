VR_OS = raw_arc
ifeq (,$(findstring \,$(SHELL)))
HOST = unix
else
HOST = windows
endif
VDK_INSTALL_DIR = C:/ARC/VDK
VR_INSTALL_LIB_DIR = $(VDK_INSTALL_DIR)/lib/runtime/arc7xx/$(VR_OS)/debug
VR_INSTALL_INCLUDE_DIR = $(VDK_INSTALL_DIR)/include/runtime/arc7xx/$(VR_OS)
VR_INSTALLED_HEADERS = $(wildcard $(VR_INSTALL_INCLUDE_DIR)/*)

VR_UP = arc7xx
VR_NP = 
HW_SDK_INSTALL_DIR = 
ifeq ($(METAWARE_INSTALL_DIR),)
$(error METAWARE_INSTALL_DIR not set)
endif
METAWARE_ROOT = $(METAWARE_INSTALL_DIR)

HW_FILES_C = 
HW_CFLAGS = -arc700 -D_ARC7xx -Xvraptor -Dinline=_Inline -HL -Hhostlib=  -Bstatic -Bgrouplib -Hsdata0 -Hoff=volatile_cache_bypass -Hunixerr -Xmpy -I $(METAWARE_ROOT)/arc/inc
HW_CFLAGS += -IC:/VDK_stuff/VDK/trunk/mcsg/examples/arc700_demos/graphics_pipeline/.
HW_LFLAGS =  -Bstatic -Bgrouplib -Dinline=_Inline -HL -arc700 -D_ARC7xx -e _start arcelf.x -Hldopt=-m -Hldopt=-Coutput=memory_map.txt -L$(METAWARE_ROOT)/arc/lib/a7/le -lct
HW_LIBS = \

USR_C = 
VR_EXTRA_C_FILES =   $(HW_FILES_C) $(USR_C) 
C = _displaymain.c _plot.c _screen.c  vr_application_channel_init.c  vr_application_memory_manager_init.c vr_application_mutex_init.c vr_application_queue_init.c vr_application_scheduler_init.c 

O = $(C:.c=.o) 
ifneq ($(strip$(VR_EXTRA_C_FILES)),)
O += vr_extra_files.o 
endif
.SUFFIXES: .c .o


DEPRECATED_API = on
MAIN = on
DEBUG = on
OPTIMIZATION = off

VR_LFLAGS = -L$(VR_INSTALL_LIB_DIR)

USR_INC = 

USR_CFLAGS = 
USR_LFLAGS = 
USR_OBJS = 

USR_LIBS = 

USR_LWRAP = 
CC = mcc
LD = mcc
VR_LIB = libvr_e_c.a
VR_SW_LIB = libvr_sw_e_c.a
VR_HW_LIB = libvr_hw_np_e_c.a
ifeq ($(MAIN), on)
VR_MAIN_FLAG = -DVR_MAIN 
endif
ifeq ($(DEPRECATED_API), on)
VR_DEPRECATED_FLAG = -DVR_DEPRECATED_API_ON 
endif

VR_CFLAGS = -w4 -c -I. -I$(VR_INSTALL_INCLUDE_DIR) -I$(VDK_INSTALL_DIR)/include $(VR_MAIN_FLAG) $(VR_DEPRECATED_FLAG) -DVR_IS_GENERATED_CODE -DVR_EMBEDDED 
VR_DFLAGS = -g -DVR_DEBUG -DVR_ARG_CHECKING=1 

VR_OPTFLAGS = 
CC_OPTIM      = 

VR_XTRA_LIBS = 

ifneq (, $(EXTERNAL_DEFINES))
  include $(EXTERNAL_DEFINES)
endif


displaymain: arcelf.x $(O)  $(VR_INSTALL_LIB_DIR)/$(VR_LIB) $(VR_INSTALL_LIB_DIR)/$(VR_SW_LIB)
	@echo 
	@echo Linking displaymain of default for $(VR_OS) $(VR_NP) $(VR_UP)
	@echo 
	@echo 
	@echo "Compiling displaymain for no_os_legacy $(VR_NP) $(VR_UP)"
	@echo 
	$(LD) -o displaymain $(VR_LFLAGS) $(HW_LFLAGS) $(HW_LIBS) $(USR_LFLAGS) $(O) $(USR_OBJS) $(USR_LIBS) $(VR_INSTALL_LIB_DIR)/$(VR_LIB) $(VR_INSTALL_LIB_DIR)/$(VR_SW_LIB) $(VR_INSTALL_LIB_DIR)/$(VR_HW_LIB)  $(VR_XTRA_LIBS) $(HW_LIBS) $(USR_LWRAP)


_displaymain.o: _displaymain.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _displaymain.c -o _displaymain.o
_plot.o: _plot.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _plot.c -o _plot.o
_screen.o: _screen.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  _screen.c -o _screen.o
vr_application_channel_init.o: vr_application_channel_init.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_channel_init.c -o vr_application_channel_init.o
vr_application_memory_manager_init.o: vr_application_memory_manager_init.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_memory_manager_init.c -o vr_application_memory_manager_init.o
vr_application_mutex_init.o: vr_application_mutex_init.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_mutex_init.c -o vr_application_mutex_init.o
vr_application_queue_init.o: vr_application_queue_init.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_queue_init.c -o vr_application_queue_init.o
vr_application_scheduler_init.o: vr_application_scheduler_init.c
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  vr_application_scheduler_init.c -o vr_application_scheduler_init.o
vr_extra_files.o: $(VR_EXTRA_C_FILES) 
	$(CC) $(VR_CFLAGS)  $(HW_CFLAGS) $(USR_INC) $(USR_CFLAGS) $(VR_DFLAGS) $(VR_OPTFLAGS)  $(VR_EXTRA_C_FILES) -o vr_extra_files.o



clean:
ifeq ($(HOST), unix)
	-rm -f $(O) displaymain
else
	-del $(subst /,\,$(O)) displaymain
endif
