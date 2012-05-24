/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.dwarf2;

public class Dwarf2 implements DwarfConstants{
    /**
     * Return the name of a tag ID.
     * @param tagID the tag ID.
     * @return the name of a tag ID.
     */
    public static String getTagName(int tagID){
        switch (tagID){
            case DW_TAG_array_type: return "TAG_array_type";
            case DW_TAG_class_type: return "TAG_class_type";
            case DW_TAG_entry_point: return "TAG_entry_point";
            case DW_TAG_enumeration_type: return "TAG_enumeration_type";
            case DW_TAG_formal_parameter: return "TAG_formal_parameter";
            case DW_TAG_imported_declaration: return "TAG_imported_declaration";
            case DW_TAG_label: return "TAG_label";
            case DW_TAG_lexical_block: return "TAG_lexical_block";
            case DW_TAG_member: return "TAG_member";
            case DW_TAG_pointer_type: return "TAG_pointer_type";
            case DW_TAG_reference_type: return "TAG_reference_type";
            case DW_TAG_compile_unit: return "TAG_compile_unit";
            case DW_TAG_string_type: return "TAG_string_type";
            case DW_TAG_structure_type: return "TAG_structure_type";
            case DW_TAG_subroutine_type: return "TAG_subroutine_type";
            case DW_TAG_typedef: return "TAG_typedef";
            case DW_TAG_union_type: return "TAG_union_type";
            case DW_TAG_unspecified_parameters: return "TAG_unspecified_parameters";
            case DW_TAG_variant: return "TAG_variant";
            case DW_TAG_common_block: return "TAG_common_block";
            case DW_TAG_common_inclusion: return "TAG_common_inclusion";
            case DW_TAG_inheritance: return "TAG_inheritance";
            case DW_TAG_inlined_subroutine: return "TAG_inlined_subroutine";
            case DW_TAG_module: return "TAG_module";
            case DW_TAG_ptr_to_member_type: return "TAG_ptr_to_member_type";
            case DW_TAG_set_type: return "TAG_set_type";
            case DW_TAG_subrange_type: return "TAG_subrange_type";
            case DW_TAG_with_stmt: return "TAG_with_stmt";
            case DW_TAG_access_declaration: return "TAG_access_declaration";
            case DW_TAG_base_type: return "TAG_base_type";
            case DW_TAG_catch_block: return "TAG_catch_block";
            case DW_TAG_const_type: return "TAG_const_type";
            case DW_TAG_constant: return "TAG_constant";
            case DW_TAG_enumerator: return "TAG_enumerator";
            case DW_TAG_file_type: return "TAG_file_type";
            case DW_TAG_friend: return "TAG_friend";
            case DW_TAG_namelist: return "TAG_namelist";
            case DW_TAG_namelist_item: return "TAG_namelist_item";
            case DW_TAG_packed_type: return "TAG_packed_type";
            case DW_TAG_subprogram: return "TAG_subprogram";
            case DW_TAG_template_type_param: return "TAG_template_type_param";
            case DW_TAG_template_value_param: return "TAG_template_value_param";
            case DW_TAG_thrown_type: return "TAG_thrown_type";
            case DW_TAG_try_block: return "TAG_try_block";
            case DW_TAG_variant_part: return "TAG_variant_part";
            case DW_TAG_variable: return "TAG_variable";
            case DW_TAG_volatile_type: return "TAG_volatile_type";
            case DW_TAG_lo_user: return "TAG_lo_user";
            case DW_TAG_MIPS_loop: return "TAG_MIPS_loop";
            case DW_TAG_format_label: return "TAG_format_label";
            case DW_TAG_function_template: return "TAG_function_template";
            case DW_TAG_class_template: return "TAG_class_template";
            case DW_TAG_hi_user: return "TAG_hi_user";
            default: return "0x" + Integer.toHexString(tagID);
        }
    }
    
