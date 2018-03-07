package com.sxtanna.korm.base

import com.sxtanna.korm.base.CustomCodecTest.Codec
import com.sxtanna.korm.data.KormType
import com.sxtanna.korm.data.custom.KormCustomCodec
import com.sxtanna.korm.reader.KormReader
import com.sxtanna.korm.writer.KormWriter

@KormCustomCodec(Codec::class)
data class CustomCodecTest(val name: String) {

    class Codec : KormCodec<CustomCodecTest> {

        override fun pull(reader: KormReader.ReaderContext, types: MutableList<KormType>): CustomCodecTest? {
            return types.typeByName("names")
                    ?.asList()
                    ?.let { it.data[0] as? String }
                    ?.let { CustomCodecTest(it) }
        }

        override fun push(data: CustomCodecTest?, writer: KormWriter.WriterContext) {
            data ?: return writer.writeData("null")

            writer.writeName("names")
            writer.writeList(listOf(data.name, data.name))
        }

    }

}