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

package net.akehurst.omg.uml.gui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ClassDiagram : StructureDiagram {

    @Composable
    fun AssociationView() {
    }

    @Composable
    fun ClassifierView(titleBoxText:String, compartments:List<List<String>>,stroke:Color, fill: Color) = Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fill)
            .border(1.5.dp, stroke)
    ) {
        Text(
            text = titleBoxText,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = stroke
        )
        compartments.forEach { comp ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fill)
                    .border(1.5.dp, stroke)
            ) {
                comp.forEach { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = stroke
                    )
                }
            }
        }
    }


    @Composable
    fun DependencyView() {
    }

    @Composable
    fun GeneralizationView() {
    }
}