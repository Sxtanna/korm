package com.sxtanna.korm.legacy.base

data class ArcadeConfig(val store: StoreConfig)
{
	
	constructor() : this(StoreConfig())
	
	
	data class StoreConfig(val type: Type, val temp: Boolean)
	{
		
		constructor() : this(Type.LOCAL_KORM, false)
		
		enum class Type
		{
			
			KEDIS,
			KUERY,
			
			LOCAL_XML,
			LOCAL_JSON,
			LOCAL_KORM,
			LOCAL_TOML;
			
		}
		
	}
	
}