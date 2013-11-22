#include "aom.h"

/*
 * this file is here only to demonstrate the AOM. It copies
 * the data from within the executable into the requested
 * memory region. These mock handlers are emulating the
 * real user-supplied routines (the mock handlers are
 * essentially a firewall between the overlay manager and
 * the code and data that it needs to access).
 *
 * See inc/aom.h for more details on these functions. The data
 * types are defined in inc/aom_types.h.
 */

/*
 * this structure is defined in aom/aom_g/arc_t/aom_handlers.s and
 * accesses the internal overlay data sections.
 */
struct metadata {
    unsigned char *foverlay;
    unsigned char *eoverlay;
    unsigned char *foffsetTable;
    unsigned char *eoffsetTable;
    };

extern struct metadata AOM_metadata;

void _Asm init_AOM_metadata() {
	.align	4
	b	.Lfuhler0
AOM_metadata::
	.long	_foverlay
	.long	_eoverlay
	.long	_foffsetTable
	.long	_eoffsetTable
.Lfuhler0:
    }

void only_here_to_make_AOM_metadata() {
    init_AOM_metadata();
    }

void _Save_all_regs user_write_backing_store(const package_t *request) {
    user_system_error(et_UNEXPECTED_ERROR);
    return;
    }

void _Save_all_regs user_read_backing_store(const package_t *request) {
    unsigned char *base = 0;
    int len = request->nblks * 512;
    switch (request->type) {
	case ps_offset_table_section: {
	    base = AOM_metadata.foffsetTable + request->off;
	    } break;
	case ps_overlay_section: {
    	    base = AOM_metadata.foverlay + request->off;
	    } break;
	}
    if (base && len) {
	unsigned char *addr = request->pc_addr;
	for (int i = 0; i < len; i++) {
	    addr[i] = base[i];
	    }
	return;
	}
    user_system_error(et_UNEXPECTED_ERROR);
    }

int _Never_returns user_system_error(event_t errornum) {
    /* halt the system - an unrecoverable error has occured */
    _brk();
    return 0;
    }

ulong_t _Save_all_regs user_package_section_size(int pkgnum,package_section_t type) {
    ulong_t bytes = 0;
    switch (type) {
	case ps_offset_table_section:
    	    return bytes = AOM_metadata.eoffsetTable - AOM_metadata.foffsetTable;
	case ps_overlay_section:
    	    return bytes = AOM_metadata.eoverlay - AOM_metadata.foverlay;
	}
    user_system_error(et_UNEXPECTED_ERROR);
    }

