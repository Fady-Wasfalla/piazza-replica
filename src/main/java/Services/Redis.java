package Services;

import io.github.cdimascio.dotenv.Dotenv;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class Redis {
    protected static Jedis redisClient;

    public static void initRedis() {
        Dotenv dotenv = Dotenv.load();
        String host = dotenv.get("REDIS_HOST", "localhost");
        int port = Integer.parseInt(dotenv.get("REDIS_PORT", "6379"));
        String password = dotenv.get("REDIS_PASSWORD", "");
        Jedis jedisClient = new Jedis(host, port);
        if (password != "")
            jedisClient.auth(password);
        redisClient = jedisClient;
    }

    public static void setLayeredCache(String key, String query, String cachedDocument) {
        redisClient.hset(key, query, cachedDocument);
    }

    public static String getLayeredCache(String key, String query) {
        return redisClient.hget(key, query);
    }

    public static void deleteCache(String key) {
        redisClient.del(key);
    }

    public static Set<String> returnKeys(String key) {
        return redisClient.keys(key);
    }

    public static Jedis getJedisClient() {
        return redisClient;
    }
}
