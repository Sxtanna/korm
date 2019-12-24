package com.sxtanna.korm.writer.base

enum class Options
{
	
	/**
	 * Will write a trailing comma after list and hash entries written on new lines
	 *
	 * **Requires**
	 *  - [LIST_ENTRY_ON_NEW_LINE] for lists
	 *  - [HASH_ENTRY_ON_NEW_LINE] for hashs
	 *
	 * @sample [trailingCommasSample]
	 */
	TRAILING_COMMAS,
	/**
	 * Will write a space after writing the assign symbol **:**
	 *
	 * @sample [spaceAfterAssignSample]
	 */
	SPACE_AFTER_ASSIGN,
	/**
	 * Will write a comma after each entry in a hash *(how a list behaves)*
	 *
	 * @sample [commaAfterHashEntrySample]
	 */
	COMMA_AFTER_HASH_ENTRY,
	/**
	 * Will write hash entries on new lines
	 *
	 * @sample [hashEntryOnNewLineSample]
	 */
	HASH_ENTRY_ON_NEW_LINE,
	/**
	 * Will write the entries of a complex key on new lines
	 *
	 * @sample [complexKeyEntryOnNewLineSample]
	 */
	COMPLEX_KEY_ENTRY_ON_NEW_LINE,
	/**
	 * Will write all list entries on new lines
	 *
	 * @sample [listEntryOnNewLineSample]
	 */
	LIST_ENTRY_ON_NEW_LINE,
	/**
	 * Will write complex list entries on new lines
	 *
	 * @sample [complexListEntryOnNewLine]
	 */
	COMPLEX_LIST_ENTRY_ON_NEW_LINE,
	/**
	 * Will write the comments found from [KormComment] annotations
	 */
	INCLUDE_COMMENTS;
	
	
	companion object
	{
		
		@JvmStatic
		fun of(vararg options: Options) = WriterOptions(setOf(*options))
		
		
		@JvmStatic
		fun none() = of()
		
		@JvmStatic
		fun max() = of(*values())
		
		@JvmStatic
		fun min(vararg options: Options) = of(SPACE_AFTER_ASSIGN, HASH_ENTRY_ON_NEW_LINE, COMPLEX_LIST_ENTRY_ON_NEW_LINE, INCLUDE_COMMENTS, *options)
		
	}
	
	
	@Suppress("UNUSED_EXPRESSION")
	private fun trailingCommasSample()
	{
		// For lists
		"""
            [
              1,
              2
            ]
        """
		// becomes
		"""
            [
              1,
              2,
            ]
        """
		
		
		// For hashs
		"""
            {
              1: 'A',
              2: 'B'
            }
        """
		// becomes
		"""
            {
              1: 'A',
              2: 'B',
            }
        """
	}
	
	@Suppress("UNUSED_EXPRESSION")
	private fun spaceAfterAssignSample()
	{
		"""
            name:"value"
        """
		// becomes
		"""
            name: "value"
        """
	}
	
	@Suppress("UNUSED_EXPRESSION")
	private fun commaAfterHashEntrySample()
	{
		"""
            {
              1: 'A'
              2: 'B'
              3: 'C'
            }
        """
		// becomes
		"""
            {
              1: 'A',
              2: 'B',
              3: 'C'
            }
        """
	}
	
	@Suppress("UNUSED_EXPRESSION")
	private fun hashEntryOnNewLineSample()
	{
		"""
            { 1:'A' 2:'B' 3:'C' }
        """
		// becomes
		"""
            {
              1:'A'
              2:'B'
              3:'C'
            }
        """
	}
	
	@Suppress("UNUSED_EXPRESSION")
	private fun listEntryOnNewLineSample()
	{
		"""
            [1, 2, 3, 4, 5]
        """
		// becomes
		"""
            [
              1,
              2,
              3,
              4,
              5
            ]
        """
	}
	
	@Suppress("UNUSED_EXPRESSION")
	private fun complexKeyEntryOnNewLineSample()
	{
		"""
            { `{ first: 1 other: 2 }`:21 }
        """
		// becomes
		"""
            { `{
                first: 1
                other: 2
              }`:21 }
        """
	}
	
	@Suppress("UNUSED_EXPRESSION")
	private fun complexListEntryOnNewLine()
	{
		"""
            [{ numb: 1 }, { numb: 2 }, { numb: 3 }, { numb: 4 }, { numb: 5 }]
        """
		// becomes
		"""
            [
              { numb: 1 },
              { numb: 2 },
              { numb: 3 },
              { numb: 4 },
              { numb: 5 }
            ]
        """
	}
	
}