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

Feature: Step - percentile()

  Scenario: g_V_age_percentileX50X
    Given the modern graph
    And the traversal of
      """
      g.V().values("age").percentile(50)
      """
    When iterated to list
    Then the result should be unordered
      | result  |
      | d[29].i |

  Scenario: g_V_foo_percentileX50X
    Given the modern graph
    And the traversal of
      """
      g.V().values("foo").percentile(50)
      """
    When iterated to list
    Then the result should be empty

  Scenario: g_V_age_fold_percentileXlocal_50X
    Given the modern graph
    And the traversal of
      """
      g.V().values("age").fold().percentile(Scope.local, 50)
      """
    When iterated to list
    Then the result should be unordered
      | result  |
      | d[29].i |

  Scenario: g_V_foo_fold_percentileXlocal_50X
    Given the modern graph
    And the traversal of
      """
      g.V().values("foo").fold().percentile(Scope.local, 50)
      """
    When iterated to list
    Then the result should be empty

  Scenario: g_V_age_percentileX25_75X
    Given the modern graph
    And the traversal of
      """
      g.V().values("age").percentile(25, 75)
      """
    When iterated to list
    Then the result should be unordered
      | result                                        |
      | m[{"d[25].i":"d[27].i", "d[75].i":"d[32].i"}] |

  Scenario: g_V_age_fold_percentileXlocal_25_75X
    Given the modern graph
    And the traversal of
      """
      g.V().values("age").fold().percentile(Scope.local, 25, 75)
      """
    When iterated to list
    Then the result should be unordered
      | result                                        |
      | m[{"d[25].i":"d[27].i", "d[75].i":"d[32].i"}] |