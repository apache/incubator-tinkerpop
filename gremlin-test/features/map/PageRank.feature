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

Feature: Step - pageRank()
                
  Scenario: g_V_pageRank_hasXpageRankX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().pageRank().has("gremlin.pageRankVertexProgram.pageRank")
      """
    When iterated to list
    Then the result should be unordered
      | result |
      | v[marko] |
      | v[vadas] |
      | v[lop] |
      | v[josh] |
      | v[ripple] |
      | v[peter] |
    And the graph should return 6 for count of "g.withComputer().V().pageRank().has(\"gremlin.pageRankVertexProgram.pageRank\")"

  Scenario: g_V_outXcreatedX_pageRank_byXbothEX_byXprojectRankX_timesX0X_valueMapXname_projectRankX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().out("created").pageRank().by(__.bothE()).by("projectRank").times(0).valueMap("name", "projectRank")
      """
    When iterated to list
    Then the result should be unordered
      | result |
      | m[{"name": ["lop"], "projectRank": [3.0]}] |
      | m[{"name": ["lop"], "projectRank": [3.0]}] |
      | m[{"name": ["lop"], "projectRank": [3.0]}] |
      | m[{"name": ["ripple"], "projectRank": [1.0]}] |

  Scenario: g_V_pageRank_order_byXpageRank_decrX_byXnameX_name
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().pageRank().order().by("gremlin.pageRankVertexProgram.pageRank", Order.decr).by("name").values("name")
      """
    When iterated to list
    Then the result should be ordered
      | result |
      | lop    |
      | ripple |
      | josh   |
      | vadas  |
      | marko  |
      | peter  |

  Scenario: g_V_pageRank_order_byXpageRank_decrX_name_limitX2X
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().pageRank().order().by("gremlin.pageRankVertexProgram.pageRank", Order.decr).values("name").limit(2)
      """
    When iterated to list
    Then the result should be ordered
      | result |
      | lop    |
      | ripple |

  Scenario: g_V_pageRank_byXoutEXknowsXX_byXfriendRankX_project_byXnameX_byXvaluesXfriendRankX_mathX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().pageRank().by(__.outE("knows")).by("friendRank").project("name", "friendRank").by("name").by(__.values("friendRank").math("ceil(_ * 100)"))
      """
    When iterated to list
    Then the result should be unordered
      | result |
      | m[{"name": "marko", "friendRank": 15.0}] |
      | m[{"name": "vadas", "friendRank": 21.0}] |
      | m[{"name": "lop", "friendRank": 15.0}] |
      | m[{"name": "josh", "friendRank": 21.0}] |
      | m[{"name": "ripple", "friendRank": 15.0}] |
      | m[{"name": "peter", "friendRank": 15.0}] |

  Scenario: g_V_hasLabelXpersonX_pageRank_byXpageRankX_project_byXnameX_byXvaluesXpageRankX_mathX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().hasLabel("person").pageRank().by("pageRank").project("name", "pageRank").by("name").by(__.values("pageRank").math("ceil(_ * 100)"))
      """
    When iterated to list
    Then the result should be unordered
      | result |
      | m[{"name": "marko", "pageRank": 46.0}] |
      | m[{"name": "vadas", "pageRank": 59.0}] |
      | m[{"name": "josh", "pageRank": 59.0}] |
      | m[{"name": "peter", "pageRank": 46.0}] |

  Scenario: g_V_pageRank_byXpageRankX_asXaX_outXknowsX_pageRank_asXbX_selectXa_bX_by_byXmathX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().pageRank().by("pageRank").as("a").out("knows").values("pageRank").as("b").select("a", "b").by().by(__.math("ceil(_ * 100)"))
      """
    When iterated to list
    Then the result should be unordered
      | result |
      | m[{"a": "v[marko]", "b": 15.0}] |
      | m[{"a": "v[marko]", "b": 15.0}] |

  Scenario: g_V_hasLabelXsoftwareX_hasXname_rippleX_pageRankX1X_byXinEXcreatedXX_timesX1X_byXpriorsX_inXcreatedX_unionXboth__identityX_valueMapXname_priorsX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().hasLabel("software").has("name", "ripple").pageRank(1.0).by(__.inE("created")).times(1).by("priors").in("created").union(__.both(), __.identity()).valueMap("name", "priors")
      """
    When iterated to list
    Then the result should be unordered
      | result |
      | m[{"name": ["josh"], "priors": [1.0]}] |
      | m[{"name": ["marko"], "priors": [0.0]}] |
      | m[{"name": ["lop"], "priors": [0.0]}] |
      | m[{"name": ["ripple"], "priors": [0.0]}] |

  Scenario: g_V_outXcreatedX_groupXmX_byXlabelX_pageRankX1X_byXpageRankX_byXinEX_timesX1X_inXcreatedX_groupXmX_byXpageRankX_capXmX
    Given the modern graph
    And the traversal of
      """
      g.withComputer().V().out("created").group("m").by(T.label).pageRank(1.0).by("pageRank").by(__.inE()).times(1).in("created").group("m").by("pageRank").cap("m")
      """
    When iterated next
    Then the result should have a count of 3

    # TODO: would really like to have a full assertion here, but withComputer() makes stuff not always return in order.
    # order could be forced, but that just bulks up the test. we could write better assertion logic but that makes it
    # harder for GLVs to be tested.  
    #  | result |
    #  | m[{"d[1.0].d":"l[v[marko],v[marko],v[marko],v[peter],v[peter],v[peter]]","d[2.0].d":"l[v[josh],v[josh],v[josh],v[josh]]","software":"l[v[lop],v[lop],v[lop],v[ripple]]"}] |
