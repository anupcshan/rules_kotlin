/*
 * Copyright 2018 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.bazel.kotlin.builder.model

import com.google.protobuf.util.JsonFormat
import io.bazel.kotlin.builder.*
import io.bazel.kotlin.model.KotlinModel

/**
 * The flags supported by the worker.
 */
class Flags(argMap: ArgMap) {
    val label = argMap.mandatorySingle(JavaBuilderFlags.TARGET_LABEL.flag)
    val ruleKind = argMap.mandatorySingle(JavaBuilderFlags.RULE_KIND.flag)

    val classDir = argMap.lazilyCreatedDirectory(JavaBuilderFlags.CLASSDIR.flag)
    val tempDir = argMap.lazilyCreatedDirectory(JavaBuilderFlags.TEMPDIR.flag)
    val sourceGenDir = argMap.lazilyCreatedDirectory(JavaBuilderFlags.SOURCEGEN_DIR.flag)

    // these following three fields shouldn't be used directly but via CompileDependencies.
    val classpath = argMap.mandatory(JavaBuilderFlags.CLASSPATH.flag)
    val directDependencies = argMap.labelDepMap(JavaBuilderFlags.DIRECT_DEPENDENCY.flag)
    val indirectDependencies = argMap.labelDepMap(JavaBuilderFlags.INDIRECT_DEPENDENCY.flag)

    // val strictJavaDeps = argMap.mandatorySingle(JavaBuilderFlags.STRICT_JAVA_DEPS.flag)
    val outputClassJar = argMap.mandatorySingle(JavaBuilderFlags.OUTPUT.flag)

    val source = argMap.optional(JavaBuilderFlags.SOURCES.flag)
    val sourceJars = argMap.optional(JavaBuilderFlags.SOURCE_JARS.flag)

    val classpath = argMap.mandatory(JavaBuilderFlags.CLASSPATH.flag)
    val plugins: KotlinModel.CompilerPlugins? = argMap.optionalSingle("--kt-plugins")?.let { input ->
        KotlinModel.CompilerPlugins.newBuilder().let {
            jsonFormat.merge(input, it)
            it.build()
        }
    }

    val outputJdeps = argMap.mandatorySingle("--output_jdeps")

    val kotlinApiVersion = argMap.mandatorySingle("--kotlin_api_version")
    val kotlinLanguageVersion = argMap.mandatorySingle("--kotlin_language_version")
    val kotlinJvmTarget = argMap.mandatorySingle("--kotlin_jvm_target")

    /**
     * These flags are passed through to the compiler verbatim, the rules ensure they are safe. These flags are to toggle features or they carry a single value
     * so the string is tokenized by space.
     */
    val kotlinPassthroughFlags = argMap.optionalSingle("--kotlin_passthrough_flags")

    val kotlinModuleName = argMap.optionalSingle("--kotlin_module_name")

    companion object {
        @JvmStatic
        private val jsonTypeRegistry = JsonFormat.TypeRegistry.newBuilder()
            .add(KotlinModel.getDescriptor().messageTypes).build()

        @JvmStatic
        private val jsonFormat = JsonFormat.parser().usingTypeRegistry(jsonTypeRegistry)
    }
}