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

const EventEmitter = require('events');
//const WebSocket = require('ws');
//var Buffer = require('buffer');
const util = require('util');
const utils = require('../utils');
const serializer = require('../structure/io/graph-serializer');
const ResultSet = require('./result-set');
const ResponseError = require('./response-error');

const responseStatusCode = {
  success: 200,
  noContent: 204,
  partialContent: 206,
  authenticationChallenge:  407,
};

const defaultMimeType = 'application/vnd.gremlin-v3.0+json';
const graphSON2MimeType = 'application/vnd.gremlin-v2.0+json';

const pingIntervalDelay = 60 * 1000;
const pongTimeoutDelay = 30 * 1000;

/**
 * Represents a single connection to a Gremlin Server.
 */
class Connection extends EventEmitter {

  /**
   * Creates a new instance of {@link Connection}.
   * @param {String} url The resource uri.
   * @param {Object} [options] The connection options.
   * @param {Array} [options.ca] Trusted certificates.
   * @param {String|Array|Buffer} [options.cert] The certificate key.
   * @param {String} [options.mimeType] The mime type to use.
   * @param {String|Buffer} [options.pfx] The private key, certificate, and CA certs.
   * @param {GraphSONReader} [options.reader] The reader to use.
   * @param {Boolean} [options.rejectUnauthorized] Determines whether to verify or not the server certificate.
   * @param {String} [options.traversalSource] The traversal source. Defaults to: 'g'.
   * @param {GraphSONWriter} [options.writer] The writer to use.
   * @param {Authenticator} [options.authenticator] The authentication handler to use.
   * @param {Object} [options.headers] An associative array containing the additional header key/values for the initial request.
   * @param {Boolean} [options.pingEnabled] Setup ping interval. Defaults to: true.
   * @param {Number} [options.pingInterval] Ping request interval in ms if ping enabled. Defaults to: 60000.
   * @param {Number} [options.pongTimeout] Timeout of pong response in ms after sending a ping. Defaults to: 30000.
   * @param {Boolean} [options.connectOnStartup] Open websocket on startup. Defaults to: true.
   * @constructor
   */
  constructor(url, options) {
    super();

    this.url = url;
    this.options = options = options || {};

    /**
     * Gets the MIME type.
     * @type {String}
     */
    this.mimeType = options.mimeType || defaultMimeType;

    // A map containing the request id and the handler
    this._responseHandlers = {};
    this._reader = options.reader || this._getDefaultReader(this.mimeType);
    this._writer = options.writer || this._getDefaultWriter(this.mimeType);
    this._openPromise = null;
    this._openCallback = null;
    this._closePromise = null;
    this._closeCallback = null;
    this._pingInterval = null;
    this._pongTimeout = null;

    this._header = String.fromCharCode(this.mimeType.length) + this.mimeType;
    this.isOpen = false;
    this.traversalSource = options.traversalSource || 'g';
    this._authenticator = options.authenticator;

    this._pingEnabled = this.options.pingEnabled === false ? false : true;
    this._pingIntervalDelay = this.options.pingInterval || pingIntervalDelay;
    this._pongTimeoutDelay = this.options.pongTimeout || pongTimeoutDelay;

    if (this.options.connectOnStartup !== false) {
      this.open();
    }
  }

  /**
   * Opens the connection, if its not already opened.
   * @returns {Promise}
   */
  open() {
    if (this.isOpen) {
      return Promise.resolve();
    }
    if (this._openPromise) {
      return this._openPromise;
    }

    this.emit('log', `ws open`);

    this._ws = new WebSocket(this.url);

    this._ws.onmessage = (data => this._handleMessage(data));
    this._ws.onerror = (err => this._handleError(err));
    this._ws.onclose = (code, message) => this._handleClose(code, message);

    this._ws.pong = (() => {
      this.emit('log', 'ws pong received');
    if (this._pongTimeout) {
      clearTimeout(this._pongTimeout);
      this._pongTimeout = null;
    }
  });
    this._ws.ping = (() => {
      this.emit('log', 'ws ping received');
    this._ws.pong();
  });

    return this._openPromise = new Promise((resolve, reject) => {
      this._ws.onopen = (() => {
      this.isOpen = true;
    if (this._pingEnabled) {
      this._pingHeartbeat();
    }
    resolve();
  });
  });
  }

  /** @override */
  submit(bytecode, op, args, requestId, processor) {
    return this.open().then(() => new Promise((resolve, reject) => {
      if (requestId === null || requestId === undefined) {
      requestId = utils.getUuid();
      this._responseHandlers[requestId] = {
        callback: (err, result) => err ? reject(err) : resolve(result),
          result: null
    };
    }

    //const message = Buffer.from(this._header + JSON.stringify(this._getRequest(requestId, bytecode, op, args, processor)));
     const message = this._header + JSON.stringify(this._getRequest(requestId, bytecode, op, args, processor));
      var buf = new ArrayBuffer(message.length); // 2 bytes for each char
      var bufView = new Uint8Array(buf);
      for (var i=0, strLen=message.length; i < strLen; i++) {
        bufView[i] = message.charCodeAt(i);
      }
      this._ws.binaryType = 'arraybuffer';
    this._ws.send(bufView);
  }));
  }

  _getDefaultReader(mimeType) {
    return mimeType === graphSON2MimeType
      ? new serializer.GraphSON2Reader()
      : new serializer.GraphSONReader();
  }

  _getDefaultWriter(mimeType) {
    return mimeType === graphSON2MimeType
      ? new serializer.GraphSON2Writer()
      : new serializer.GraphSONWriter();
  }

