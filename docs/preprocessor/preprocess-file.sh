#!/bin/bash
#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

TP_HOME=`pwd`
CONSOLE_HOME=$1

input=$2
name=`basename ${input}`
output="${TP_HOME}/target/postprocess-asciidoc/${name}"

trap cleanup INT

function cleanup() {
  rm -rf ${output}
  exit 255
}

function processed {
  echo -ne "\r   progress: [====================================================================================================] 100%\n"
}

echo
echo " * source:   ${input}"
echo "   target:   ${output}"
echo -ne "   progress: initializing"

if [ $(grep -c '^\[gremlin' ${input}) -gt 0 ]; then
  if [ ${output} -nt ${input} ]; then
    processed
    exit 0
  fi
  pushd "${CONSOLE_HOME}" > /dev/null
  bin/gremlin.sh -e ${TP_HOME}/docs/preprocessor/processor.groovy ${input} > ${input}.part2.groovy
  ec=${PIPESTATUS[0]}
  if [ ${ec} -eq 0 ]; then
    cat ${input}.part2.groovy | grep -o '^pb([0-9][0-9]*' | tail -n1 | cut -d '(' -f2 | xargs echo "TOTAL_LINES =" > ${input}.part1.groovy
    cat ${input}.part?.groovy > ${input}.groovy && rm -f ${input}.part?.groovy
    cat ${input}.groovy | HADOOP_GREMLIN_LIBS="${CONSOLE_HOME}/ext/hadoop-gremlin/lib" bin/gremlin.sh | grep -v '^gremlin> ' | awk 'BEGIN {i=0} /^==>¶IGNORE$/ {i=!i} /^==>¶END$/ {p=0} !/^==>¶IGNORE$/ {if(!i&&p)print} /^==>¶START$/ {p=1}' | grep -v '^WARN ' | sed 's/^==>¶//' > ${output}
    ec=${PIPESTATUS[1]}
  fi
  rm -f ${input}.groovy
  popd > /dev/null
  if [ ${ec} -ne 0 ]; then
    cleanup
  fi
else
  cp ${input} ${output}
  processed
fi
