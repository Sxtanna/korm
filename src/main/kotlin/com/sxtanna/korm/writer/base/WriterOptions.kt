package com.sxtanna.korm.writer.base

class WriterOptions internal constructor(private val options: Set<Options>)
{
	
	/**
	 * @see [Options.TRAILING_COMMAS]
	 */
	val trailingCommas: Boolean
		get() = Options.TRAILING_COMMAS in options
	
	/**
	 * @see [Options.SPACE_AFTER_ASSIGN]
	 */
	val spaceAfterAssign: Boolean
		get() = Options.SPACE_AFTER_ASSIGN in options
	
	/**
	 * @see [Options.COMMA_AFTER_HASH_ENTRY]
	 */
	val commaAfterHashEntry: Boolean
		get() = Options.COMMA_AFTER_HASH_ENTRY in options
	
	/**
	 * @see [Options.HASH_ENTRY_ON_NEW_LINE]
	 */
	val hashEntryOnNewLine: Boolean
		get() = Options.HASH_ENTRY_ON_NEW_LINE in options
	
	/**
	 * @see [Options.COMPLEX_KEY_ENTRY_ON_NEW_LINE]
	 */
	val complexKeyEntryOnNewLine: Boolean
		get() = Options.COMPLEX_KEY_ENTRY_ON_NEW_LINE in options
	
	/**
	 * @see [Options.LIST_ENTRY_ON_NEW_LINE]
	 */
	val listEntryOnNewLine: Boolean
		get() = Options.LIST_ENTRY_ON_NEW_LINE in options
	
	/**
	 * @see [Options.COMPLEX_LIST_ENTRY_ON_NEW_LINE]
	 */
	val complexListEntryOnNewLine: Boolean
		get() = Options.COMPLEX_LIST_ENTRY_ON_NEW_LINE in options
	
}