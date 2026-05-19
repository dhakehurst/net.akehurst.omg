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
package net.akehurst.omg.templates.examples


import net.akehurst.kotlinx.collections.OrderedSet
import net.akehurst.kotlinx.collections.mutableOrderedSetOf

interface SimplePropertyCollections {
    /**
     * prop1: PropType [0..-1] {  }
     */
    val prop1Collection: Collection<PropType>
    fun set_prop1Collection(value: Collection<PropType>)

    /**
     * prop2: PropType [0..-1]  { ordered }
     */
    val prop2List: List<PropType>
    fun set_prop2List(value: List<PropType>)

    /**
     * prop3: PropType [0..-1]  { unique }
     */
    val prop3Set: Set<PropType>
    fun set_prop3Set(value: Set<PropType>)

    /**
     * prop4: PropType [0..-1]  { unique, ordered }
     */
    val prop4OrderedSet: OrderedSet<PropType>
    fun set_prop4OrderedSet(value:OrderedSet<PropType>)
}

data class SimplePropertyCollectionsRam(val identifier_: Any): SimplePropertyCollections {
    override var prop1Collection: Collection<PropType> = mutableListOf()
    override fun set_prop1Collection(value: Collection<PropType>) {
        this.prop1Collection = value
    }

    override var prop2List: List<PropType> = mutableListOf()
    override fun set_prop2List(value: List<PropType>) {
        this.prop2List = value
    }

    override var prop3Set: Set<PropType> = mutableSetOf()
    override fun set_prop3Set(value: Set<PropType>) {
        this.prop3Set = value
    }

    override var prop4OrderedSet: OrderedSet<PropType> = mutableOrderedSetOf()
    override fun set_prop4OrderedSet(value: OrderedSet<PropType>) {
        this.prop1Collection = value
    }
}