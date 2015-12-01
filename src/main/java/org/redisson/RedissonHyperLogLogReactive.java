/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.reactivestreams.Publisher;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandReactiveExecutor;
import org.redisson.core.RHyperLogLogReactive;

public class RedissonHyperLogLogReactive<V> extends RedissonExpirableReactive implements RHyperLogLogReactive<V> {

    protected RedissonHyperLogLogReactive(CommandReactiveExecutor commandExecutor, String name) {
        super(commandExecutor, name);
    }

    protected RedissonHyperLogLogReactive(Codec codec, CommandReactiveExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
    }

    @Override
    public Publisher<Boolean> add(V obj) {
        return commandExecutor.writeReactive(getName(), codec, RedisCommands.PFADD, getName(), obj);
    }

    @Override
    public Publisher<Boolean> addAll(Collection<V> objects) {
        List<Object> args = new ArrayList<Object>(objects.size() + 1);
        args.add(getName());
        args.addAll(objects);
        return commandExecutor.writeReactive(getName(), codec, RedisCommands.PFADD, getName(), args.toArray());
    }

    @Override
    public Publisher<Long> count() {
        return commandExecutor.writeReactive(getName(), codec, RedisCommands.PFCOUNT, getName());
    }

    @Override
    public Publisher<Long> countWith(String... otherLogNames) {
        List<Object> args = new ArrayList<Object>(otherLogNames.length + 1);
        args.add(getName());
        args.addAll(Arrays.asList(otherLogNames));
        return commandExecutor.writeReactive(getName(), codec, RedisCommands.PFCOUNT, args.toArray());
    }

    @Override
    public Publisher<Void> mergeWith(String... otherLogNames) {
        List<Object> args = new ArrayList<Object>(otherLogNames.length + 1);
        args.add(getName());
        args.addAll(Arrays.asList(otherLogNames));
        return commandExecutor.writeReactive(getName(), codec, RedisCommands.PFMERGE, args.toArray());
    }

}