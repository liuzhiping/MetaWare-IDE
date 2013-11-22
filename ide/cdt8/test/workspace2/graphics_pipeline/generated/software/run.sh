#!/bin/bash

# Setup for process "grmain"
scac \
  -nogoifmain \
  -pset=1 \
  -prop=instrs_per_pass=50 \
  -psetname=grmain \
  grmain/grmain \
  -cmpd=soc \
  -simextp=name=imemory_model1.initiator,out=0x03c00000 \
  -simextp=name=imemory_model2.initiator,out=0x03c00000 \
  -simextp=name=imemory_model3.initiator,out=0x03c00000 \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -icache=32768,32,2,a \
  -dcache=32768,32,4,a \
  -on=stop_all

# Setup for process "displaymain"
scac \
  -nogoifmain \
  -pset=2 \
  -prop=instrs_per_pass=50 \
  -psetname=displaymain \
  displaymain/displaymain \
  -cmpd=soc \
  -simextp=name=imemory_model1.target,in=0x03c00000:0x0fffffff \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -icache=32768,32,2,a \
  -dcache=32768,32,4,a \
  -on=stop_all

# Setup for process "rendermain0"
scac \
  -nogoifmain \
  -pset=3 \
  -prop=instrs_per_pass=50 \
  -psetname=rendermain0 \
  rendermain0/rendermain0 \
  -cmpd=soc \
  -simextp=name=imemory_model2.target,in=0x03c00000:0x0fffffff \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -icache=32768,32,2,a \
  -dcache=32768,32,4,a \
  -on=stop_all

# Setup for process "rendermain1"
scac \
  -nogoifmain \
  -pset=4 \
  -prop=instrs_per_pass=50 \
  -psetname=rendermain1 \
  rendermain1/rendermain1 \
  -cmpd=soc \
  -simextp=name=imemory_model3.target,in=0x03c00000:0x0fffffff \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -icache=32768,32,2,a \
  -dcache=32768,32,4,a \
  -on=stop_all

# Launch the debugger! 
scac \
  -multifiles=grmain,displaymain,rendermain0,rendermain1 \
  -cmpd=soc \
  -off=cr_for_more  \
  -simexts=bridge,instance=imemory_model1 \
  -simexts=bridge,instance=imemory_model2 \
  -simexts=bridge,instance=imemory_model3 \
  -OKN $@

