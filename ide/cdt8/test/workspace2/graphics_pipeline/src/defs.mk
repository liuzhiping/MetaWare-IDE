
ifeq ($(findstring CYGWIN,$(HOST)),CYGWIN)

LIB_PREFIX =
LIB_EXT = dll
LOCAL_VR_INSTALL_DIR := $(shell cygpath $(subst \,/, $(VR_INSTALL_DIR)))
NATIVE_VR_INSTALL_DIR := $(shell cygpath -m $(VR_INSTALL_DIR))
CC = gcc
LD = gcc
HOSTCC_FLAGS = -g -Wall -Wwrite-strings -mno-cygwin -DVR_CYGWIN
LDFLAGS = -shared -mno-cygwin
TARGET=arc
CSP_C = $(LOCAL_VR_INSTALL_DIR)/src/csp/arc/ARCAngel4.c

else 
ifeq ($(findstring Win,$(HOST)),Win)
# MKS shell?
LIB_PREFIX =
LIB_EXT = dll
LOCAL_VR_INSTALL_DIR = $(VR_INSTALL_DIR)
NATIVE_VR_INSTALL_DIR := $(VR_INSTALL_DIR)
CC = gcc
LD = gcc
HOSTCC_FLAGS = -g -Wall -Wwrite-strings -mno-cygwin -DVR_CYGWIN
LDFLAGS = -shared -mno-cygwin
TARGET=arc
CSP_C = $(LOCAL_VR_INSTALL_DIR)/src/csp/arc/ARCAngel4.c
else 
ifeq ($(HOST),Linux)

LIB_PREFIX = lib
LIB_EXT = so
LOCAL_VR_INSTALL_DIR = $(VR_INSTALL_DIR)
NATIVE_VR_INSTALL_DIR := $(VR_INSTALL_DIR)
CC = gcc
LD = gcc
HOSTCC_FLAGS = -fPIC -g -Wall -Wwrite-strings -DVR_LINUX -fPIC
LDFLAGS = -shared
TARGET=arc
CSP_C = $(VR_INSTALL_DIR)/src/csp/arc/ARCAngel4.c

else

$(error No supported OS found)


endif
endif
endif
