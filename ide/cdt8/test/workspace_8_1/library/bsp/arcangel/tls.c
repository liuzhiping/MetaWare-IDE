/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: met.c
***
*** Comments:
***   This file contains the source functions for runtime support
*** for the Metaware Compiler.  Thread local storage is needed if
*** you use C++ exceptions.  When NOT using C++ exceptions it is
*** VERY unlikely you'll include any of these functions from the
*** BSP library.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"

/*
 * Runtime routines to execute constructors and
 * destructors for task local storage.
 */
extern void __mw_run_tls_dtor();
extern void __mw_run_tls_ctor();

/*
 * Linker generated symbols to mark .tls section addresses
 * first byte .. last byte
 */
extern char _ftls[], _etls[];
#pragma weak _ftls
#pragma weak _etls

static int tls_space_offset;
static __declspec(thread) void (*old_exit_handler)(void);

static void
reserve_tls_foreach_task() {
    unsigned len = (unsigned)(_etls - _ftls);
    tls_space_offset = _task_reserve_space(len);
    }
// Very high priority to reserve space early
#pragma startup_expr(reserve_tls_foreach_task(), 240);

static _Inline void
initialize_tls(void *tls) {
    unsigned len = (unsigned)(_etls - _ftls);
    _mem_copy(tls, _ftls, len);
    }

static void
destroy_tls(void) {
    char **tls_field_in_td;
    if (old_exit_handler != 0) (*old_exit_handler)();
    tls_field_in_td = _crt_tls_reference();
    if (tls_field_in_td != 0) { // otherwise an isr
        if (*tls_field_in_td != 0) { // otherwise never used
            __mw_run_tls_dtor();
            *tls_field_in_td = 0;
            }
        }
    }

static void*
get_isr_tls(void) {
    // In an ISR
    static int first = 1;
    if (_Rarely(first)) {
        first = 0;
        __mw_run_tls_ctor();    // Run constructors
        }
    return _ftls;
    }
#pragma off_inline(get_isr_tls)

static void*
init_task_tls(char **tls_field_in_td) {
    _task_id self;
    char *tls = _task_get_reserved_base();
    if (tls == 0) {
        // Nothing was reserved!  Why are we being called?
        return _ftls;
        }
    tls += tls_space_offset;
    *tls_field_in_td = tls;
    initialize_tls(tls);
    __mw_run_tls_ctor();        // Run constructors
    self = _task_get_id();
    old_exit_handler = _task_get_exit_handler(self);
    _task_set_exit_handler(self, &destroy_tls);
    return tls;
    }
#pragma off_inline(init_task_tls)

/*
 * Back end gens calls to find local data for this task
 */
void *_mwget_tls(void) {
    char **tls_field_in_td = _crt_tls_reference();
    if (_Rarely(tls_field_in_td == 0)) return get_isr_tls();

    // We're counting on MQX to zero the pointer when a task is created.
    if (_Rarely(*tls_field_in_td == 0)) return init_task_tls(tls_field_in_td);

    return *tls_field_in_td;
    }

/* EOF */