  _getRequest(id, bytecode, op, args, processor) {
    if (args) {
      args = this._adaptArgs(args, true);
    }


    return ({
      'requestId': { '@type': 'g:UUID', '@value': id },
      'op': op || 'bytecode',
      // if using op eval need to ensure processor stays unset if caller didn't set it.
      'processor': (!processor && op !== 'eval') ? 'traversal' : processor,
      'args': args || {
        'gremlin': this._writer.adaptObject(bytecode),
        'aliases': { 'g': this.traversalSource }
      }
    });
  }

  _pingHeartbeat() {

    if (this._pingInterval) {
      clearInterval(this._pingInterval);
      this._pingInterval = null;
    }

    this._pingInterval = setInterval(() => {
      if (this.isOpen === false) {
      // in case of if not open..
      if (this._pingInterval) {
        clearInterval(this._pingInterval);
        this._pingInterval = null;
      }
    }

    this._pongTimeout = setTimeout(() => {
      this._ws.terminate();
  }, this._pongTimeoutDelay);

    this._ws.ping();

  }, this._pingIntervalDelay);
  }

  _handleError(err) {
    this.emit('log', `ws error ${err}`);
    this._cleanupWebsocket();
    this.emit('socketError', err);
  }

  _handleClose(code, message) {
    this.emit('log', `ws close code=${code} message=${message}`);
    this._cleanupWebsocket();
    if (this._closeCallback) {
      this._closeCallback();
    }
    this.emit('close', code, message);
  }

  _handleMessage(msg) {
    if(msg.data instanceof ArrayBuffer ) {
      //if in browser javascript, the data are sent as Uint8
      var data = String.fromCharCode.apply(null, new Uint8Array(msg.data));
    }else{
      data = msg;
    }
    const response = this._reader.read(JSON.parse(data));
    if (response.requestId === null || response.requestId === undefined) {
      // There was a serialization issue on the server that prevented the parsing of the request id
      // We invoke any of the pending handlers with an error
      Object.keys(this._responseHandlers).forEach(requestId => {
        const handler = this._responseHandlers[requestId];
        this._clearHandler(requestId);
        if (response.status !== undefined && response.status.message) {
          return handler.callback(
            // TINKERPOP-2285: keep the old server error message in case folks are parsing that - fix in a future breaking version
            new ResponseError(util.format(
              'Server error (no request information): %s (%d)', response.status.message, response.status.code), response.status));
        } else {
           // TINKERPOP-2285: keep the old server error message in case folks are parsing that - fix in a future breaking version
          return handler.callback(new ResponseError(util.format('Server error (no request information): %j', response), response.status));
        }
      });
      return;
    }

    const handler = this._responseHandlers[response.requestId];

    if (!handler) {
      // The handler for a given request id was not found
      // It was probably invoked earlier due to a serialization issue.
      return;
    }

    if (response.status.code === responseStatusCode.authenticationChallenge && this._authenticator) {
      this._authenticator.evaluateChallenge(response.result.data).then(res => {
        return this.submit(null, 'authentication', res, response.requestId);
    }).catch(handler.callback);

      return;
    }
    else if (response.status.code >= 400) {
      // callback in error
      return handler.callback(
        // TINKERPOP-2285: keep the old server error message in case folks are parsing that - fix in a future breaking version
        new ResponseError(util.format('Server error: %s (%d)', response.status.message, response.status.code), response.status));
    }
    switch (response.status.code) {
      case responseStatusCode.noContent:
        this._clearHandler(response.requestId);
        return handler.callback(null, new ResultSet(utils.emptyArray, response.status.attributes));
      case responseStatusCode.partialContent:
        handler.result = handler.result || [];
        handler.result.push.apply(handler.result, response.result.data);
        break;
      default:
        if (handler.result) {
          handler.result.push.apply(handler.result, response.result.data);
        }
        else {
          handler.result = response.result.data;
        }
        this._clearHandler(response.requestId);
        return handler.callback(null, new ResultSet(handler.result, response.status.attributes));
    }
  }

  /**
   * clean websocket context
   */
  _cleanupWebsocket() {
    if (this._pingInterval) {
      clearInterval(this._pingInterval);
    }
    this._pingInterval = null;
    if (this._pongTimeout) {
      clearTimeout(this._pongTimeout);
    }
    this._pongTimeout = null;

    this._ws.removeAllListeners();
    this._openPromise = null;
    this._closePromise = null;
    this.isOpen = false;
  }

  /**
   * Clears the internal state containing the callback and result buffer of a given request.
   * @param requestId
   * @private
   */
  _clearHandler(requestId) {
    delete this._responseHandlers[requestId];
  }

  /**
   * Takes the given args map and ensures all arguments are passed through to _write.adaptObject
   * @param {Object} args Map of arguments to process.
   * @param {Boolean} protocolLevel Determines whether it's a protocol level binding.
   * @returns {Object}
   * @private
   */
  _adaptArgs(args, protocolLevel) {
    if (args instanceof Object) {
      let newObj = {};
      Object.keys(args).forEach((key) => {
        // bindings key (at the protocol-level needs special handling. without this, it wraps the generated Map
        // in another map for types like EnumValue. Could be a nicer way to do this but for now it's solving the
        // problem with script submission of non JSON native types
        if (protocolLevel && key === 'bindings')
      newObj[key] = this._adaptArgs(args[key], false);
    else
      newObj[key] = this._writer.adaptObject(args[key]);
    });

      return newObj;
    }

    return args;
  }

  /**
   * Closes the Connection.
   * @return {Promise}
   */
  close() {
    if (this.isOpen === false) {
      return Promise.resolve();
    }
    if (!this._closePromise) {
      this._closePromise = new Promise(resolve => {
        this._closeCallback = resolve;
      this._ws.close();
    });
    }
    return this._closePromise;
  }
}

module.exports = Connection;
