package org.redisson;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.core.RKeys;

import io.netty.util.concurrent.Future;

public class RedissonKeys implements RKeys {

    private final CommandExecutor commandExecutor;

    public RedissonKeys(CommandExecutor commandExecutor) {
        super();
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String randomKey() {
        return commandExecutor.get(randomKeyAsync());
    }

    @Override
    public Future<String> randomKeyAsync() {
        return commandExecutor.readRandomAsync(RedisCommands.RANDOM_KEY, StringCodec.INSTANCE);
    }

    /**
     * Find keys by key search pattern
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern
     * @return
     */
    @Override
    public Collection<String> findKeysByPattern(String pattern) {
        return commandExecutor.get(findKeysByPatternAsync(pattern));
    }

    /**
     * Find keys by key search pattern in async mode
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern
     * @return
     */
    @Override
    public Future<Collection<String>> findKeysByPatternAsync(String pattern) {
        return commandExecutor.readAllAsync(RedisCommands.KEYS, pattern);
    }

    /**
     * Delete multiple objects by a key pattern
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern
     * @return
     */
    @Override
    public long deleteByPattern(String pattern) {
        return commandExecutor.get(deleteByPatternAsync(pattern));
    }

    /**
     * Delete multiple objects by a key pattern in async mode
     *
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern
     * @return
     */
    @Override
    public Future<Long> deleteByPatternAsync(String pattern) {
        return commandExecutor.evalWriteAllAsync(RedisCommands.EVAL_INTEGER, new SlotCallback<Long, Long>() {
            AtomicLong results = new AtomicLong();
            @Override
            public void onSlotResult(Long result) {
                results.addAndGet(result);
            }

            @Override
            public Long onFinish() {
                return results.get();
            }
        }, "local keys = redis.call('keys', ARGV[1]) "
                + "local n = 0 "
                + "for i=1, table.getn(keys),5000 do "
                    + "n = n + redis.call('del', unpack(keys, i, math.min(i+4999, table.getn(keys)))) "
                + "end "
            + "return n;",Collections.emptyList(), pattern);
    }

    /**
     * Delete multiple objects by name
     *
     * @param keys - object names
     * @return
     */
    @Override
    public long delete(String ... keys) {
        return commandExecutor.get(deleteAsync(keys));
    }

    /**
     * Delete multiple objects by name in async mode
     *
     * @param keys - object names
     * @return
     */
    @Override
    public Future<Long> deleteAsync(String ... keys) {
        return commandExecutor.writeAllAsync(RedisCommands.DEL, new SlotCallback<Long, Long>() {
            AtomicLong results = new AtomicLong();
            @Override
            public void onSlotResult(Long result) {
                results.addAndGet(result);
            }

            @Override
            public Long onFinish() {
                return results.get();
            }
        }, (Object[])keys);
    }


}
