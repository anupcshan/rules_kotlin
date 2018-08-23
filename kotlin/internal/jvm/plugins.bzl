# Copyright 2018 The Bazel Authors. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
load(
    "//kotlin/internal/utils:utils.bzl",
    _utils = "utils",
)

KtJvmPluginInfo = provider(
    doc = "This provider contains the plugin info for the JVM aspect",
    fields = {
        "annotation_processors": "a serializeable list of structs containing annotation processor definitions",
        "transitive_runtime_jars": "set of jars required during annotation processor execution",
    },
)

_EMPTY_PLUGIN_INFO = [KtJvmPluginInfo(annotation_processors = [], transitive_runtime_jars = depset())]

def merge_plugin_infos(attrs):
    """Merge all of the plugin infos found in the provided sequence of attributes.
    Returns:
        A KtJvmPluginInfo provider, Each of the entries is serializable."""
    tally = {}
    annotation_processors = []
    runtime_jars = depset()
    for info in [a[KtJvmPluginInfo] for a in attrs]:
        for p in info.annotation_processors:
            if p.label not in tally:
                tally[p.label] = True
                annotation_processors.append(p)
        runtime_jars += info.transitive_runtime_jars
    return KtJvmPluginInfo(
        annotation_processors = annotation_processors,
        transitive_runtime_jars = runtime_jars,
    )

def _kt_jvm_plugin_aspect_impl(target, ctx):
    if ctx.rule.kind == "java_plugin":
        processor = ctx.rule.attr
        merged_deps = java_common.merge([j[JavaInfo] for j in processor.deps])
        return [KtJvmPluginInfo(
            annotation_processors = [
                struct(
                    label = _utils.restore_label(ctx.label),
                    processor_class = processor.processor_class,
                    classpath = [cp.path for cp in merged_deps.transitive_runtime_jars],
                    generates_api = processor.generates_api,
                ),
            ],
            transitive_runtime_jars = merged_deps.transitive_runtime_jars,
        )]
    elif ctx.rule.kind == "java_library":
        return [merge_plugin_infos(ctx.rule.attr.exported_plugins)]
    else:
        return _EMPTY_PLUGIN_INFO

kt_jvm_plugin_aspect = aspect(
    doc = """This aspect collects Java Plugins info and other Kotlin compiler plugin configurations from the graph.""",
    attr_aspects = [
        "plugins",
        "exported_plugins",
    ],
    provides = [KtJvmPluginInfo],
    implementation = _kt_jvm_plugin_aspect_impl,
)
