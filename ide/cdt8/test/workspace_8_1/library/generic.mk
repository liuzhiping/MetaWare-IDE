## *MAKEFILE***********************************************************
## ********************************************************************
##
## Copyright (c) 1989-2005 ARC International
## All rights reserved
##
## This software embodies materials and concepts which are
## confidential to ARC International and is made
## available solely pursuant to the terms of a written license
## agreement with ARC International
##
## File: generic.mk
##
## Comments:
##    This file defines various variables required to build 
##    MQX generic kernel
##
## ********************************************************************
## *END****************************************************************

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

dupdirs += include io io$(PS)serial io$(PS)pcb io$(PS)pcb$(PS)mqxa io$(PS)pcb$(PS)shmem

dups    += mqx23x.h  kernel.h   target.h   errno.h    event.h    fio.h
dups    += gen_rev.h io.h       io_pcb.h   pcb_mqxa.h ipc.h      ipc_pcb.h
dups    += klog.h    log.h      message.h  mqx.h      mqx_cnfg.h mqx_ioc.h    
dups    += mutex.h   name.h     part.h     pcb.h      sem.h      serial.h
dups    += timer.h   watchdog.h eds.h      posix.h
dups    += lwevent.h lwlog.h    lwmem.h    lwtimer.h
dups    += mqx_str.h
# Start CR 461
dups    += pcb_shm.h
# End CR 461
dups    += rterrchk.h
dups    += lwmsgq.h

srcdirs += eds
srcs    += \
   eds_init

#srcdirs += edserial
#srcs    += \
#   edserial

srcdirs += event
srcs    += \
   ev_all   ev_allf  ev_allt  ev_allu  ev_any   ev_anyf  ev_anyt  ev_anyu  \
   ev_clear ev_close ev_comp  ev_creaa ev_creai ev_creat ev_dest  ev_fcrt  \
   ev_fcrta ev_fcrti ev_fdest ev_fopn  ev_open  ev_set   ev_tdel  ev_test  \
   ev_util  ev_waiti

srcdirs += fio
srcs    += \
   io_clre  io_dopr  io_fclos io_feof  io_ferr  io_fflsh io_fgetc io_fgetl \
   io_fgets io_fopen io_fp    io_fprt  io_fputc io_fputs io_fseek io_fscan \
   io_fstat io_ftell io_fung  io_ioctl io_misc  io_pntf  io_read  io_scanf \
   io_scanl io_spr   io_sscan io_vpr   io_write 

srcdirs += io
srcs    += \
   io_ghand io_init io_inst io_instx io_shand io_uinst

srcdirs += io$(PS)pcb
srcs    += \
   iop_inst io_pcb io_pcb2

srcdirs += io$(PS)pcb$(PS)mqxa
srcs    += \
   pcb_mqxa

# Start CR 461
srcdirs += io$(PS)pcb$(PS)shmem
srcs    += \
   pcb_shm
# End CR 461

srcdirs += io$(PS)serial
srcs    += \
   io_sinit

srcdirs += ipc
srcs    += \
   ipc_add ipc_adio ipc_evnt ipc_pcb ipc_rout ipc_rtt ipc_task 

srcdirs += kernel
srcs    += \
   idletask int_dep  int_disa int_ena  int_gdat int_gdef int_geh  int_gisr \
   int_idef int_iisr int_init int_isr  int_iunx int_ixcp int_sdat int_seh  \
   lws_crea lws_dest lws_test lws_wait lws_poll lws_post lws_wain lws_wati \
   lws_wafo lws_waun \
 \
   mem_alli mem_allo mem_allp mem_allz mem_alzp mem_crei mem_crep mem_extd \
   mem_exti mem_extp mem_fprt mem_free mem_frei mem_gsyp mem_gsys mem_init \
   mem_list mem_size mem_swap mem_tesa mem_tesp mem_test mem_util mem_xfer \
   mem_xfri mem_xftd mem_zsyf mem_zsys mem_swapn mem_vrfy \
 \
   monitor  mqx      mqx_dat  \
   mqx_fatl mqx_gcnt mqx_gcpu mqx_gtad mqx_gini mqx_gxit mqx_ioc  mqxiinit \
   mqx_scpu mqx_stad mqx_sxit mqxdebug \
 \
   sc_bost  sc_gprio sc_grr   sc_grrt  sc_ipq   sc_obsel sc_numq  \
   sc_spol  sc_sprio sc_srr   sc_srri  sc_srrt  sc_yield \
 \
   ta_abort ta_build ta_creab ta_creat ta_dest  ta_env   ta_exit  ta_fp    \
   ta_getx  ta_gexit ta_init  ta_param ta_prem  ta_prio  ta_rdy   ta_rest  \
   ta_setx  ta_sexit ta_sync  ta_util  ta_util2 \
 \
   td_alloc td_gerr  td_get td_gid  td_pid td_serr td_serrt td_sysid td_util \
 \
   ti_aday  ti_ahor  ti_amin  ti_amsec ti_ansec ti_apsec ti_asec  ti_ausec \
   ti_delay ti_delfo ti_deli  ti_delti ti_delun ti_difda ti_diff  ti_difft \
   ti_difhr ti_difmn ti_difms ti_difns ti_difps ti_difse ti_difus ti_dque  \
   ti_dquet ti_from  ti_elaps ti_elapt ti_get   ti_gethw ti_getp  ti_getr  \
   ti_gett  ti_ghwtt ti_init  ti_krnl  ti_kset  ti_ksett ti_leap  ti_nxd   \
   ti_rom   ti_setr  ti_sett  ti_setv  ti_shwtf ti_shwtt ti_ti2xd ti_to    \
   ti_xd2ti ti_xrom  titotks  tkstoti \
 \
   tq_creat tq_dest  tq_func  tq_test tq_tsusp tq_util

