package com.sxtanna.korm.base

import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.reader.KormReader
import com.sxtanna.korm.writer.KormWriter

@KormCustomCodec(Lang.Codec::class)
interface Lang {

    val name: String


    class Codec : KormCodec<Lang> {

        override fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): Lang? {
            println(types)
            return English
        }

        override fun push(data: Lang?, writer: KormWriter.WriterContext) {
            writer.writeHash(mapOf("name" to data?.name))
        }

    }


    object English : Lang {

        override val name = "English"

    }

    object French : Lang {

        override val name = "French"

    }

}