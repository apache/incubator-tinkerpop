/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * @author Jorge Bay Gondra
 */
'use strict';

const assert = require('assert');
const expect = require('chai').expect;
const { Vertex } = require('../../lib/structure/graph');
const { traversal } = require('../../lib/process/anonymous-traversal');
const { GraphTraversalSource, GraphTraversal, statics } = require('../../lib/process/graph-traversal');
const { SubgraphStrategy, ReadOnlyStrategy,
        ReservedKeysVerificationStrategy, EdgeLabelVerificationStrategy } = require('../../lib/process/traversal-strategy');
const Bytecode = require('../../lib/process/bytecode');
const helper = require('../helper');
const __ = statics;

let connection;

class SocialTraversal extends GraphTraversal {
  constructor(graph, traversalStrategies, bytecode) {
    super(graph, traversalStrategies, bytecode);
  }

  aged(age) {
    return this.has('person', 'age', age);
  }
}

class SocialTraversalSource extends GraphTraversalSource {
  constructor(graph, traversalStrategies, bytecode) {
    super(graph, traversalStrategies, bytecode, SocialTraversalSource, SocialTraversal);
  }

  person(name) {
    return this.V().has('person', 'name', name);
  }
}

function anonymous() {
  return new SocialTraversal(null, null, new Bytecode());
}

function aged(age) {
  return anonymous().aged(age);
}

describe('Traversal', function () {
  before(function () {
    connection = helper.getConnection('gmodern');
    return connection.open();
  });
  after(function () {
    return connection.close();
  });
  describe('#toList()', function () {
    it('should submit the traversal and return a list', function () {
      var g = traversal().withRemote(connection);
      return g.V().toList().then(function (list) {
        assert.ok(list);
        assert.strictEqual(list.length, 6);
        list.forEach(v => assert.ok(v instanceof Vertex));
      });
    });
  });
  describe('#clone()', function () {
    it('should reset a traversal when cloned', function () {
      var g = traversal().withRemote(connection);
      var t = g.V().count();
      return t.next().then(function (item1) {
            assert.ok(item1);
            assert.strictEqual(item1.value, 6);
            t.clone().next().then(function (item2) {
              assert.ok(item2);
              assert.strictEqual(item2.value, 6);
            });
      });
    });
  });
  describe('#next()', function () {
    it('should submit the traversal and return an iterator', function () {
      var g = traversal().withRemote(connection);
      var t = g.V().count();
      return t.hasNext()
        .then(function (more) {
          assert.ok(more);
          assert.strictEqual(more, true);
          return t.next();
        }).then(function (item) {
          assert.strictEqual(item.done, false);
          assert.strictEqual(typeof item.value, 'number');
          return t.next();
        }).then(function (item) {
          assert.ok(item);
          assert.strictEqual(item.done, true);
          assert.strictEqual(item.value, null);
        });
    });
  });
  describe('dsl', function() {
    it('should expose DSL methods', function() {
      const g = traversal(SocialTraversalSource).withRemote(connection);
      return g.person('marko').aged(29).values('name').toList().then(function (list) {
          assert.ok(list);
          assert.strictEqual(list.length, 1);
          assert.strictEqual(list[0], 'marko');
        });
    });

    it('should expose anonymous DSL methods', function() {
      const g = traversal(SocialTraversalSource).withRemote(connection);
      return g.person('marko').filter(aged(29)).values('name').toList().then(function (list) {
        assert.ok(list);
        assert.strictEqual(list.length, 1);
        assert.strictEqual(list[0], 'marko');
      });
    });
  });
  describe("more complex traversals", function() {
    it('should return paths of value maps', function() {
      const g = traversal().withRemote(connection);
      return g.V(1).out().in_().limit(1).path().by(__.valueMap('name')).toList().then(function (list) {
        assert.ok(list);
        assert.strictEqual(list.length, 1);
        assert.strictEqual(list[0].objects[0].get('name')[0], "marko");
        assert.strictEqual(list[0].objects[1].get('name')[0], "lop");
        assert.strictEqual(list[0].objects[2].get('name')[0], "marko");
      });
    });
  });
  describe("should allow TraversalStrategy definition", function() {
    it('should allow SubgraphStrategy', function() {
      const g = traversal().withRemote(connection).withStrategies(
          new SubgraphStrategy({vertices:__.hasLabel("person"), edges:__.hasLabel("created")}));
      g.V().count().next().then(function (item1) {
        assert.ok(item1);
        assert.strictEqual(item1.value, 4);
      });
      g.E().count().next().then(function (item1) {
        assert.ok(item1);
        assert.strictEqual(item1.value, 0);
      });
      g.V().label().dedup().count().next().then(function (item1) {
        assert.ok(item1);
        assert.strictEqual(item1.value, 1);
      });
      g.V().label().dedup().next().then(function (item1) {
        assert.ok(item1);
        assert.strictEqual(item1.value, "person");
      });
    });
    it('should allow ReadOnlyStrategy', function() {
      const g = traversal().withRemote(connection).withStrategies(new ReadOnlyStrategy());
      return g.addV().iterate().then(() => assert.fail("should have tanked"), (err) => assert.ok(err));
    });
    it('should allow ReservedKeysVerificationStrategy', function() {
      const g = traversal().withRemote(connection).withStrategies(new ReservedKeysVerificationStrategy(false, true));
      return g.addV().property("id", "please-don't-use-id").iterate().then(() => assert.fail("should have tanked"), (err) => assert.ok(err));
    });
    it('should allow EdgeLabelVerificationStrategy', function() {
      const g = traversal().withRemote(connection).withStrategies(new EdgeLabelVerificationStrategy(false, true));
      g.V().outE("created", "knows").count().next().then(function (item1) {
        assert.ok(item1);
        assert.strictEqual(item1.value, 6);
      });
      return g.V().out().iterate().then(() => assert.fail("should have tanked"), (err) => assert.ok(err));
    });
  });
});