    public static String getFormName(int formID){
        switch (formID){
            case DW_FORM_addr: return "FORM_addr";
            case DW_FORM_block2: return "FORM_block2";
            case DW_FORM_block4: return "FORM_block4";
            case DW_FORM_data2: return "FORM_data2";
            case DW_FORM_data4: return "FORM_data4";
            case DW_FORM_data8: return "FORM_data8";
            case DW_FORM_string: return "FORM_string";
            case DW_FORM_block: return "FORM_block";
            case DW_FORM_block1: return "FORM_block1";
            case DW_FORM_data1: return "FORM_data1";
            case DW_FORM_flag: return "FORM_flag";
            case DW_FORM_sdata: return "FORM_sdata";
            case DW_FORM_strp: return "FORM_strp";
            case DW_FORM_udata: return "FORM_udata";
            case DW_FORM_ref_addr: return "FORM_ref_addr";
            case DW_FORM_ref1: return "FORM_ref1";
            case DW_FORM_ref2: return "FORM_ref2";
            case DW_FORM_ref4: return "FORM_ref4";
            case DW_FORM_ref8: return "FORM_ref8";
            case DW_FORM_ref_udata: return "FORM_ref_udata";
            case DW_FORM_indirect: return "FORM_indirect";
	    default: return "0x" + Integer.toHexString(formID);
        }
    }
    
