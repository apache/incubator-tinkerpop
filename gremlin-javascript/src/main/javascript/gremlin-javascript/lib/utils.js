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
 * A module containing any utility functions.
 * @author Jorge Bay Gondra
 */
'use strict';

const uuidv4 = require('uuid/v4');

exports.toLong = function toLong(value) {
  return new Long(value);
};

const Long = exports.Long = function Long(value) {
  if (typeof value !== 'string' && typeof value !== 'number') {
    throw new TypeError('The value must be a string or a number');
  }
  this.value = value.toString();
};

exports.getUuid = function getUuid() {
  return uuidv4();
};

exports.emptyArray = Object.freeze([]);

class ImmutableMap extends Map {
  constructor(iterable) {
    super(iterable);
  }

  set(){
    return this;
  }

  ['delete'](){
    return false;
  }

  clear() { }
}

exports.ImmutableMap = ImmutableMap;
