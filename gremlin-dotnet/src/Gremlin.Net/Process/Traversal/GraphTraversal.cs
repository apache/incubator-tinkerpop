#region License

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#endregion

using System.Collections.Generic;
using System.Linq;
using Gremlin.Net.Structure;

namespace Gremlin.Net.Process.Traversal
{
    /// <summary>
    ///     Graph traversals are the primary way in which graphs are processed.
    /// </summary>
    public class GraphTraversal<S, E> : DefaultTraversal<S, E>
    {
        /// <summary>
        ///     Initializes a new instance of the <see cref="GraphTraversal{SType, EType}" /> class.
        /// </summary>
        public GraphTraversal()
            : this(new List<ITraversalStrategy>(), new Bytecode(), true)
        {
        }

        /// <summary>
        ///     Initializes a new instance of the <see cref="GraphTraversal{SType, EType}" /> class.
        /// </summary>
        /// <param name="traversalStrategies">The traversal strategies to be used by this graph traversal at evaluation time.</param>
        /// <param name="bytecode">The <see cref="Bytecode" /> associated with the construction of this graph traversal.</param>
        public GraphTraversal(ICollection<ITraversalStrategy> traversalStrategies, Bytecode bytecode)
            : this(traversalStrategies, bytecode, false)
        {
        }

        /// <summary>
        ///     Initializes a new instance of the <see cref="GraphTraversal{SType, EType}" /> class.
        /// </summary>
        /// <param name="traversalStrategies">The traversal strategies to be used by this graph traversal at evaluation time.</param>
        /// <param name="bytecode">The <see cref="Bytecode" /> associated with the construction of this graph traversal.</param>
        /// <param name="anonymous">Set to true if spawned from an anonymous traversal source and false otherwise.</param>
        private GraphTraversal(ICollection<ITraversalStrategy> traversalStrategies, Bytecode bytecode, bool anonymous)
        {
            TraversalStrategies = traversalStrategies;
            Bytecode = bytecode;
            IsAnonymous = anonymous;
        }

        private static GraphTraversal<S2, E2> Wrap<S2, E2>(GraphTraversal<S, E> traversal)
        {
            if (typeof(S2) == typeof(S) && typeof(E2) == typeof(E))
            {
                return traversal as GraphTraversal<S2, E2>;
            }
            // New wrapper
            return new GraphTraversal<S2, E2>(traversal.TraversalStrategies, traversal.Bytecode, traversal.IsAnonymous);
        }


