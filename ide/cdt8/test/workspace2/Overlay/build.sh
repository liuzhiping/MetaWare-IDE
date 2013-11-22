#!/bin/csh -x
rm ovl2 ovl2.s
hcac -g -Os1 -Hreg_reserve=22,23,24,25 ovl2.c -a5 -S -Hnosdata -Hoff=ldst_from_code
hcac -Os1 ovl2.s libaom.a mock_overlay_loader.o -o ovl2

# tar -fcvh /sol/tmp/rich/ovl2.tar mk ovl2.c ovl2.s libAOM.a
