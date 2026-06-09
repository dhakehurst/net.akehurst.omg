/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.omg._simple_mof_for_xmi


import net.akehurst.kotlinx.collections.LazyMapNotNull
import net.akehurst.kotlinx.collections.lazyMapNotNull
import kotlin.collections.set
import kotlin.reflect.KClass

class XmiReferenceHandler {

    val refsByFile: LazyMapNotNull<String, MutableMap<String, Any>> = lazyMapNotNull { file ->
        mutableMapOf()
    }

    fun hasRef(currentFileName: String?, ref: String) =
            currentFileName?.let { refsByFile[currentFileName].containsKey(ref) }
            ?: refsByFile.values.any { it.containsKey(ref) }

    fun getRef(currentFileName: String?, ref: String) =
        refsByFile["OVERRIDE"][ref]
            ?: currentFileName?.let { refsByFile[currentFileName][ref] }
            ?: refsByFile.values.firstNotNullOfOrNull { it[ref] }

    fun setRef(currentFileName: String, ref: String, value: Any) {
        refsByFile[currentFileName][ref] = value
    }

    inline fun<reified T:Any> allValuesOfType(): List<T> = refsByFile.values.flatMap { it.values }.filterIsInstance<T>()
}