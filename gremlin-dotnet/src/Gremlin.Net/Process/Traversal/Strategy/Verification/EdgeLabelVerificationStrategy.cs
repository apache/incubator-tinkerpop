﻿#region License

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

namespace Gremlin.Net.Process.Traversal.Strategy.Verification
{
    /// <summary>
    ///     Provides a way to prevent traversals that sub-optimally fail to include edge label specification .
    /// </summary>
    public class EdgeLabelVerificationStrategy : AbstractTraversalStrategy
    {
        private const string JavaFqcn = VerificationNamespace + nameof(EdgeLabelVerificationStrategy);

        /// <summary>
        ///     Initializes a new instance of the <see cref="EdgeLabelVerificationStrategy" /> class.
        /// </summary>
        public EdgeLabelVerificationStrategy() : base(JavaFqcn)
        {
        }

        /// <summary>
        ///     Initializes a new instance of the <see cref="EdgeLabelVerificationStrategy" /> class.
        /// </summary>
        /// <param name="logWarning">Constrains vertices for the <see cref="ITraversal" />.</param>
        /// <param name="throwException">Constrains edges for the <see cref="ITraversal" />.</param>
        public EdgeLabelVerificationStrategy(bool logWarning = false, bool throwException = false)
            : this()
        {
            Configuration["logWarning"] = logWarning;
            Configuration["throwException"] = throwException;
        }
    }
}