package io.github.theapache64.retrosheet.core

import io.github.theapache64.retrosheet.utils.SheetUtils
import kotlinx.serialization.json.Json

expect fun shouldUseProxyForWrite(): Boolean

class RetrosheetConfig
private constructor(
    val isLoggingEnabled: Boolean = false,
    internal val useProxyForWrite: Boolean,
    val sheets: Map<String, Map<String, String>>,
    val forms: Map<String, String>,
    val json: Json
) {

    class Builder {
        private val sheets = mutableMapOf<String, Map<String, String>>()
        private val forms = mutableMapOf<String, String>()
        private var isLoggingEnabled: Boolean = false
        private var useProxyForWrite: Boolean = shouldUseProxyForWrite()
        private var json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun build(): RetrosheetConfig {
            return RetrosheetConfig(
                isLoggingEnabled,
                useProxyForWrite,
                sheets,
                forms,
                json
            )
        }

        fun setLogging(isLoggingEnabled: Boolean): Builder {
            this.isLoggingEnabled = isLoggingEnabled
            return this
        }

        internal fun setUseProxyForWrite(useProxyForWrite: Boolean): Builder {
            this.useProxyForWrite = useProxyForWrite
            return this
        }

        @Suppress("unused")
        fun setJson(json: Json): Builder {
            this.json = json
            return this
        }

        @Suppress("MemberVisibilityCanBePrivate")
        fun addSheet(sheetName: String, columnMap: Map<String, String>): Builder {
            ColumnNameVerifier(columnMap.keys).verify()
            this.sheets[sheetName] = columnMap
            return this
        }

        /**
         * Columns should be in order
         */
        fun addSheet(sheetName: String, vararg columns: String): Builder {
            return addSheet(
                sheetName,
                SheetUtils.toLetterMap(*columns)
            )
        }

        fun addForm(endPoint: String, formLink: String): Builder {
            if (endPoint.contains('/')) {
                throw IllegalArgumentException("Form endPoint name cannot contains '/'. Found '$endPoint'")
            }
            forms[endPoint] = formLink
            return this
        }
    }
}