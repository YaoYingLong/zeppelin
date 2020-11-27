/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.elasticsearch.action;

/**
 * Contains the result of an aggregation.
 */
public class AggWrapper {
    private final AggregationType type;

    ;
    private final String result;
    public AggWrapper(AggregationType type, String result) {
        this.type = type;
        this.result = result;
    }

    public AggregationType getType() {
        return type;
    }

    public String getResult() {
        return result;
    }

    /**
     * Type of an aggregation (to know if there are buckets or not).
     */
    public enum AggregationType {SIMPLE, MULTI_BUCKETS}
}
