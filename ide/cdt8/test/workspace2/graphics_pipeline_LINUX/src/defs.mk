
ifeq ($(findstring CYGWIN,$(HOST)),CYGWIN)

LIB_PREFIX =
LIB_EXT = dll
LOCAL_VDK_INSTALL_DIR := $(shell cygpath $(subst \,/, $(VDK_INSTALL_DIR)))
NATIVE_VDK_INSTALL_DIR := $(shell cygpath -m $(VDK_INSTALL_DIR))
CC = c:\cygwin\bin\gcc
LD = $(CC)
HOSTCC_FLAGS = -g -Wall -Wwrite-strings -mno-cygwin -DVR_CYGWIN
LDFLAGS = -shared -mno-cygwin
TARGET=arc
CSP_C = $(LOCAL_VDK_INSTALL_DIR)/src/csp/arc/ARCAngel4.c

else
ifeq ($(HOST),Linux)

LIB_PREFIX = lib
LIB_EXT = so
LOCAL_VDK_INSTALL_DIR = $(VDK_INSTALL_DIR)
NATIVE_VDK_INSTALL_DIR := $(VDK_INSTALL_DIR)
CC = gcc
LD = gcc
HOSTCC_FLAGS = -fPIC -g -Wall -Wwrite-strings -DVR_LINUX -fPIC
LDFLAGS = -shared
TARGET=arc
CSP_C = $(VDK_INSTALL_DIR)/src/csp/arc/ARCAngel4.c

else

$(error No supported OS found)

endif
endif
