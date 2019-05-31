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

using System;
using System.Collections.Generic;
using System.Net.WebSockets;
using System.Threading.Tasks;
using Gremlin.Net.Driver.Messages;
using Gremlin.Net.Structure.IO.GraphSON;

namespace Gremlin.Net.Driver
{
    /// <summary>
    ///     Provides a mechanism for submitting Gremlin requests to one Gremlin Server.
    /// </summary>
    public class GremlinClient : IGremlinClient
    {
        /// <summary>
        /// Defines the default mime type to use.
        /// </summary>
        public const string DefaultMimeType = "application/vnd.gremlin-v3.0+json";

        /// <summary>
        /// The GraphSON2 mime type to use.
        /// </summary>
        public const string GraphSON2MimeType = "application/vnd.gremlin-v2.0+json";

        private readonly ConnectionPool _connectionPool;

        /// <summary>
        ///     Initializes a new instance of the <see cref="GremlinClient" /> class for the specified Gremlin Server.
        /// </summary>
        /// <param name="gremlinServer">The <see cref="GremlinServer" /> the requests should be sent to.</param>
        /// <param name="graphSONReader">A <see cref="GraphSONReader" /> instance to read received GraphSON data.</param>
        /// <param name="graphSONWriter">a <see cref="GraphSONWriter" /> instance to write GraphSON data.</param>
        /// <param name="mimeType">The GraphSON version mime type, defaults to latest supported by the server.</param>
        /// <param name="connectionPoolSettings">The <see cref="ConnectionPoolSettings"/> for the connection pool.</param>
        /// <param name="webSocketConfiguration">
        ///     A delegate that will be invoked with the <see cref="ClientWebSocketOptions" />
        ///     object used to configure WebSocket connections.
        /// </param>
        public GremlinClient(GremlinServer gremlinServer, GraphSONReader graphSONReader = null,
            GraphSONWriter graphSONWriter = null, string mimeType = null,
            ConnectionPoolSettings connectionPoolSettings = null,
            Action<ClientWebSocketOptions> webSocketConfiguration = null)
        {
            var reader = graphSONReader ?? new GraphSON3Reader();
            var writer = graphSONWriter ?? new GraphSON3Writer();
            var connectionFactory = new ConnectionFactory(gremlinServer, reader, writer, mimeType ?? DefaultMimeType,
                webSocketConfiguration);
            _connectionPool =
                new ConnectionPool(connectionFactory, connectionPoolSettings ?? new ConnectionPoolSettings());
        }
        /// <summary>
        ///   Connection Count
        /// </summary>
        public int NrConnections => _connectionPool.NrConnections;
        /// <summary>
        ///   Exposes the first available connection on the client
        /// </summary>
        public Connection FirstAvailableConnection => _connectionPool.FirstConnection;


        /// <inheritdoc />
        public async Task<ResultSet<T>> SubmitAsync<T>(RequestMessage requestMessage)
        {
            using (var connection = await _connectionPool.GetAvailableConnectionAsync().ConfigureAwait(false))
            {
                return await connection.SubmitAsync<T>(requestMessage).ConfigureAwait(false);
            }
        }

        #region IDisposable Support

        private bool _disposed;

        /// <inheritdoc />
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        /// <summary>
        ///     Releases the resources used by the <see cref="GremlinClient" /> instance.
        /// </summary>
        /// <param name="disposing">Specifies whether managed resources should be released.</param>
        protected virtual void Dispose(bool disposing)
        {
            if (!_disposed)
            {
                if (disposing)
                    _connectionPool?.Dispose();
                _disposed = true;
            }
        }

        #endregion
    }
}