    public static String getAttributeName(int atID){
        switch(atID){
            case DW_AT_sibling: return "AT_sibling";
            case DW_AT_location: return "AT_location";
            case DW_AT_name: return "AT_name";
            case DW_AT_ordering: return "AT_ordering";
            case DW_AT_subscr_data: return "AT_subscr_data";
            case DW_AT_byte_size: return "AT_byte_size";
            case DW_AT_bit_offset: return "AT_bit_offset";
            case DW_AT_bit_size: return "AT_bit_size";
            case DW_AT_element_list: return "AT_element_list";
            case DW_AT_stmt_list: return "AT_stmt_list";
            case DW_AT_low_pc: return "AT_low_pc";
            case DW_AT_high_pc: return "AT_high_pc";
            case DW_AT_language: return "AT_language";
            case DW_AT_member: return "AT_member";
            case DW_AT_discr: return "AT_discr";
            case DW_AT_discr_value: return "AT_discr_value";
            case DW_AT_visibility: return "AT_visibility";
            case DW_AT_import: return "AT_import";
            case DW_AT_string_length: return "AT_string_length";
            case DW_AT_common_reference: return "AT_common_reference";
            case DW_AT_comp_dir: return "AT_comp_dir";
            case DW_AT_const_value: return "AT_const_value";
            case DW_AT_containing_type: return "AT_containing_type";
            case DW_AT_default_value: return "AT_default_value";
            case DW_AT_inline: return "AT_inline";
            case DW_AT_is_optional: return "AT_is_optional";
            case DW_AT_lower_bound: return "AT_lower_bound";
            case DW_AT_producer: return "AT_producer";
            case DW_AT_prototyped: return "AT_prototyped";
            case DW_AT_return_addr: return "AT_return_addr";
            case DW_AT_start_scope: return "AT_start_scope";
            case DW_AT_stride_size: return "AT_stride_size";
            case DW_AT_upper_bound: return "AT_upper_bound";
            case DW_AT_abstract_origin: return "AT_abstract_origin";
            case DW_AT_accessibility: return "AT_accessibility";
            case DW_AT_address_class: return "AT_address_class";
            case DW_AT_artificial: return "AT_artificial";
            case DW_AT_base_types: return "AT_base_types";
            case DW_AT_calling_convention: return "AT_calling_convention";
            case DW_AT_count: return "AT_count";
            case DW_AT_data_member_location: return "AT_data_member_location";
            case DW_AT_decl_column: return "AT_decl_column";
            case DW_AT_decl_file: return "AT_decl_file";
            case DW_AT_decl_line: return "AT_decl_line";
            case DW_AT_declaration: return "AT_declaration";
            case DW_AT_discr_list: return "AT_discr_list";
            case DW_AT_encoding: return "AT_encoding";
            case DW_AT_external: return "AT_external";
            case DW_AT_frame_base: return "AT_frame_base";
            case DW_AT_friend: return "AT_friend";
            case DW_AT_identifier_case: return "AT_identifier_case";
            case DW_AT_macro_info: return "AT_macro_info";
            case DW_AT_namelist_items: return "AT_namelist_items";
            case DW_AT_priority: return "AT_priority";
            case DW_AT_segment: return "AT_segment";
            case DW_AT_specification: return "AT_specification";
            case DW_AT_static_link: return "AT_static_link";
            case DW_AT_type: return "AT_type";
            case DW_AT_use_location: return "AT_use_location";
            case DW_AT_variable_parameter: return "AT_variable_parameter";
            case DW_AT_virtuality: return "AT_virtuality";
            case DW_AT_vtable_elem_location: return "AT_vtable_elem_location";
            case DW_AT_lo_user: return "AT_lo_user";
            case DW_AT_MIPS_fde: return "AT_MIPS_fde";
            case DW_AT_MIPS_loop_begin: return "AT_MIPS_loop_begin";
            case DW_AT_MIPS_tail_loop_begin: return "AT_MIPS_tail_loop_begin";
            case DW_AT_MIPS_epilog_begin: return "AT_MIPS_epilog_begin";
            case DW_AT_MIPS_loop_unroll_factor: return "AT_MIPS_loop_unroll_factor";
            case DW_AT_MIPS_software_pipeline_depth: return "AT_MIPS_software_pipeline_depth";
            case DW_AT_MIPS_linkage_name: return "AT_MIPS_linkage_name";
            case DW_AT_MIPS_stride: return "AT_MIPS_stride";
            case DW_AT_MIPS_abstract_name: return "AT_MIPS_abstract_name";
            case DW_AT_MIPS_clone_origin: return "AT_MIPS_clone_origin";
            case DW_AT_MIPS_has_inlines: return "AT_MIPS_has_inlines";
            case DW_AT_MIPS_stride_byte: return "AT_MIPS_stride_byte";
            case DW_AT_MIPS_stride_elem: return "AT_MIPS_stride_elem";
            case DW_AT_MIPS_ptr_dopetype: return "AT_MIPS_ptr_dopetype";
            case DW_AT_MIPS_allocatable_dopetype: return "AT_MIPS_allocatable_dopetype";
            case DW_AT_MIPS_assumed_shape_dopetype: return "AT_MIPS_assumed_shape_dopetype";
            case DW_AT_MIPS_assumed_size: return "AT_MIPS_assumed_size";
            case DW_AT_sf_names: return "AT_sf_names";
            case DW_AT_src_info: return "AT_src_info";
            case DW_AT_mac_info: return "AT_mac_info";
            case DW_AT_src_coords: return "AT_src_coords";
            case DW_AT_body_begin: return "AT_body_begin";
            case DW_AT_body_end: return "AT_body_end";
            case DW_AT_hi_user: return "AT_hi_user";
        default: return "0x" + Integer.toHexString(atID);
        }
    }
    
