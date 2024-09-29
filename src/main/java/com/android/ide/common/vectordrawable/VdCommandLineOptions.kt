/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.ide.common.vectordrawable

import java.io.File
import java.util.*

data class VdCommandLineOptions(
    var convertSvg: Boolean = false,
    var inputFiles: Array<File>? = null,
    var outputDir: File? = null,
    var forceWidth: Double = -1.0,
    var forceHeight: Double = -1.0,
    var addHeader: Boolean = false
) {
    companion object {
        private const val OPTION_CONVERT = "-c"
        private const val OPTION_IN = "-in"
        private const val OPTION_OUT = "-out"
        private const val OPTION_FORCE_WIDTH_DP = "-widthDp"
        private const val OPTION_FORCE_HEIGHT_DP = "-heightDp"
        private const val OPTION_ADD_HEADER = "-addHeader"

        val COMMAND_LINE_OPTION = """
            Converts SVG files to VectorDrawable XML files.
            Usage: -c -in <file or directory> -out <directory> [-widthDp <size>] [-heightDp <size>] [-addHeader]
            Options:
              -c                        Convert SVG files to VectorDrawable XML.
              -in <file or directory>   Input SVG file or directory containing SVG files.
              -out <directory>          Output directory for converted XML files.
              -widthDp <size>           Force the width to be <size> dp, <size> must be integer.
              -heightDp <size>          Force the height to be <size> dp, <size> must be integer.
              -addHeader                Add AOSP header to the top of the generated XML file.
            Example:
              Convert SVG files from <directory> into XML files in the output directory:
              vd-tool -c -in <input_directory> -out <output_directory>
        """.trimIndent()
    }

    /**
     * Parse the command line options.
     *
     * @param args the incoming command line options
     * @return null if no critical error happens, otherwise the error message.
     */
    fun parse(args: Array<String>?): String? {
        var argIn: File? = null

        // First parse the command line options.
        if (args.isNullOrEmpty()) {
            return "ERROR: empty arguments"
        }

        var index = 0
        while (index < args.size) {
            when (args[index].lowercase(Locale.getDefault())) {
                OPTION_CONVERT -> {
                    println("$OPTION_CONVERT parsed, SVG files will be converted")
                    convertSvg = true
                }

                OPTION_IN -> {
                    if (index + 1 < args.size) {
                        argIn = File(args[++index])
                        println("$OPTION_IN parsed ${argIn.absolutePath}")
                    }
                }

                OPTION_OUT -> {
                    if (index + 1 < args.size) {
                        outputDir = File(args[++index].replaceFirst("^~".toRegex(), System.getProperty("user.home")))
                        println("$OPTION_OUT parsed ${outputDir?.absolutePath}")
                    }
                }

                OPTION_FORCE_WIDTH_DP -> {
                    if (index + 1 < args.size) {
                        forceWidth = args[++index].toDoubleOrNull() ?: -1.0
                        println("$OPTION_FORCE_WIDTH_DP parsed $forceWidth")
                    }
                }

                OPTION_FORCE_HEIGHT_DP -> {
                    if (index + 1 < args.size) {
                        forceHeight = args[++index].toDoubleOrNull() ?: -1.0
                        println("$OPTION_FORCE_HEIGHT_DP parsed $forceHeight")
                    }
                }

                OPTION_ADD_HEADER -> {
                    addHeader = true
                    println("$OPTION_ADD_HEADER parsed, AOSP header will be added to the XML file")
                }

                else -> return "ERROR: unrecognized option ${args[index]}"
            }
            index++
        }

        if (!convertSvg) {
            return "ERROR: $OPTION_CONVERT must be specified"
        }

        // Then we decide the input resources.
        inputFiles = when {
            argIn?.isFile == true -> arrayOf(argIn)
            argIn?.isDirectory == true -> argIn.listFiles()?.sortedArray()
            else -> null
        }

        if (outputDir == null) {
            return "ERROR: no output directory specified"
        }
        if (!outputDir!!.isDirectory) {
            return "ERROR: Output directory ${outputDir!!.absolutePath} doesn't exist or isn't a valid directory"
        }

        if (inputFiles == null || inputFiles!!.isEmpty()) {
            return "ERROR: There is no file to process in ${argIn?.name}"
        }

        return null
    }
}