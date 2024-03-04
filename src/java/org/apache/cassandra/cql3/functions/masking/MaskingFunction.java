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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.cql3.functions.masking;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.ObjectArrays;

import org.apache.cassandra.cql3.functions.FunctionFactory;
import org.apache.cassandra.cql3.functions.FunctionName;
import org.apache.cassandra.cql3.functions.FunctionParameter;
import org.apache.cassandra.cql3.functions.NativeScalarFunction;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.transport.ProtocolVersion;

/**
 * A {@link NativeScalarFunction} that totally or partially replaces the original value of a column value,
 * meant to obscure the real value of the column.
 * <p>
 * The names of all masking functions share a common prefix, {@link MaskingFunction#NAME_PREFIX}, to easily identify
 * their purpose.
 */
public abstract class MaskingFunction extends NativeScalarFunction
{
    /** The common prefix for the names of all the native data masking functions. */
    public static final String NAME_PREFIX = "mask_";

    /**
     * @param name the name of the function
     * @param outputType the type of the values returned by the function
     * @param inputType the type of the values accepted by the function, always be the first argument of the function
     * @param argsType the type of the additional arguments of the function
     */
    protected MaskingFunction(FunctionName name,
                              AbstractType<?> outputType,
                              AbstractType<?> inputType,
                              AbstractType<?>... argsType)
    {
        super(name.name, outputType, ObjectArrays.concat(inputType, argsType));
    }

    @Override
    public final ByteBuffer execute(ProtocolVersion protocolVersion, List<ByteBuffer> parameters)
    {
        ByteBuffer[] partialParameters = new ByteBuffer[parameters.size() - 1];
        for (int i = 0; i < partialParameters.length; i++)
            partialParameters[i] = parameters.get(i + 1);

        return masker(partialParameters).mask(parameters.get(0));
    }

    /**
     * Returns a new {@link Masker} for the specified masking parameters.
     * This is meant to be used by {@link ColumnMask}, so it doesn't need to evaluate the arguments on every call.
     *
     * @param parameters the masking parameters in the function call.
     * @return a new {@link Masker} using the specified masking arguments
     */
    public abstract Masker masker(ByteBuffer... parameters);

    /**
     * Class that actually makes the masking of the first function parameter according to the masking arguments.
     */
    public interface Masker
    {
        public ByteBuffer mask(ByteBuffer value);
    }

    protected static abstract class Factory extends FunctionFactory
    {
        public Factory(String name, FunctionParameter... parameters)
        {
            super(NAME_PREFIX + name.toLowerCase(), parameters);
        }
    }
}