    public static String getOpcodeName(int op){
        switch(op) {
            case DW_OP_addr: return "DW_OP_addr"; /* Constant address. */
            case DW_OP_deref: return "DW_OP_deref";
            case DW_OP_const1u: return "DW_OP_const1u"; /* Unsigned 1-byte constant. */
            case DW_OP_const1s: return "DW_OP_const1s"; /* Signed 1-byte constant. */
            case DW_OP_const2u: return "DW_OP_const2u"; /* Unsigned 2-byte constant. */
            case DW_OP_const2s: return "DW_OP_const2s"; /* Signed 2-byte constant. */
            case DW_OP_const4u: return "DW_OP_const4u"; /* Unsigned 4-byte constant. */
            case DW_OP_const4s: return "DW_OP_const4s"; /* Signed 4-byte constant. */
            case DW_OP_const8u: return "DW_OP_const8u"; /* Unsigned 8-byte constant. */
            case DW_OP_const8s: return "DW_OP_const8s"; /* Signed 8-byte constant. */
            case DW_OP_constu: return "DW_OP_constu"; /* Unsigned LEB128 constant. */
            case DW_OP_consts: return "DW_OP_consts"; /* Signed LEB128 constant. */
            case DW_OP_dup: return "DW_OP_dup";
            case DW_OP_drop: return "DW_OP_drop";
            case DW_OP_over: return "DW_OP_over";
            case DW_OP_pick: return "DW_OP_pick"; /* 1-byte stack index. */
            case DW_OP_swap: return "DW_OP_swap";
            case DW_OP_rot: return "DW_OP_rot";
            case DW_OP_xderef: return "DW_OP_xderef";
            case DW_OP_abs: return "DW_OP_abs";
            case DW_OP_and: return "DW_OP_and";
            case DW_OP_div: return "DW_OP_div";
            case DW_OP_minus: return "DW_OP_minus";
            case DW_OP_mod: return "DW_OP_mod";
            case DW_OP_mul: return "DW_OP_mul";
            case DW_OP_neg: return "DW_OP_neg";
            case DW_OP_not: return "DW_OP_not";
            case DW_OP_or: return "DW_OP_or";
            case DW_OP_plus: return "DW_OP_plus";
            case DW_OP_plus_uconst: return "DW_OP_plus_uconst"; /* Unsigned LEB128 addend. */
            case DW_OP_shl: return "DW_OP_shl";
            case DW_OP_shr: return "DW_OP_shr";
            case DW_OP_shra: return "DW_OP_shra";
            case DW_OP_xor: return "DW_OP_xor";
            case DW_OP_bra: return "DW_OP_bra"; /* Signed 2-byte constant. */
            case DW_OP_eq: return "DW_OP_eq";
            case DW_OP_ge: return "DW_OP_ge";
            case DW_OP_gt: return "DW_OP_gt";
            case DW_OP_le: return "DW_OP_le";
            case DW_OP_lt: return "DW_OP_lt";
            case DW_OP_ne: return "DW_OP_ne";
            case DW_OP_skip: return "DW_OP_skip"; /* Signed 2-byte constant. */
            case DW_OP_lit0: return "DW_OP_lit0"; /* Literal 0. */
            case DW_OP_lit1: return "DW_OP_lit1"; /* Literal 1. */
            case DW_OP_lit2: return "DW_OP_lit2"; /* Literal 2. */
            case DW_OP_lit3: return "DW_OP_lit3"; /* Literal 3. */
            case DW_OP_lit4: return "DW_OP_lit4"; /* Literal 4. */
            case DW_OP_lit5: return "DW_OP_lit5"; /* Literal 5. */
            case DW_OP_lit6: return "DW_OP_lit6"; /* Literal 6. */
            case DW_OP_lit7: return "DW_OP_lit7"; /* Literal 7. */
            case DW_OP_lit8: return "DW_OP_lit8"; /* Literal 8. */
            case DW_OP_lit9: return "DW_OP_lit9"; /* Literal 9. */
            case DW_OP_lit10: return "DW_OP_lit10"; /* Literal 10. */
            case DW_OP_lit11: return "DW_OP_lit11"; /* Literal 11. */
            case DW_OP_lit12: return "DW_OP_lit12"; /* Literal 12. */
            case DW_OP_lit13: return "DW_OP_lit13"; /* Literal 13. */
            case DW_OP_lit14: return "DW_OP_lit14"; /* Literal 14. */
            case DW_OP_lit15: return "DW_OP_lit15"; /* Literal 15. */
            case DW_OP_lit16: return "DW_OP_lit16"; /* Literal 16. */
            case DW_OP_lit17: return "DW_OP_lit17"; /* Literal 17. */
            case DW_OP_lit18: return "DW_OP_lit18"; /* Literal 18. */
            case DW_OP_lit19: return "DW_OP_lit19"; /* Literal 19. */
            case DW_OP_lit20: return "DW_OP_lit20"; /* Literal 20. */
            case DW_OP_lit21: return "DW_OP_lit21"; /* Literal 21. */
            case DW_OP_lit22: return "DW_OP_lit22"; /* Literal 22. */
            case DW_OP_lit23: return "DW_OP_lit23"; /* Literal 23. */
            case DW_OP_lit24: return "DW_OP_lit24"; /* Literal 24. */
            case DW_OP_lit25: return "DW_OP_lit25"; /* Literal 25. */
            case DW_OP_lit26: return "DW_OP_lit26"; /* Literal 26. */
            case DW_OP_lit27: return "DW_OP_lit27"; /* Literal 27. */
            case DW_OP_lit28: return "DW_OP_lit28"; /* Literal 28. */
            case DW_OP_lit29: return "DW_OP_lit29"; /* Literal 29. */
            case DW_OP_lit30: return "DW_OP_lit30"; /* Literal 30. */
            case DW_OP_lit31: return "DW_OP_lit31"; /* Literal 31. */
            case DW_OP_reg0: return "DW_OP_reg0"; /* Register 0. */
            case DW_OP_reg1: return "DW_OP_reg1"; /* Register 1. */
            case DW_OP_reg2: return "DW_OP_reg2"; /* Register 2. */
            case DW_OP_reg3: return "DW_OP_reg3"; /* Register 3. */
            case DW_OP_reg4: return "DW_OP_reg4"; /* Register 4. */
            case DW_OP_reg5: return "DW_OP_reg5"; /* Register 5. */
            case DW_OP_reg6: return "DW_OP_reg6"; /* Register 6. */
            case DW_OP_reg7: return "DW_OP_reg7"; /* Register 7. */
            case DW_OP_reg8: return "DW_OP_reg8"; /* Register 8. */
            case DW_OP_reg9: return "DW_OP_reg9"; /* Register 9. */
            case DW_OP_reg10: return "DW_OP_reg10"; /* Register 10. */
            case DW_OP_reg11: return "DW_OP_reg11"; /* Register 11. */
            case DW_OP_reg12: return "DW_OP_reg12"; /* Register 12. */
            case DW_OP_reg13: return "DW_OP_reg13"; /* Register 13. */
            case DW_OP_reg14: return "DW_OP_reg14"; /* Register 14. */
            case DW_OP_reg15: return "DW_OP_reg15"; /* Register 15. */
            case DW_OP_reg16: return "DW_OP_reg16"; /* Register 16. */
            case DW_OP_reg17: return "DW_OP_reg17"; /* Register 17. */
            case DW_OP_reg18: return "DW_OP_reg18"; /* Register 18. */
            case DW_OP_reg19: return "DW_OP_reg19"; /* Register 19. */
            case DW_OP_reg20: return "DW_OP_reg20"; /* Register 20. */
            case DW_OP_reg21: return "DW_OP_reg21"; /* Register 21. */
            case DW_OP_reg22: return "DW_OP_reg22"; /* Register 22. */
            case DW_OP_reg23: return "DW_OP_reg23"; /* Register 24. */
            case DW_OP_reg24: return "DW_OP_reg24"; /* Register 24. */
            case DW_OP_reg25: return "DW_OP_reg25"; /* Register 25. */
            case DW_OP_reg26: return "DW_OP_reg26"; /* Register 26. */
            case DW_OP_reg27: return "DW_OP_reg27"; /* Register 27. */
            case DW_OP_reg28: return "DW_OP_reg28"; /* Register 28. */
            case DW_OP_reg29: return "DW_OP_reg29"; /* Register 29. */
            case DW_OP_reg30: return "DW_OP_reg30"; /* Register 30. */
            case DW_OP_reg31: return "DW_OP_reg31"; /* Register 31. */
            case DW_OP_breg0: return "DW_OP_breg0"; /* Base register 0. */
            case DW_OP_breg1: return "DW_OP_breg1"; /* Base register 1. */
            case DW_OP_breg2: return "DW_OP_breg2"; /* Base register 2. */
            case DW_OP_breg3: return "DW_OP_breg3"; /* Base register 3. */
            case DW_OP_breg4: return "DW_OP_breg4"; /* Base register 4. */
            case DW_OP_breg5: return "DW_OP_breg5"; /* Base register 5. */
            case DW_OP_breg6: return "DW_OP_breg6"; /* Base register 6. */
            case DW_OP_breg7: return "DW_OP_breg7"; /* Base register 7. */
            case DW_OP_breg8: return "DW_OP_breg8"; /* Base register 8. */
            case DW_OP_breg9: return "DW_OP_breg9"; /* Base register 9. */
            case DW_OP_breg10: return "DW_OP_breg10"; /* Base register 10. */
            case DW_OP_breg11: return "DW_OP_breg11"; /* Base register 11. */
            case DW_OP_breg12: return "DW_OP_breg12"; /* Base register 12. */
            case DW_OP_breg13: return "DW_OP_breg13"; /* Base register 13. */
            case DW_OP_breg14: return "DW_OP_breg14"; /* Base register 14. */
            case DW_OP_breg15: return "DW_OP_breg15"; /* Base register 15. */
            case DW_OP_breg16: return "DW_OP_breg16"; /* Base register 16. */
            case DW_OP_breg17: return "DW_OP_breg17"; /* Base register 17. */
            case DW_OP_breg18: return "DW_OP_breg18"; /* Base register 18. */
            case DW_OP_breg19: return "DW_OP_breg19"; /* Base register 19. */
            case DW_OP_breg20: return "DW_OP_breg20"; /* Base register 20. */
            case DW_OP_breg21: return "DW_OP_breg21"; /* Base register 21. */
            case DW_OP_breg22: return "DW_OP_breg22"; /* Base register 22. */
            case DW_OP_breg23: return "DW_OP_breg23"; /* Base register 23. */
            case DW_OP_breg24: return "DW_OP_breg24"; /* Base register 24. */
            case DW_OP_breg25: return "DW_OP_breg25"; /* Base register 25. */
            case DW_OP_breg26: return "DW_OP_breg26"; /* Base register 26. */
            case DW_OP_breg27: return "DW_OP_breg27"; /* Base register 27. */
            case DW_OP_breg28: return "DW_OP_breg28"; /* Base register 28. */
            case DW_OP_breg29: return "DW_OP_breg29"; /* Base register 29. */
            case DW_OP_breg30: return "DW_OP_breg30"; /* Base register 30. */
            case DW_OP_breg31: return "DW_OP_breg31"; /* Base register 31. */
            case DW_OP_regx: return "DW_OP_regx"; /* Unsigned LEB128 register. */
            case DW_OP_fbreg: return "DW_OP_fbreg"; /* Signed LEB128 register. */
            case DW_OP_bregx: return "DW_OP_bregx"; /* ULEB128 register followed by SLEB128 off. */
            case DW_OP_piece: return "DW_OP_piece"; /* ULEB128 size of piece addressed. */
            case DW_OP_deref_size: return "DW_OP_deref_size"; /* 1-byte size of data retrieved. */
            case DW_OP_xderef_size: return "DW_OP_xderef_size"; /* 1-byte size of data retrieved. */
            case DW_OP_nop: return "DW_OP_nop";
            case DW_OP_push_object_address: return "DW_OP_push_object_address";
            case DW_OP_call2: return "DW_OP_call2";
            case DW_OP_call4: return "DW_OP_call4";
            case DW_OP_call_ref: return "DW_OP_call_ref";
            case DW_OP_form_tls_address: return "DW_OP_form_tls_address";
            default: return "DW_OP_???<0x" + Integer.toString(op,16) + ">";
        }
    }
    
    /**
     * Attribute formats.
     * @author davidp
     * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
     * @version $Revision$
     * @lastModified $Date$
     * @lastModifiedBy $Author$
     * @reviewed 0 $Revision:1$
     */
    public enum AttributeFormat {
        STRING_FORM,    // Attribute is a string
        INT_FORM,       // Attribute is an integer.
        REF_FORM,       // Address relative to start of compile unit within .debug_info section.
        BLOCK_FORM,     // Attribute is a block of bytes.
    }

}
