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

package net.akehurst.omg.templates

import net.akehurst.omg._simple_mof_for_xmi.GeneratorFromTemplate
import net.akehurst.omg._simple_mof_for_xmi.MofModel
import net.akehurst.omg._simple_mof_for_xmi.SimpleMof
import net.akehurst.omg._simple_mof_for_xmi.XmiReferenceHandler
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals

abstract class test_TemplatesAbstract {

    companion object {

        val refHandler = XmiReferenceHandler()

        fun doTest(templateString:String, model: MofModel, parameters:Map<String,Any>, expected:Map<String,String>) {
            val logger = LoggerFactory.getLogger(test_TemplatesAbstract::class.java)
            val generator = GeneratorFromTemplate(logger, templateString, SimpleMof.types)
            val actual = generator.generate(model, parameters)
            assertEquals(expected.keys, actual.keys)
            for(key in actual.keys) {
                assertEquals(expected[key], actual[key])
            }
        }
    }

}