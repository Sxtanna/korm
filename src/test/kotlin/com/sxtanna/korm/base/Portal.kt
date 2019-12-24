package com.sxtanna.korm.base

import java.util.UUID

data class Portal(val name: String, val args: String, val uuid: UUID, val type: Type, val vecs: Set<Vec>)
{
	
	@delegate:Transient
	val delegate by lazy {
		"Hello"
	}
	
	enum class Type
	{
		
		UNKNOWN
	}
	
	/*private companion object : KormPuller<Portal>
	{
		
		override fun pull(reader: ReaderContext, types: MutableList<KormType>): Portal?
		{
			val name = types.byName("name")
			val args = types.byName("args")
			val uuid = types.byName("uuid")
			val type = types.byName("type")
			val vecs = types.byName("vecs")
			
			
			return null
		}
		
	}*/
	
}