# Start CR 897
srcs    += ta_creas
# End   CR 897

# Start CR 1124
srcs    +=  tls_gref ta_rtos  ta_gtos
# End   CR 1124

srcdirs += klog
srcs    += \
   kl_cntrl kl_creat kl_creaa kl_disp  kl_dispi  kl_log   kl_stack kl_stsh  \
   kl_task 

srcdirs += log
srcs    += \
   lo_comp  lo_dest  lo_enabl lo_open lo_read  lo_reset lo_test  lo_write

srcdirs += lwevent
srcs    += \
   lwe_clr  lwe_crea lwe_dest lwe_set  lwe_test lwe_waif lwe_waii  lwe_waiu \
   lwe_watt

srcdirs += lwlog
srcs    += \
   lwl_comp lwl_crea lwl_crei lwl_cret lwl_dest lwl_ena  lwl_read  lwl_rst \
   lwl_size lwl_test lwl_wrii lwl_writ

srcdirs += lwmem
srcs    += \
   lwm_alli lwm_allo lwm_allp lwm_allz lwm_alzp lwm_crep lwm_free \
   lwm_gsyp lwm_gsys lwm_init lwm_list lwm_setd lwm_size lwm_test \
   lwm_xfer lwm_xfri lwm_xftd lwm_zsyf lwm_zsys

srcdirs += lwmsg
srcs    += lwmsgq

srcdirs += lwtimer
srcs    += \
   lwt_cncl lwt_cncp lwt_test lwtimer

srcdirs += message
srcs    += \
   ms_alloc ms_broad ms_comp  ms_close ms_count ms_dpool ms_free  ms_id    \
   ms_note  ms_open  ms_openi ms_own   ms_peek  ms_poll  ms_pool  ms_poola \
   ms_pooli ms_recv  ms_recvf ms_recvi ms_recvt ms_recvu ms_send  ms_sendb \
   ms_sendi ms_sendp ms_sendq ms_sendu ms_sopen ms_spool ms_swap  ms_tdel  \
   ms_testp ms_testq

srcdirs += mutex
srcs    += \
   mu_atini mu_comp  mu_dest  mu_gpol mu_gprio mu_gprot mu_gspin mu_init \
   mu_lock  mu_pol   mu_prio  mu_prot mu_spin  mu_tdel  mu_test  mu_tlock \
   mu_ulock mu_util

srcdirs += name
srcs    += \
   na_abi   na_add   na_addi  na_close na_comp  na_crth  na_dbi   na_del   \
   na_deli  na_fbi   na_find  na_findi na_findn na_fni   na_test  na_testi

srcdirs += part
srcs    += \
   pa_alloc pa_alloi pa_avail pa_comp  pa_creaa pa_creai pa_creat pa_dest  \
   pa_extnd pa_extni pa_free  pa_sallo pa_sallz pa_tdel  pa_test  pa_tx    \
   pa_util  pa_zallo

srcdirs += queue
srcs    += \
   qu_deq   qu_enq   qu_head  qu_init qu_insrt qu_next  qu_test  qu_unlnk \
   qu_util

srcdirs += sem
srcs    += \
   se_close se_comp  se_creat se_dest  se_fcrt  se_fdest se_fopn  se_iprio \
   se_open  se_post  se_tdel  se_test  se_util  se_wait  se_waitf se_waiti \
   se_waitt se_waitu
   
srcdirs += string
srcs    += \
   str_utos strnlen

srcdirs += timer
srcs    += \
   ti_cancl ti_comp  ti_s1afi ti_s1aft ti_s1ati ti_s1att  ti_spati  ti_spatt \
   ti_spevi ti_spevt ti_st1af ti_st1at ti_stpat ti_stpev  ti_task   ti_tdel  \
   ti_test

srcdirs += watchdog
srcs    += \
   wa_comp  wa_start  wa_stop  wa_strti  wa_strtt wa_tdel   wa_test

# EOF
