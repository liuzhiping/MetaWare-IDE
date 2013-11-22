
/**************************************************************************
 *
 * Generated by mcsg
 * Version 1.0.0
 * Build trunk
 * Fri Jul 18 16:04:30 PDT 2008
 * ARC International (c) 2008
 * This is a generated file. All manual edits will be lost.
 *
 **************************************************************************/


#include "vr_application_include.h"
VR_CONST char * vr_code_generation_time = "Fri Jul 18 16:04:30 PDT 2008";
extern void _grmain(void);

VR_CONST unsigned int vr_n_rtctxs = 1;
VrScheduler * vr_rtctxs [] = {0, 0, 0, 0, 0, 0};

volatile unsigned long vr_barrier[4] __attribute__ ((section(".zbarrier"), aligned(8)));


VR_CONST int vr_n_process_less_threads = 0;

int
vr_get_thread_id_for_sequential_number (int seq)
{
  if (seq == 0)
    return swarch_thd_grmain_thread_id;

  return -1;
}

int
vr_get_id_for_thread_name (char *thread_name)
{
  if (!strcmp ("thd_grmain", thread_name))
    return swarch_thd_grmain_thread_id;

  return -1;
}

char *
vr_get_thread_name_for_id (vr_thread_t thread_id)
{
  switch (thread_id)
    {
    case swarch_thd_grmain_thread_id:
      return "thd_grmain";

    case VR_ANY_THREAD_ID:
      return "ANY THREAD";

    }
  return 0;
}

int
vr_call_thread_entry_point (vr_thread_t thread_id)
{
  switch (thread_id)
    {
    case swarch_thd_grmain_thread_id:
      /* thd_grmain */
      _grmain();
      return 0;

    }
  vr_print_error("%s, %d: invalid thread id.\n ", __FILE__, __LINE__);
  vr_thread_shutdown();
  return -1;

}

int
vr_app_init (void)
{

  if (!strcmp (vr_get_thread_name (), "thd_grmain"))
    {
      return 0;
    }

  return -1;
}

unsigned int ticks_per_second = 98566144;
void *
vr_unassigned_thread_fn (void * arg)
{
  while (1);
  return 0;
}

char *
vr_get_heap_start (int i)
{
  vr_int_t result = 0;
  switch (i)
    {
    default:
      break;
    }
  return (char *) result;
}

char *
vr_get_heap_end (int i)
{
  vr_int_t result = 0;
  switch (i)
    {
    default:
      break;
    }
  return (char *) result;
}



/*returns 0 on success and -1 on failure*/
int vr_early_system_init  (void)
{
  /*ARC: getCpuEarlyInit() */

  return 0;
}



int
vr_system_init (void)
{
  /*ARC: getCpuInit() */
  /* grmain is the master process so proceed to do common initialization */

  return 0;
}


int vr_system_start (void)
{
  /*ARC: getCpuStartCommon() */
  int i;
  int pcount = 4;
  /* for each process, lift the barrier so they can proceed */
  for (i=0;i<pcount;i++) {
    if (i==0) continue;
    while (vr_nc_load((void *)(&vr_barrier[i])) != i) { (void)vr_sleep(0, 10); }
    vr_nc_store((void *)(&vr_barrier[i]), 0xffffffff);
  }

  return 0;
}


int vr_system_stop (void)
{
  /*ARC: getCpuStop() */

  return 0;
}

int vr_app_init_components(void)
{
  return 0;
}

#ifdef VR_MAIN

int
main (int argc, char **argv)
{
  char * default_arguments[] = {"./grmain", "thread", "0.0", "vr-svr", "thd_grmain", "--no-malloc-fallback" };
  int default_argument_count = 6;

  vr_set_argc (argc);
  vr_set_argv (argv);

  return vr_main (default_argument_count, default_arguments);
}

#endif /* VR_MAIN */

