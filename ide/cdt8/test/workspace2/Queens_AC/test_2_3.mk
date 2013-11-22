CYGWIN_DIR = c:/cygwin/bin

all:

t_2_2:
	#$(CYGWIN_DIR)/cp.exe queens_2_3a.c queens.c
	$(CYGWIN_DIR)/cp.exe queens_2_3.c queens.c
	echo test_2_2.mk

t_2_3a:
	$(CYGWIN_DIR)/cp.exe queens_2_3.c queens.c
	#$(CYGWIN_DIR)/cp.exe queens_2_3a.c queens.c
	echo test_2_3a.mk

t_2_3b:
	$(CYGWIN_DIR)/cp.exe queens_2_3.c queens.c
	#$(CYGWIN_DIR)/cp.exe queens_2_3a.c queens.c
	echo test_2_3b.mk	