        /// <summary>
        ///     Adds the V step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> V (params object[] vertexIdsOrElements)
        {
            var args = new List<object>(0 + vertexIdsOrElements.Length) {};
            args.AddRange(vertexIdsOrElements);
            Bytecode.AddStep("V", args.ToArray());
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the addE step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> AddE (string edgeLabel)
        {
            Bytecode.AddStep("addE", edgeLabel);
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the addE step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> AddE (ITraversal edgeLabelTraversal)
        {
            Bytecode.AddStep("addE", edgeLabelTraversal);
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the addV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> AddV ()
        {
            Bytecode.AddStep("addV");
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the addV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> AddV (string vertexLabel)
        {
            Bytecode.AddStep("addV", vertexLabel);
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the addV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> AddV (ITraversal vertexLabelTraversal)
        {
            Bytecode.AddStep("addV", vertexLabelTraversal);
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the aggregate step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Aggregate (Scope scope, string sideEffectKey)
        {
            Bytecode.AddStep("aggregate", scope, sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the aggregate step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Aggregate (string sideEffectKey)
        {
            Bytecode.AddStep("aggregate", sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the and step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> And (params ITraversal[] andTraversals)
        {
            var args = new List<object>(0 + andTraversals.Length) {};
            args.AddRange(andTraversals);
            Bytecode.AddStep("and", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the as step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> As (string stepLabel, params string[] stepLabels)
        {
            var args = new List<object>(1 + stepLabels.Length) {stepLabel};
            args.AddRange(stepLabels);
            Bytecode.AddStep("as", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the barrier step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Barrier ()
        {
            Bytecode.AddStep("barrier");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the barrier step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Barrier (IConsumer barrierConsumer)
        {
            Bytecode.AddStep("barrier", barrierConsumer);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the barrier step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Barrier (int maxBarrierSize)
        {
            Bytecode.AddStep("barrier", maxBarrierSize);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the both step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> Both (params string[] edgeLabels)
        {
            var args = new List<object>(0 + edgeLabels.Length) {};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("both", args.ToArray());
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the bothE step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> BothE (params string[] edgeLabels)
        {
            var args = new List<object>(0 + edgeLabels.Length) {};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("bothE", args.ToArray());
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the bothV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> BothV ()
        {
            Bytecode.AddStep("bothV");
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the branch step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Branch<E2> (IFunction function)
        {
            Bytecode.AddStep("branch", function);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the branch step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Branch<E2> (ITraversal branchTraversal)
        {
            Bytecode.AddStep("branch", branchTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By ()
        {
            Bytecode.AddStep("by");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (IComparator comparator)
        {
            Bytecode.AddStep("by", comparator);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (IFunction function)
        {
            Bytecode.AddStep("by", function);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (IFunction function, IComparator comparator)
        {
            Bytecode.AddStep("by", function, comparator);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (Order order)
        {
            Bytecode.AddStep("by", order);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (string key)
        {
            Bytecode.AddStep("by", key);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (string key, IComparator comparator)
        {
            Bytecode.AddStep("by", key, comparator);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (T token)
        {
            Bytecode.AddStep("by", token);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (ITraversal traversal)
        {
            Bytecode.AddStep("by", traversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the by step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> By (ITraversal traversal, IComparator comparator)
        {
            Bytecode.AddStep("by", traversal, comparator);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the cap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Cap<E2> (string sideEffectKey, params string[] sideEffectKeys)
        {
            var args = new List<object>(1 + sideEffectKeys.Length) {sideEffectKey};
            args.AddRange(sideEffectKeys);
            Bytecode.AddStep("cap", args.ToArray());
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the choose step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Choose<E2> (IFunction choiceFunction)
        {
            Bytecode.AddStep("choose", choiceFunction);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the choose step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Choose<E2> (IPredicate choosePredicate, ITraversal trueChoice)
        {
            Bytecode.AddStep("choose", choosePredicate, trueChoice);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the choose step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Choose<E2> (IPredicate choosePredicate, ITraversal trueChoice, ITraversal falseChoice)
        {
            Bytecode.AddStep("choose", choosePredicate, trueChoice, falseChoice);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the choose step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Choose<E2> (ITraversal choiceTraversal)
        {
            Bytecode.AddStep("choose", choiceTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the choose step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Choose<E2> (ITraversal traversalPredicate, ITraversal trueChoice)
        {
            Bytecode.AddStep("choose", traversalPredicate, trueChoice);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the choose step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Choose<E2> (ITraversal traversalPredicate, ITraversal trueChoice, ITraversal falseChoice)
        {
            Bytecode.AddStep("choose", traversalPredicate, trueChoice, falseChoice);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the coalesce step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Coalesce<E2> (params ITraversal[] coalesceTraversals)
        {
            var args = new List<object>(0 + coalesceTraversals.Length) {};
            args.AddRange(coalesceTraversals);
            Bytecode.AddStep("coalesce", args.ToArray());
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the coin step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Coin (double probability)
        {
            Bytecode.AddStep("coin", probability);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the connectedComponent step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> ConnectedComponent ()
        {
            Bytecode.AddStep("connectedComponent");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the constant step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Constant<E2> (E2 e)
        {
            Bytecode.AddStep("constant", e);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the count step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, long> Count ()
        {
            Bytecode.AddStep("count");
            return Wrap<S, long>(this);
        }

        /// <summary>
        ///     Adds the count step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, long> Count (Scope scope)
        {
            Bytecode.AddStep("count", scope);
            return Wrap<S, long>(this);
        }

        /// <summary>
        ///     Adds the cyclicPath step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> CyclicPath ()
        {
            Bytecode.AddStep("cyclicPath");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the dedup step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Dedup (Scope scope, params string[] dedupLabels)
        {
            var args = new List<object>(1 + dedupLabels.Length) {scope};
            args.AddRange(dedupLabels);
            Bytecode.AddStep("dedup", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the dedup step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Dedup (params string[] dedupLabels)
        {
            var args = new List<object>(0 + dedupLabels.Length) {};
            args.AddRange(dedupLabels);
            Bytecode.AddStep("dedup", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the drop step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Drop ()
        {
            Bytecode.AddStep("drop");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the elementMap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<object, E2>> ElementMap<E2> (params string[] propertyKeys)
        {
            var args = new List<object>(0 + propertyKeys.Length) {};
            args.AddRange(propertyKeys);
            Bytecode.AddStep("elementMap", args.ToArray());
            return Wrap<S, IDictionary<object, E2>>(this);
        }

        /// <summary>
        ///     Adds the emit step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Emit ()
        {
            Bytecode.AddStep("emit");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the emit step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Emit (IPredicate emitPredicate)
        {
            Bytecode.AddStep("emit", emitPredicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the emit step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Emit (ITraversal emitTraversal)
        {
            Bytecode.AddStep("emit", emitTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the filter step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Filter (IPredicate predicate)
        {
            Bytecode.AddStep("filter", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the filter step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Filter (ITraversal filterTraversal)
        {
            Bytecode.AddStep("filter", filterTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the flatMap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> FlatMap<E2> (IFunction function)
        {
            Bytecode.AddStep("flatMap", function);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the flatMap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> FlatMap<E2> (ITraversal flatMapTraversal)
        {
            Bytecode.AddStep("flatMap", flatMapTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the fold step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IList<E>> Fold ()
        {
            Bytecode.AddStep("fold");
            return Wrap<S, IList<E>>(this);
        }

        /// <summary>
        ///     Adds the fold step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Fold<E2> (E2 seed, IBiFunction foldFunction)
        {
            Bytecode.AddStep("fold", seed, foldFunction);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the from step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> From (string fromStepLabel)
        {
            Bytecode.AddStep("from", fromStepLabel);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the from step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> From (ITraversal fromVertex)
        {
            Bytecode.AddStep("from", fromVertex);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the from step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> From (Vertex fromVertex)
        {
            Bytecode.AddStep("from", fromVertex);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the group step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<K, V>> Group<K, V> ()
        {
            Bytecode.AddStep("group");
            return Wrap<S, IDictionary<K, V>>(this);
        }

        /// <summary>
        ///     Adds the group step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Group (string sideEffectKey)
        {
            Bytecode.AddStep("group", sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the groupCount step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<K, long>> GroupCount<K> ()
        {
            Bytecode.AddStep("groupCount");
            return Wrap<S, IDictionary<K, long>>(this);
        }

        /// <summary>
        ///     Adds the groupCount step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> GroupCount (string sideEffectKey)
        {
            Bytecode.AddStep("groupCount", sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (string propertyKey)
        {
            Bytecode.AddStep("has", propertyKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (string propertyKey, object value)
        {
            Bytecode.AddStep("has", propertyKey, value);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (string propertyKey, P predicate)
        {
            Bytecode.AddStep("has", propertyKey, predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (string label, string propertyKey, object value)
        {
            Bytecode.AddStep("has", label, propertyKey, value);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (string label, string propertyKey, P predicate)
        {
            Bytecode.AddStep("has", label, propertyKey, predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (string propertyKey, ITraversal propertyTraversal)
        {
            Bytecode.AddStep("has", propertyKey, propertyTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (T accessor, object value)
        {
            Bytecode.AddStep("has", accessor, value);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (T accessor, P predicate)
        {
            Bytecode.AddStep("has", accessor, predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the has step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Has (T accessor, ITraversal propertyTraversal)
        {
            Bytecode.AddStep("has", accessor, propertyTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasId step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasId (object id, params object[] otherIds)
        {
            var args = new List<object>(1 + otherIds.Length) {id};
            args.AddRange(otherIds);
            Bytecode.AddStep("hasId", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasId step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasId (P predicate)
        {
            Bytecode.AddStep("hasId", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasKey step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasKey (P predicate)
        {
            Bytecode.AddStep("hasKey", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasKey step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasKey (string label, params string[] otherLabels)
        {
            var args = new List<object>(1 + otherLabels.Length) {label};
            args.AddRange(otherLabels);
            Bytecode.AddStep("hasKey", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasLabel step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasLabel (P predicate)
        {
            Bytecode.AddStep("hasLabel", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasLabel step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasLabel (string label, params string[] otherLabels)
        {
            var args = new List<object>(1 + otherLabels.Length) {label};
            args.AddRange(otherLabels);
            Bytecode.AddStep("hasLabel", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasNot step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasNot (string propertyKey)
        {
            Bytecode.AddStep("hasNot", propertyKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasValue step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasValue (object value, params object[] otherValues)
        {
            var args = new List<object>(1 + otherValues.Length) {value};
            args.AddRange(otherValues);
            Bytecode.AddStep("hasValue", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the hasValue step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> HasValue (P predicate)
        {
            Bytecode.AddStep("hasValue", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the id step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, object> Id ()
        {
            Bytecode.AddStep("id");
            return Wrap<S, object>(this);
        }

        /// <summary>
        ///     Adds the identity step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Identity ()
        {
            Bytecode.AddStep("identity");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the in step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> In (params string[] edgeLabels)
        {
            var args = new List<object>(0 + edgeLabels.Length) {};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("in", args.ToArray());
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the inE step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> InE (params string[] edgeLabels)
        {
            var args = new List<object>(0 + edgeLabels.Length) {};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("inE", args.ToArray());
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the inV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> InV ()
        {
            Bytecode.AddStep("inV");
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the index step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Index<E2> ()
        {
            Bytecode.AddStep("index");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the inject step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Inject (params E[] injections)
        {
            var args = new List<object>(0 + injections.Length) {};
            args.AddRange(injections.Cast<object>());
            Bytecode.AddStep("inject", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the is step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Is (object value)
        {
            Bytecode.AddStep("is", value);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the is step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Is (P predicate)
        {
            Bytecode.AddStep("is", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the key step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, string> Key ()
        {
            Bytecode.AddStep("key");
            return Wrap<S, string>(this);
        }

        /// <summary>
        ///     Adds the label step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, string> Label ()
        {
            Bytecode.AddStep("label");
            return Wrap<S, string>(this);
        }

        /// <summary>
        ///     Adds the limit step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Limit<E2> (Scope scope, long limit)
        {
            Bytecode.AddStep("limit", scope, limit);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the limit step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Limit<E2> (long limit)
        {
            Bytecode.AddStep("limit", limit);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the local step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Local<E2> (ITraversal localTraversal)
        {
            Bytecode.AddStep("local", localTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the loops step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, int> Loops ()
        {
            Bytecode.AddStep("loops");
            return Wrap<S, int>(this);
        }

        /// <summary>
        ///     Adds the loops step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, int> Loops (string loopName)
        {
            Bytecode.AddStep("loops", loopName);
            return Wrap<S, int>(this);
        }

        /// <summary>
        ///     Adds the map step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Map<E2> (IFunction function)
        {
            Bytecode.AddStep("map", function);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the map step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Map<E2> (ITraversal mapTraversal)
        {
            Bytecode.AddStep("map", mapTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the match step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<string, E2>> Match<E2> (params ITraversal[] matchTraversals)
        {
            var args = new List<object>(0 + matchTraversals.Length) {};
            args.AddRange(matchTraversals);
            Bytecode.AddStep("match", args.ToArray());
            return Wrap<S, IDictionary<string, E2>>(this);
        }

        /// <summary>
        ///     Adds the math step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, double> Math (string expression)
        {
            Bytecode.AddStep("math", expression);
            return Wrap<S, double>(this);
        }

        /// <summary>
        ///     Adds the max step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Max<E2> ()
        {
            Bytecode.AddStep("max");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the max step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Max<E2> (Scope scope)
        {
            Bytecode.AddStep("max", scope);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the mean step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Mean<E2> ()
        {
            Bytecode.AddStep("mean");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the mean step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Mean<E2> (Scope scope)
        {
            Bytecode.AddStep("mean", scope);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the min step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Min<E2> ()
        {
            Bytecode.AddStep("min");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the min step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Min<E2> (Scope scope)
        {
            Bytecode.AddStep("min", scope);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the none step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> None ()
        {
            Bytecode.AddStep("none");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the not step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Not (ITraversal notTraversal)
        {
            Bytecode.AddStep("not", notTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the option step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Option (object pickToken, ITraversal traversalOption)
        {
            Bytecode.AddStep("option", pickToken, traversalOption);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the option step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Option (ITraversal traversalOption)
        {
            Bytecode.AddStep("option", traversalOption);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the optional step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Optional<E2> (ITraversal optionalTraversal)
        {
            Bytecode.AddStep("optional", optionalTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the or step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Or (params ITraversal[] orTraversals)
        {
            var args = new List<object>(0 + orTraversals.Length) {};
            args.AddRange(orTraversals);
            Bytecode.AddStep("or", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the order step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Order ()
        {
            Bytecode.AddStep("order");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the order step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Order (Scope scope)
        {
            Bytecode.AddStep("order", scope);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the otherV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> OtherV ()
        {
            Bytecode.AddStep("otherV");
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the out step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> Out (params string[] edgeLabels)
        {
            var args = new List<object>(0 + edgeLabels.Length) {};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("out", args.ToArray());
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the outE step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> OutE (params string[] edgeLabels)
        {
            var args = new List<object>(0 + edgeLabels.Length) {};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("outE", args.ToArray());
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the outV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> OutV ()
        {
            Bytecode.AddStep("outV");
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the pageRank step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> PageRank ()
        {
            Bytecode.AddStep("pageRank");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the pageRank step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> PageRank (double alpha)
        {
            Bytecode.AddStep("pageRank", alpha);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the path step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Path> Path ()
        {
            Bytecode.AddStep("path");
            return Wrap<S, Path>(this);
        }

        /// <summary>
        ///     Adds the peerPressure step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> PeerPressure ()
        {
            Bytecode.AddStep("peerPressure");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the percentile step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, object> Percentile (Scope scope, params int[] n)
        {
            var args = new List<object>(1 + n.Length) {scope};
            args.AddRange(n.Cast<object>());
            Bytecode.AddStep("percentile", args.ToArray());
            return Wrap<S, object>(this);
        }

        /// <summary>
        ///     Adds the percentile step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, object> Percentile (params int[] n)
        {
            var args = new List<object>(0 + n.Length) {};
            args.AddRange(n.Cast<object>());
            Bytecode.AddStep("percentile", args.ToArray());
            return Wrap<S, object>(this);
        }

        /// <summary>
        ///     Adds the profile step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Profile<E2> ()
        {
            Bytecode.AddStep("profile");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the profile step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Profile (string sideEffectKey)
        {
            Bytecode.AddStep("profile", sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the program step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Program (object vertexProgram)
        {
            Bytecode.AddStep("program", vertexProgram);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the project step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<string, E2>> Project<E2> (string projectKey, params string[] otherProjectKeys)
        {
            var args = new List<object>(1 + otherProjectKeys.Length) {projectKey};
            args.AddRange(otherProjectKeys);
            Bytecode.AddStep("project", args.ToArray());
            return Wrap<S, IDictionary<string, E2>>(this);
        }

        /// <summary>
        ///     Adds the properties step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Properties<E2> (params string[] propertyKeys)
        {
            var args = new List<object>(0 + propertyKeys.Length) {};
            args.AddRange(propertyKeys);
            Bytecode.AddStep("properties", args.ToArray());
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the property step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Property (Cardinality cardinality, object key, object value, params object[] keyValues)
        {
            var args = new List<object>(3 + keyValues.Length) {cardinality, key, value};
            args.AddRange(keyValues);
            Bytecode.AddStep("property", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the property step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Property (object key, object value, params object[] keyValues)
        {
            var args = new List<object>(2 + keyValues.Length) {key, value};
            args.AddRange(keyValues);
            Bytecode.AddStep("property", args.ToArray());
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the propertyMap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<string, E2>> PropertyMap<E2> (params string[] propertyKeys)
        {
            var args = new List<object>(0 + propertyKeys.Length) {};
            args.AddRange(propertyKeys);
            Bytecode.AddStep("propertyMap", args.ToArray());
            return Wrap<S, IDictionary<string, E2>>(this);
        }

        /// <summary>
        ///     Adds the range step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Range<E2> (Scope scope, long low, long high)
        {
            Bytecode.AddStep("range", scope, low, high);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the range step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Range<E2> (long low, long high)
        {
            Bytecode.AddStep("range", low, high);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the read step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Read ()
        {
            Bytecode.AddStep("read");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the repeat step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Repeat (string loopName, ITraversal repeatTraversal)
        {
            Bytecode.AddStep("repeat", loopName, repeatTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the repeat step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Repeat (ITraversal repeatTraversal)
        {
            Bytecode.AddStep("repeat", repeatTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the sack step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Sack<E2> ()
        {
            Bytecode.AddStep("sack");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the sack step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Sack (IBiFunction sackOperator)
        {
            Bytecode.AddStep("sack", sackOperator);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the sample step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Sample (Scope scope, int amountToSample)
        {
            Bytecode.AddStep("sample", scope, amountToSample);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the sample step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Sample (int amountToSample)
        {
            Bytecode.AddStep("sample", amountToSample);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, ICollection<E2>> Select<E2> (Column column)
        {
            Bytecode.AddStep("select", column);
            return Wrap<S, ICollection<E2>>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Select<E2> (Pop pop, string selectKey)
        {
            Bytecode.AddStep("select", pop, selectKey);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<string, E2>> Select<E2> (Pop pop, string selectKey1, string selectKey2, params string[] otherSelectKeys)
        {
            var args = new List<object>(3 + otherSelectKeys.Length) {pop, selectKey1, selectKey2};
            args.AddRange(otherSelectKeys);
            Bytecode.AddStep("select", args.ToArray());
            return Wrap<S, IDictionary<string, E2>>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Select<E2> (Pop pop, ITraversal keyTraversal)
        {
            Bytecode.AddStep("select", pop, keyTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Select<E2> (string selectKey)
        {
            Bytecode.AddStep("select", selectKey);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<string, E2>> Select<E2> (string selectKey1, string selectKey2, params string[] otherSelectKeys)
        {
            var args = new List<object>(2 + otherSelectKeys.Length) {selectKey1, selectKey2};
            args.AddRange(otherSelectKeys);
            Bytecode.AddStep("select", args.ToArray());
            return Wrap<S, IDictionary<string, E2>>(this);
        }

        /// <summary>
        ///     Adds the select step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Select<E2> (ITraversal keyTraversal)
        {
            Bytecode.AddStep("select", keyTraversal);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the shortestPath step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Path> ShortestPath ()
        {
            Bytecode.AddStep("shortestPath");
            return Wrap<S, Path>(this);
        }

        /// <summary>
        ///     Adds the sideEffect step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> SideEffect (IConsumer consumer)
        {
            Bytecode.AddStep("sideEffect", consumer);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the sideEffect step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> SideEffect (ITraversal sideEffectTraversal)
        {
            Bytecode.AddStep("sideEffect", sideEffectTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the simplePath step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> SimplePath ()
        {
            Bytecode.AddStep("simplePath");
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the skip step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Skip<E2> (Scope scope, long skip)
        {
            Bytecode.AddStep("skip", scope, skip);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the skip step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Skip<E2> (long skip)
        {
            Bytecode.AddStep("skip", skip);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the stdev step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Stdev<E2> ()
        {
            Bytecode.AddStep("stdev");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the stdev step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Stdev<E2> (Scope scope)
        {
            Bytecode.AddStep("stdev", scope);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the store step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Store (string sideEffectKey)
        {
            Bytecode.AddStep("store", sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the subgraph step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> Subgraph (string sideEffectKey)
        {
            Bytecode.AddStep("subgraph", sideEffectKey);
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the sum step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Sum<E2> ()
        {
            Bytecode.AddStep("sum");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the sum step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Sum<E2> (Scope scope)
        {
            Bytecode.AddStep("sum", scope);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the tail step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Tail<E2> ()
        {
            Bytecode.AddStep("tail");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the tail step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Tail<E2> (Scope scope)
        {
            Bytecode.AddStep("tail", scope);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the tail step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Tail<E2> (Scope scope, long limit)
        {
            Bytecode.AddStep("tail", scope, limit);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the tail step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Tail<E2> (long limit)
        {
            Bytecode.AddStep("tail", limit);
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the timeLimit step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> TimeLimit (long timeLimit)
        {
            Bytecode.AddStep("timeLimit", timeLimit);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the times step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Times (int maxLoops)
        {
            Bytecode.AddStep("times", maxLoops);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the to step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> To (Direction direction, params string[] edgeLabels)
        {
            var args = new List<object>(1 + edgeLabels.Length) {direction};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("to", args.ToArray());
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the to step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> To (string toStepLabel)
        {
            Bytecode.AddStep("to", toStepLabel);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the to step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> To (ITraversal toVertex)
        {
            Bytecode.AddStep("to", toVertex);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the to step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> To (Vertex toVertex)
        {
            Bytecode.AddStep("to", toVertex);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the toE step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Edge> ToE (Direction direction, params string[] edgeLabels)
        {
            var args = new List<object>(1 + edgeLabels.Length) {direction};
            args.AddRange(edgeLabels);
            Bytecode.AddStep("toE", args.ToArray());
            return Wrap<S, Edge>(this);
        }

        /// <summary>
        ///     Adds the toV step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, Vertex> ToV (Direction direction)
        {
            Bytecode.AddStep("toV", direction);
            return Wrap<S, Vertex>(this);
        }

        /// <summary>
        ///     Adds the tree step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Tree<E2> ()
        {
            Bytecode.AddStep("tree");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the tree step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Tree (string sideEffectKey)
        {
            Bytecode.AddStep("tree", sideEffectKey);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the unfold step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Unfold<E2> ()
        {
            Bytecode.AddStep("unfold");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the union step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Union<E2> (params ITraversal[] unionTraversals)
        {
            var args = new List<object>(0 + unionTraversals.Length) {};
            args.AddRange(unionTraversals);
            Bytecode.AddStep("union", args.ToArray());
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the until step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Until (IPredicate untilPredicate)
        {
            Bytecode.AddStep("until", untilPredicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the until step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Until (ITraversal untilTraversal)
        {
            Bytecode.AddStep("until", untilTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the value step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Value<E2> ()
        {
            Bytecode.AddStep("value");
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the valueMap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<TKey, TValue>> ValueMap<TKey, TValue> (params string[] propertyKeys)
        {
            var args = new List<object>(0 + propertyKeys.Length) {};
            args.AddRange(propertyKeys);
            Bytecode.AddStep("valueMap", args.ToArray());
            return Wrap<S, IDictionary<TKey, TValue>>(this);
        }

        /// <summary>
        ///     Adds the valueMap step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, IDictionary<TKey, TValue>> ValueMap<TKey, TValue> (bool includeTokens, params string[] propertyKeys)
        {
            var args = new List<object>(1 + propertyKeys.Length) {includeTokens};
            args.AddRange(propertyKeys);
            Bytecode.AddStep("valueMap", args.ToArray());
            return Wrap<S, IDictionary<TKey, TValue>>(this);
        }

        /// <summary>
        ///     Adds the values step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E2> Values<E2> (params string[] propertyKeys)
        {
            var args = new List<object>(0 + propertyKeys.Length) {};
            args.AddRange(propertyKeys);
            Bytecode.AddStep("values", args.ToArray());
            return Wrap<S, E2>(this);
        }

        /// <summary>
        ///     Adds the where step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Where (P predicate)
        {
            Bytecode.AddStep("where", predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the where step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Where (string startKey, P predicate)
        {
            Bytecode.AddStep("where", startKey, predicate);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the where step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Where (ITraversal whereTraversal)
        {
            Bytecode.AddStep("where", whereTraversal);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the with step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> With (string key)
        {
            Bytecode.AddStep("with", key);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the with step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> With (string key, object value)
        {
            Bytecode.AddStep("with", key, value);
            return Wrap<S, E>(this);
        }

        /// <summary>
        ///     Adds the write step to this <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public GraphTraversal<S, E> Write ()
        {
            Bytecode.AddStep("write");
            return Wrap<S, E>(this);
        }


        /// <summary>
        /// Make a copy of a traversal that is reset for iteration.
        /// </summary>
        public GraphTraversal<S, E> Clone()
        {
            return new GraphTraversal<S, E>(TraversalStrategies, new Bytecode(Bytecode));
        }
    }
}
