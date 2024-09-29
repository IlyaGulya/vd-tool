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

import com.android.SdkConstants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.xml.sax.InputSource

/**
 * Support a command line tool to convert SVG files to VectorDrawables and display them.
 */
class VdCommandLineTool {
    companion object {
        const val BROKEN_FILE_EXTENSION = ".broken"
        private const val DBG_COPY_BROKEN_SVG = false

        private fun exitWithErrorMessage(message: String) {
            System.err.println(message)
            kotlin.system.exitProcess(-1)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val options = VdCommandLineOptions()
            val criticalError = options.parse(args)
            if (criticalError != null) {
                exitWithErrorMessage("$criticalError\n\n${VdCommandLineOptions.COMMAND_LINE_OPTION}")
            }

            if (options.convertSvg) {
                convertSVGToXml(options)
            } else {
                println("No action specified. Use -c to convert SVG files.")
            }
        }

        private fun convertSVGToXml(options: VdCommandLineOptions): Array<File> {
            val inputSVGFiles = options.inputFiles ?: return emptyArray()
            val outputDir = options.outputDir ?: return emptyArray()
            var totalSvgFileCounter = 0
            var errorSvgFileCounter = 0
            val allOutputFiles = mutableListOf<File>()

            inputSVGFiles.forEach { inputSVGFile ->
                val svgFilename = inputSVGFile.name
                if (!svgFilename.endsWith(SdkConstants.DOT_SVG)) return@forEach

                val svgFilenameWithoutExtension = svgFilename.substringBeforeLast('.')
                val outputFile = File(outputDir, "$svgFilenameWithoutExtension${SdkConstants.DOT_XML}")
                allOutputFiles.add(outputFile)

                try {
                    val byteArrayOutStream = ByteArrayOutputStream()
                    val error = Svg2Vector.parseSvgToXml(inputSVGFile.toPath(), byteArrayOutStream)

                    if (error.isNotEmpty()) {
                        errorSvgFileCounter++
                        System.err.println("Error: $error")
                        if (DBG_COPY_BROKEN_SVG) {
                            // Copy the broken svg file in the same directory but with a new extension.
                            val brokenFileName = "$svgFilename$BROKEN_FILE_EXTENSION"
                            val brokenSvgFile = File(outputDir, brokenFileName)
                            inputSVGFile.copyTo(brokenSvgFile, overwrite = true)
                        }
                    }

                    // Override the size info if needed. Negative value will be ignored.
                    var vectorXmlContent = byteArrayOutStream.toString()
                    if (options.forceWidth > 0 || options.forceHeight > 0) {
                        val vdDocument = parseVdStringIntoDocument(vectorXmlContent, null)
                        if (vdDocument != null) {
                            val info = VdOverrideInfo(
                                width = options.forceWidth,
                                height = options.forceHeight,
                                tint = null,
                                alpha = 1.0,
                                autoMirrored = false
                            )
                            vectorXmlContent = VdPreview.overrideXmlContent(vdDocument, info, null)
                        }
                    }

                    if (options.addHeader) {
                        vectorXmlContent = AOSP_HEADER + vectorXmlContent
                    }

                    PrintWriter(outputFile).use { writer ->
                        writer.print(vectorXmlContent)
                    }
                } catch (e: Exception) {
                    System.err.println("Exception: ${e.message}")
                    e.printStackTrace()
                }
                totalSvgFileCounter++
            }

            println("Converted $totalSvgFileCounter SVG files in total, errors found in $errorSvgFileCounter files")
            return allOutputFiles.toTypedArray()
        }

        /**
         * Parses a vector drawable XML file into a [Document] object.
         *
         * @param xmlFileContent the content of the VectorDrawable's XML file.
         * @param errorLog when errors were found, log them in this builder if it is not null.
         * @return parsed document or null if errors happened.
         */
        private fun parseVdStringIntoDocument(xmlFileContent: String, errorLog: StringBuilder?): Document? {
            return try {
                val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                db.parse(InputSource(StringReader(xmlFileContent)))
            } catch (e: Exception) {
                errorLog?.append("Exception while parsing XML file:\n${e.message}")
                null
            }
        }

        private val AOSP_HEADER = """
            <!--
            Copyright (C) ${Calendar.getInstance().get(Calendar.YEAR)} The Android Open Source Project

            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at

                 http://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software
            distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            See the License for the specific language governing permissions and
            limitations under the License.
            -->
        """.trimIndent() + "\n"
    }
}
