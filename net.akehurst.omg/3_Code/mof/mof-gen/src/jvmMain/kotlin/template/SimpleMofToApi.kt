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

package net.akehurst.omg.mof.gen.template

object SimpleMofToApi {

    val format = $$"""
        namespace net.akehurst.omg.mof.gen.template
        import net.akehurst.omg.mof.gen.simple
        
        fun valid(str:String): String = {
          keywords := Set( 'as','break','class','continue','do','else','false','for','fun',
                           'if','in','interface','is','null','object','package','return',
                           'super','this','throw','true','try','typealias','typeof','val',
                           'var','when','while'
                      )
          when {
            keywords.contains(str) -> str+'_'
            else -> str
          }
        }
        
        format ApiPackageFile {
            MofPackage -> "
                /**
                 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
                 *
                 * Licensed under the Apache License, Version 2.0 (the \"License\");
                 * you may not use this file except in compliance with the License.
                 * You may obtain a copy of the License at
                 *
                 *         http://www.apache.org/licenses/LICENSE-2.0
                 *
                 * Unless required by applicable law or agreed to in writing, software
                 * distributed under the License is distributed on an \"AS IS\" BASIS,
                 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                 * See the License for the specific language governing permissions and
                 * limitations under the License.
                 */            
            
                // *** Generated code do NOT manually edit. ***
                package $qualifiedName
                
                import net.akehurst.kotlinx.collections.OrderedSet
                $[allImport.map({it -> 'import UML.'+it+'.*'}) sep $EOL]
                
                $[enums sep $EOL]
                
                $[classes sep $EOL]
            "
            MofEnum -> "
              enum class ${valid(name)} { }
            "
            MofClass -> {
                superTypes := when {
                  generalizations.isEmpty -> ''
                  else -> " : $[generalizations sep ',']"
                }
                "
                  interface ${valid(name)}$superTypes {
                     $[attributes sep $EOL]
                  }
                "
            }
            MofProperty -> {
                val_var := when {
                  true==isReadOnly -> 'val'
                  else -> 'var'
                }
                resType := when {
                  (1 == upperBound) -> when {
                    (0 == lowerBound) -> valid(typeName)+'?'  
                    else -> valid(typeName)
                  }
                  else -> when {
                    (false == isUnique) and (false == isOrdered) -> "Collection<${valid(typeName)}>"
                    (true == isUnique) and (false == isOrdered) -> "Set<${valid(typeName)}>"
                    (false == isUnique) and (true == isOrdered) -> "List<${valid(typeName)}>"
                    (true == isUnique) and (true == isOrdered) -> "OrderedSet<${valid(typeName)}>"
                    else -> 'ERROR'
                  }
                }
                cons := when {
                    (false == isUnique) and (false == isOrdered) -> '{}'
                    (true == isUnique) and (false == isOrdered) -> '{unique}'
                    (false == isUnique) and (true == isOrdered) -> '{ordered}'
                    (true == isUnique) and (true == isOrdered) -> '{ordered, unique}'
                    else -> '{ERROR}'               
                }
                "
                  /** [$lowerBound..${when{ (-1==upperBound) -> '*' else -> upperBound}}] $cons */
                  $val_var ${valid(name)}: $resType
                "
            }
        }
        
        
    """

}