#!/bin/bash

# Setup for process "grmain"
scac \
  -prop=ident=0 \
  -nogoifmain \
  -pset=1 \
  -prop=instrs_per_pass=500 \
  -psetname=grmain \
  grmain/grmain \
  -cmpd=soc \
  -simextp=name=ssramInst_mmap_bank1_ram01.initiator,out=0x00e00000 \
  -simextp=name=ssramInst_mmap_bank1_ram02.initiator,out=0x00e00000 \
  -simextp=name=ssramInst_mmap_bank1_ram03.initiator,out=0x00e00000 \
  -simextp=name=ssramInst1.initiator,out=0x02000000 \
  -simextp=name=ssramInst2.initiator,out=0x02000000 \
  -simextp=name=ssramInst3.initiator,out=0x02000000 \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -on=stop_all

# Setup for process "displaymain"
scac \
  -prop=ident=0 \
  -nogoifmain \
  -pset=2 \
  -prop=instrs_per_pass=500 \
  -psetname=displaymain \
  displaymain/displaymain \
  -cmpd=soc \
  -simextp=name=ssramInst_mmap_bank1_ram01.target,in=0x00e00000:0x01ffffff \
  -simextp=name=ssramInst1.target,in=0x02000000:0x02000fff \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -on=stop_all

# Setup for process "rendermain0"
scac \
  -prop=ident=0 \
  -nogoifmain \
  -pset=3 \
  -prop=instrs_per_pass=500 \
  -psetname=rendermain0 \
  rendermain0/rendermain0 \
  -cmpd=soc \
  -simextp=name=ssramInst_mmap_bank1_ram02.target,in=0x00e00000:0x01ffffff \
  -simextp=name=ssramInst2.target,in=0x02000000:0x02000fff \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -on=stop_all

# Setup for process "rendermain1"
scac \
  -prop=ident=0 \
  -nogoifmain \
  -pset=4 \
  -prop=instrs_per_pass=500 \
  -psetname=rendermain1 \
  rendermain1/rendermain1 \
  -cmpd=soc \
  -simextp=name=ssramInst_mmap_bank1_ram03.target,in=0x00e00000:0x01ffffff \
  -simextp=name=ssramInst3.target,in=0x02000000:0x02000fff \
  -toggle=include_local_symbols=1 \
  -profile \
  -sim \
  -a7 \
  -Xmpy \
  -mmu \
  -toggle=deadbeef=1 \
  -on=stop_all

# Launch the debugger! 
scac \
  -multifiles=grmain,displaymain,rendermain0,rendermain1 \
  -cmpd=soc \
  -off=cr_for_more  \
  -simexts=bridge,instance=ssramInst_mmap_bank1_ram01 \
  -simexts=bridge,instance=ssramInst1 \
  -simexts=bridge,instance=ssramInst_mmap_bank1_ram02 \
  -simexts=bridge,instance=ssramInst2 \
  -simexts=bridge,instance=ssramInst_mmap_bank1_ram03 \
  -simexts=bridge,instance=ssramInst3 \
  -OKN $@

