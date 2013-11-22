CYGWIN_DIR = c:/cygwin/bin

all:
	$(CYGWIN_DIR)/diff.exe main.c main.c
	$(CYGWIN_DIR)/grep.exe -w for main.c >z.txt
	$(CYGWIN_DIR)/diff.exe z.txt for.ref
	@echo === pass all ===
