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
using Gremlin.Net.Structure;

namespace Gremlin.Net.Process.Traversal
{
    /// <summary>
    ///     An anonymous <see cref="GraphTraversal{SType, EType}" />.
    /// </summary>
    public static class __
    {
        /// <summary>
        ///     Starts an empty <see cref="GraphTraversal{SType, EType}" />.
        /// </summary>
        public static GraphTraversal<object, object> Start()
        {
            return new GraphTraversal<object, object>();
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the V step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> V(params object[] vertexIdsOrElements)
        {
            return vertexIdsOrElements.Length == 0
                ? new GraphTraversal<object, Vertex>().V()
                : new GraphTraversal<object, Vertex>().V(vertexIdsOrElements);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the addE step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> AddE(string edgeLabel)
        {
            return new GraphTraversal<object, Edge>().AddE(edgeLabel);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the addE step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> AddE(ITraversal edgeLabelTraversal)
        {
            return new GraphTraversal<object, Edge>().AddE(edgeLabelTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the addV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> AddV()
        {
            return new GraphTraversal<object, Vertex>().AddV();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the addV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> AddV(string vertexLabel)
        {
            return new GraphTraversal<object, Vertex>().AddV(vertexLabel);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the addV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> AddV(ITraversal vertexLabelTraversal)
        {
            return new GraphTraversal<object, Vertex>().AddV(vertexLabelTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the aggregate step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Aggregate(Scope scope, string sideEffectKey)
        {
            return new GraphTraversal<object, object>().Aggregate(scope, sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the aggregate step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Aggregate(string sideEffectKey)
        {
            return new GraphTraversal<object, object>().Aggregate(sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the and step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> And(params ITraversal[] andTraversals)
        {
            return andTraversals.Length == 0
                ? new GraphTraversal<object, object>().And()
                : new GraphTraversal<object, object>().And(andTraversals);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the as step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> As(string label, params string[] labels)
        {
            return labels.Length == 0
                ? new GraphTraversal<object, object>().As(label)
                : new GraphTraversal<object, object>().As(label, labels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the barrier step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Barrier()
        {
            return new GraphTraversal<object, object>().Barrier();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the barrier step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Barrier(IConsumer barrierConsumer)
        {
            return new GraphTraversal<object, object>().Barrier(barrierConsumer);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the barrier step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Barrier(int maxBarrierSize)
        {
            return new GraphTraversal<object, object>().Barrier(maxBarrierSize);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the both step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> Both(params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Vertex>().Both()
                : new GraphTraversal<object, Vertex>().Both(edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the bothE step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> BothE(params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Edge>().BothE()
                : new GraphTraversal<object, Edge>().BothE(edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the bothV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> BothV()
        {
            return new GraphTraversal<object, Vertex>().BothV();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the branch step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Branch<E2>(IFunction function)
        {
            return new GraphTraversal<object, E2>().Branch<E2>(function);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the branch step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Branch<E2>(ITraversal traversalFunction)
        {
            return new GraphTraversal<object, E2>().Branch<E2>(traversalFunction);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the cap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Cap<E2>(string sideEffectKey, params string[] sideEffectKeys)
        {
            return sideEffectKeys.Length == 0
                ? new GraphTraversal<object, E2>().Cap<E2>(sideEffectKey)
                : new GraphTraversal<object, E2>().Cap<E2>(sideEffectKey, sideEffectKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the choose step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Choose<E2>(IFunction choiceFunction)
        {
            return new GraphTraversal<object, E2>().Choose<E2>(choiceFunction);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the choose step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Choose<E2>(IPredicate choosePredicate, ITraversal trueChoice)
        {
            return new GraphTraversal<object, E2>().Choose<E2>(choosePredicate, trueChoice);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the choose step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Choose<E2>(IPredicate choosePredicate, ITraversal trueChoice, ITraversal falseChoice)
        {
            return new GraphTraversal<object, E2>().Choose<E2>(choosePredicate, trueChoice, falseChoice);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the choose step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Choose<E2>(ITraversal traversalFunction)
        {
            return new GraphTraversal<object, E2>().Choose<E2>(traversalFunction);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the choose step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Choose<E2>(ITraversal traversalPredicate, ITraversal trueChoice)
        {
            return new GraphTraversal<object, E2>().Choose<E2>(traversalPredicate, trueChoice);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the choose step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Choose<E2>(ITraversal traversalPredicate, ITraversal trueChoice, ITraversal falseChoice)
        {
            return new GraphTraversal<object, E2>().Choose<E2>(traversalPredicate, trueChoice, falseChoice);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the coalesce step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Coalesce<E2>(params ITraversal[] traversals)
        {
            return traversals.Length == 0
                ? new GraphTraversal<object, E2>().Coalesce<E2>()
                : new GraphTraversal<object, E2>().Coalesce<E2>(traversals);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the coin step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Coin(double probability)
        {
            return new GraphTraversal<object, object>().Coin(probability);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the constant step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Constant<E2>(E2 a)
        {
            return new GraphTraversal<object, E2>().Constant<E2>(a);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the count step to that traversal.
        /// </summary>
        public static GraphTraversal<object, long> Count()
        {
            return new GraphTraversal<object, long>().Count();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the count step to that traversal.
        /// </summary>
        public static GraphTraversal<object, long> Count(Scope scope)
        {
            return new GraphTraversal<object, long>().Count(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the cyclicPath step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> CyclicPath()
        {
            return new GraphTraversal<object, object>().CyclicPath();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the dedup step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Dedup(Scope scope, params string[] dedupLabels)
        {
            return dedupLabels.Length == 0
                ? new GraphTraversal<object, object>().Dedup(scope)
                : new GraphTraversal<object, object>().Dedup(scope, dedupLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the dedup step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Dedup(params string[] dedupLabels)
        {
            return dedupLabels.Length == 0
                ? new GraphTraversal<object, object>().Dedup()
                : new GraphTraversal<object, object>().Dedup(dedupLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the drop step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Drop()
        {
            return new GraphTraversal<object, object>().Drop();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the elementMap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<object, E2>> ElementMap<E2>(params string[] propertyKeys)
        {
            return propertyKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<object, E2>>().ElementMap<E2>()
                : new GraphTraversal<object, IDictionary<object, E2>>().ElementMap<E2>(propertyKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the emit step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Emit()
        {
            return new GraphTraversal<object, object>().Emit();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the emit step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Emit(IPredicate emitPredicate)
        {
            return new GraphTraversal<object, object>().Emit(emitPredicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the emit step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Emit(ITraversal emitTraversal)
        {
            return new GraphTraversal<object, object>().Emit(emitTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the filter step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Filter(IPredicate predicate)
        {
            return new GraphTraversal<object, object>().Filter(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the filter step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Filter(ITraversal filterTraversal)
        {
            return new GraphTraversal<object, object>().Filter(filterTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the flatMap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> FlatMap<E2>(IFunction function)
        {
            return new GraphTraversal<object, E2>().FlatMap<E2>(function);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the flatMap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> FlatMap<E2>(ITraversal flatMapTraversal)
        {
            return new GraphTraversal<object, E2>().FlatMap<E2>(flatMapTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the fold step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IList<E2>> Fold<E2>()
        {
            return new GraphTraversal<object, E2>().Fold();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the fold step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Fold<E2>(E2 seed, IBiFunction foldFunction)
        {
            return new GraphTraversal<object, E2>().Fold<E2>(seed, foldFunction);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the group step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<K, V>> Group<K, V>()
        {
            return new GraphTraversal<object, IDictionary<K, V>>().Group<K, V>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the group step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Group(string sideEffectKey)
        {
            return new GraphTraversal<object, object>().Group(sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the groupCount step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<K, long>> GroupCount<K>()
        {
            return new GraphTraversal<object, IDictionary<K, long>>().GroupCount<K>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the groupCount step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> GroupCount(string sideEffectKey)
        {
            return new GraphTraversal<object, object>().GroupCount(sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(string propertyKey)
        {
            return new GraphTraversal<object, object>().Has(propertyKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(string propertyKey, object value)
        {
            return new GraphTraversal<object, object>().Has(propertyKey, value);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(string propertyKey, P predicate)
        {
            return new GraphTraversal<object, object>().Has(propertyKey, predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(string label, string propertyKey, object value)
        {
            return new GraphTraversal<object, object>().Has(label, propertyKey, value);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(string label, string propertyKey, P predicate)
        {
            return new GraphTraversal<object, object>().Has(label, propertyKey, predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(string propertyKey, ITraversal propertyTraversal)
        {
            return new GraphTraversal<object, object>().Has(propertyKey, propertyTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(T accessor, object value)
        {
            return new GraphTraversal<object, object>().Has(accessor, value);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(T accessor, P predicate)
        {
            return new GraphTraversal<object, object>().Has(accessor, predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the has step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Has(T accessor, ITraversal propertyTraversal)
        {
            return new GraphTraversal<object, object>().Has(accessor, propertyTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasId step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasId(object id, params object[] otherIds)
        {
            return otherIds.Length == 0
                ? new GraphTraversal<object, object>().HasId(id)
                : new GraphTraversal<object, object>().HasId(id, otherIds);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasId step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasId(P predicate)
        {
            return new GraphTraversal<object, object>().HasId(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasKey step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasKey(P predicate)
        {
            return new GraphTraversal<object, object>().HasKey(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasKey step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasKey(string label, params string[] otherLabels)
        {
            return otherLabels.Length == 0
                ? new GraphTraversal<object, object>().HasKey(label)
                : new GraphTraversal<object, object>().HasKey(label, otherLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasLabel step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasLabel(P predicate)
        {
            return new GraphTraversal<object, object>().HasLabel(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasLabel step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasLabel(string label, params string[] otherLabels)
        {
            return otherLabels.Length == 0
                ? new GraphTraversal<object, object>().HasLabel(label)
                : new GraphTraversal<object, object>().HasLabel(label, otherLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasNot step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasNot(string propertyKey)
        {
            return new GraphTraversal<object, object>().HasNot(propertyKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasValue step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasValue(object value, params object[] values)
        {
            return values.Length == 0
                ? new GraphTraversal<object, object>().HasValue(value)
                : new GraphTraversal<object, object>().HasValue(value, values);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the hasValue step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> HasValue(P predicate)
        {
            return new GraphTraversal<object, object>().HasValue(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the id step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Id()
        {
            return new GraphTraversal<object, object>().Id();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the identity step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Identity()
        {
            return new GraphTraversal<object, object>().Identity();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the in step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> In(params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Vertex>().In()
                : new GraphTraversal<object, Vertex>().In(edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the inE step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> InE(params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Edge>().InE()
                : new GraphTraversal<object, Edge>().InE(edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the inV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> InV()
        {
            return new GraphTraversal<object, Vertex>().InV();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the index step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Index<E2>()
        {
            return new GraphTraversal<object, E2>().Index<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the inject step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Inject(params object[] injections)
        {
            return injections.Length == 0
                ? new GraphTraversal<object, object>().Inject()
                : new GraphTraversal<object, object>().Inject(injections);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the is step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Is(object value)
        {
            return new GraphTraversal<object, object>().Is(value);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the is step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Is(P predicate)
        {
            return new GraphTraversal<object, object>().Is(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the key step to that traversal.
        /// </summary>
        public static GraphTraversal<object, string> Key()
        {
            return new GraphTraversal<object, string>().Key();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the label step to that traversal.
        /// </summary>
        public static GraphTraversal<object, string> Label()
        {
            return new GraphTraversal<object, string>().Label();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the limit step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Limit<E2>(Scope scope, long limit)
        {
            return new GraphTraversal<object, E2>().Limit<E2>(scope, limit);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the limit step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Limit<E2>(long limit)
        {
            return new GraphTraversal<object, E2>().Limit<E2>(limit);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the local step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Local<E2>(ITraversal localTraversal)
        {
            return new GraphTraversal<object, E2>().Local<E2>(localTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the loops step to that traversal.
        /// </summary>
        public static GraphTraversal<object, int> Loops()
        {
            return new GraphTraversal<object, int>().Loops();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the loops step to that traversal.
        /// </summary>
        public static GraphTraversal<object, int> Loops(string loopName)
        {
            return new GraphTraversal<object, int>().Loops(loopName);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the map step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Map<E2>(IFunction function)
        {
            return new GraphTraversal<object, E2>().Map<E2>(function);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the map step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Map<E2>(ITraversal mapTraversal)
        {
            return new GraphTraversal<object, E2>().Map<E2>(mapTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the match step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<string, E2>> Match<E2>(params ITraversal[] matchTraversals)
        {
            return matchTraversals.Length == 0
                ? new GraphTraversal<object, IDictionary<string, E2>>().Match<E2>()
                : new GraphTraversal<object, IDictionary<string, E2>>().Match<E2>(matchTraversals);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the math step to that traversal.
        /// </summary>
        public static GraphTraversal<object, double> Math(string expression)
        {
            return new GraphTraversal<object, double>().Math(expression);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the max step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Max<E2>()
        {
            return new GraphTraversal<object, E2>().Max<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the max step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Max<E2>(Scope scope)
        {
            return new GraphTraversal<object, E2>().Max<E2>(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the mean step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Mean<E2>()
        {
            return new GraphTraversal<object, E2>().Mean<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the mean step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Mean<E2>(Scope scope)
        {
            return new GraphTraversal<object, E2>().Mean<E2>(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the min step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Min<E2>()
        {
            return new GraphTraversal<object, E2>().Min<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the min step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Min<E2>(Scope scope)
        {
            return new GraphTraversal<object, E2>().Min<E2>(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the not step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Not(ITraversal notTraversal)
        {
            return new GraphTraversal<object, object>().Not(notTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the optional step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Optional<E2>(ITraversal optionalTraversal)
        {
            return new GraphTraversal<object, E2>().Optional<E2>(optionalTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the or step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Or(params ITraversal[] orTraversals)
        {
            return orTraversals.Length == 0
                ? new GraphTraversal<object, object>().Or()
                : new GraphTraversal<object, object>().Or(orTraversals);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the order step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Order()
        {
            return new GraphTraversal<object, object>().Order();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the order step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Order(Scope scope)
        {
            return new GraphTraversal<object, object>().Order(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the otherV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> OtherV()
        {
            return new GraphTraversal<object, Vertex>().OtherV();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the out step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> Out(params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Vertex>().Out()
                : new GraphTraversal<object, Vertex>().Out(edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the outE step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> OutE(params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Edge>().OutE()
                : new GraphTraversal<object, Edge>().OutE(edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the outV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> OutV()
        {
            return new GraphTraversal<object, Vertex>().OutV();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the path step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Path> Path()
        {
            return new GraphTraversal<object, Path>().Path();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the percentile step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Percentile(Scope scope, params int[] n)
        {
            return n.Length == 0
                ? new GraphTraversal<object, object>().Percentile(scope)
                : new GraphTraversal<object, object>().Percentile(scope, n);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the percentile step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Percentile(params int[] n)
        {
            return n.Length == 0
                ? new GraphTraversal<object, object>().Percentile()
                : new GraphTraversal<object, object>().Percentile(n);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the project step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<string, E2>> Project<E2>(string projectKey, params string[] projectKeys)
        {
            return projectKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<string, E2>>().Project<E2>(projectKey)
                : new GraphTraversal<object, IDictionary<string, E2>>().Project<E2>(projectKey, projectKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the properties step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Properties<E2>(params string[] propertyKeys)
        {
            return propertyKeys.Length == 0
                ? new GraphTraversal<object, E2>().Properties<E2>()
                : new GraphTraversal<object, E2>().Properties<E2>(propertyKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the property step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Property(Cardinality cardinality, object key, object value, params object[] keyValues)
        {
            return keyValues.Length == 0
                ? new GraphTraversal<object, object>().Property(cardinality, key, value)
                : new GraphTraversal<object, object>().Property(cardinality, key, value, keyValues);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the property step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Property(object key, object value, params object[] keyValues)
        {
            return keyValues.Length == 0
                ? new GraphTraversal<object, object>().Property(key, value)
                : new GraphTraversal<object, object>().Property(key, value, keyValues);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the propertyMap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<string, E2>> PropertyMap<E2>(params string[] propertyKeys)
        {
            return propertyKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<string, E2>>().PropertyMap<E2>()
                : new GraphTraversal<object, IDictionary<string, E2>>().PropertyMap<E2>(propertyKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the range step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Range<E2>(Scope scope, long low, long high)
        {
            return new GraphTraversal<object, E2>().Range<E2>(scope, low, high);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the range step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Range<E2>(long low, long high)
        {
            return new GraphTraversal<object, E2>().Range<E2>(low, high);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the repeat step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Repeat(string loopName, ITraversal traversal)
        {
            return new GraphTraversal<object, object>().Repeat(loopName, traversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the repeat step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Repeat(ITraversal traversal)
        {
            return new GraphTraversal<object, object>().Repeat(traversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sack step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Sack<E2>()
        {
            return new GraphTraversal<object, E2>().Sack<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sack step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Sack(IBiFunction sackOperator)
        {
            return new GraphTraversal<object, object>().Sack(sackOperator);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sample step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Sample(Scope scope, int amountToSample)
        {
            return new GraphTraversal<object, object>().Sample(scope, amountToSample);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sample step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Sample(int amountToSample)
        {
            return new GraphTraversal<object, object>().Sample(amountToSample);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, ICollection<E2>> Select<E2>(Column column)
        {
            return new GraphTraversal<object, ICollection<E2>>().Select<E2>(column);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Select<E2>(Pop pop, string selectKey)
        {
            return new GraphTraversal<object, E2>().Select<E2>(pop, selectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<string, E2>> Select<E2>(Pop pop, string selectKey1, string selectKey2, params string[] otherSelectKeys)
        {
            return otherSelectKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<string, E2>>().Select<E2>(pop, selectKey1, selectKey2)
                : new GraphTraversal<object, IDictionary<string, E2>>().Select<E2>(pop, selectKey1, selectKey2, otherSelectKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Select<E2>(Pop pop, ITraversal keyTraversal)
        {
            return new GraphTraversal<object, E2>().Select<E2>(pop, keyTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Select<E2>(string selectKey)
        {
            return new GraphTraversal<object, E2>().Select<E2>(selectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<string, E2>> Select<E2>(string selectKey1, string selectKey2, params string[] otherSelectKeys)
        {
            return otherSelectKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<string, E2>>().Select<E2>(selectKey1, selectKey2)
                : new GraphTraversal<object, IDictionary<string, E2>>().Select<E2>(selectKey1, selectKey2, otherSelectKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the select step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Select<E2>(ITraversal keyTraversal)
        {
            return new GraphTraversal<object, E2>().Select<E2>(keyTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sideEffect step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> SideEffect(IConsumer consumer)
        {
            return new GraphTraversal<object, object>().SideEffect(consumer);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sideEffect step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> SideEffect(ITraversal sideEffectTraversal)
        {
            return new GraphTraversal<object, object>().SideEffect(sideEffectTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the simplePath step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> SimplePath()
        {
            return new GraphTraversal<object, object>().SimplePath();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the skip step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Skip<E2>(Scope scope, long skip)
        {
            return new GraphTraversal<object, E2>().Skip<E2>(scope, skip);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the skip step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Skip<E2>(long skip)
        {
            return new GraphTraversal<object, E2>().Skip<E2>(skip);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the stdev step to that traversal.
        /// </summary>
        public static GraphTraversal<object, double> Stdev()
        {
            return new GraphTraversal<object, double>().Stdev<double>();
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the stdev step to that traversal.
        /// </summary>
        public static GraphTraversal<object, double> Stdev(Scope scope)
        {
            return new GraphTraversal<object, double>().Stdev<double>(scope);
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the store step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Store(string sideEffectKey)
        {
            return new GraphTraversal<object, object>().Store(sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the subgraph step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> Subgraph(string sideEffectKey)
        {
            return new GraphTraversal<object, Edge>().Subgraph(sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sum step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Sum<E2>()
        {
            return new GraphTraversal<object, E2>().Sum<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the sum step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Sum<E2>(Scope scope)
        {
            return new GraphTraversal<object, E2>().Sum<E2>(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the tail step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Tail<E2>()
        {
            return new GraphTraversal<object, E2>().Tail<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the tail step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Tail<E2>(Scope scope)
        {
            return new GraphTraversal<object, E2>().Tail<E2>(scope);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the tail step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Tail<E2>(Scope scope, long limit)
        {
            return new GraphTraversal<object, E2>().Tail<E2>(scope, limit);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the tail step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Tail<E2>(long limit)
        {
            return new GraphTraversal<object, E2>().Tail<E2>(limit);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the timeLimit step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> TimeLimit(long timeLimit)
        {
            return new GraphTraversal<object, object>().TimeLimit(timeLimit);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the times step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Times(int maxLoops)
        {
            return new GraphTraversal<object, object>().Times(maxLoops);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the to step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> To(Direction direction, params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Vertex>().To(direction)
                : new GraphTraversal<object, Vertex>().To(direction, edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the toE step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Edge> ToE(Direction direction, params string[] edgeLabels)
        {
            return edgeLabels.Length == 0
                ? new GraphTraversal<object, Edge>().ToE(direction)
                : new GraphTraversal<object, Edge>().ToE(direction, edgeLabels);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the toV step to that traversal.
        /// </summary>
        public static GraphTraversal<object, Vertex> ToV(Direction direction)
        {
            return new GraphTraversal<object, Vertex>().ToV(direction);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the tree step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Tree<E2>()
        {
            return new GraphTraversal<object, E2>().Tree<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the tree step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Tree(string sideEffectKey)
        {
            return new GraphTraversal<object, object>().Tree(sideEffectKey);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the unfold step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Unfold<E2>()
        {
            return new GraphTraversal<object, E2>().Unfold<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the union step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Union<E2>(params ITraversal[] traversals)
        {
            return traversals.Length == 0
                ? new GraphTraversal<object, E2>().Union<E2>()
                : new GraphTraversal<object, E2>().Union<E2>(traversals);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the until step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Until(IPredicate untilPredicate)
        {
            return new GraphTraversal<object, object>().Until(untilPredicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the until step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Until(ITraversal untilTraversal)
        {
            return new GraphTraversal<object, object>().Until(untilTraversal);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the value step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Value<E2>()
        {
            return new GraphTraversal<object, E2>().Value<E2>();            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the valueMap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<TKey, TValue>> ValueMap<TKey, TValue>(params string[] propertyKeys)
        {
            return propertyKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<TKey, TValue>>().ValueMap<TKey, TValue>()
                : new GraphTraversal<object, IDictionary<TKey, TValue>>().ValueMap<TKey, TValue>(propertyKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the valueMap step to that traversal.
        /// </summary>
        public static GraphTraversal<object, IDictionary<TKey, TValue>> ValueMap<TKey, TValue>(bool includeTokens, params string[] propertyKeys)
        {
            return propertyKeys.Length == 0
                ? new GraphTraversal<object, IDictionary<TKey, TValue>>().ValueMap<TKey, TValue>(includeTokens)
                : new GraphTraversal<object, IDictionary<TKey, TValue>>().ValueMap<TKey, TValue>(includeTokens, propertyKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the values step to that traversal.
        /// </summary>
        public static GraphTraversal<object, E2> Values<E2>(params string[] propertyKeys)
        {
            return propertyKeys.Length == 0
                ? new GraphTraversal<object, E2>().Values<E2>()
                : new GraphTraversal<object, E2>().Values<E2>(propertyKeys);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the where step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Where(P predicate)
        {
            return new GraphTraversal<object, object>().Where(predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the where step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Where(string startKey, P predicate)
        {
            return new GraphTraversal<object, object>().Where(startKey, predicate);            
        }

        /// <summary>
        ///     Spawns a <see cref="GraphTraversal{SType, EType}" /> and adds the where step to that traversal.
        /// </summary>
        public static GraphTraversal<object, object> Where(ITraversal whereTraversal)
        {
            return new GraphTraversal<object, object>().Where(whereTraversal);            
        }

    }
}