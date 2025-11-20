/*
 * Copyright 2025 Román Ignacio Romero (Romaster)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.romaster.appiconscrapper

import android.graphics.Bitmap

/**
 * Cache temporal en memoria para compartir los íconos procesados
 * entre ThemeCustomizationActivity y IconPreviewActivity.
 */
object IconCache {
    var iconsProcessed: List<Bitmap>? = null

    fun clear() {
        iconsProcessed = null
    }
}