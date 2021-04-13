# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

Feature: Step - stdev()

  Scenario: g_V_age_stdev
    Given the modern graph
    And the traversal of
      """
      g.V().values("age").stdev()
      """
    When iterated to list
    Then the result should be unordered
      | result                 |
      | d[3.031088913245535].d |

  Scenario: g_V_foo_stdev
    Given the modern graph
    And the traversal of
      """
      g.V().values("foo").stdev()
      """
    When iterated to list
    Then the result should be empty

  Scenario: g_V_age_fold_stdevXlocalX
    Given the modern graph
    And the traversal of
      """
      g.V().values("age").fold().stdev(Scope.local)
      """
    When iterated to list
    Then the result should be unordered
      | result                 |
      | d[3.031088913245535].d |

  Scenario:  g_V_foo_fold_stdevXlocalX
    Given the modern graph
    And the traversal of
      """
      g.V().values("foo").fold().stdev(Scope.local)
      """
    When iterated to list
    Then the result should be empty