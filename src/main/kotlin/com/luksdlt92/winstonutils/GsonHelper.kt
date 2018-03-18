package com.luksdlt92.winstonutils

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import java.io.File

class GsonHelper {
    companion object {

        @JvmStatic fun load(file: File, obj: Any) : Any {
            val config = ConfigFactory.parseFile(file)
            val configJSON = config.root().render(ConfigRenderOptions.concise())
            println(configJSON)
            return Gson().fromJson<Any>(configJSON, obj.javaClass)
        }

    }
}


