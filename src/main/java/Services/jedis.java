package Services;

import redis.clients.jedis.Jedis;

import java.util.Set;

public class jedis {
    Jedis jedis;

    public jedis(String host, int port, String password) {
        Jedis jedis = new Jedis(host, port);
        if (password != "")
            jedis.auth(password);
        this.jedis = jedis;
    }

    public void setLayeredCache(String key, String query, String cachedDocument) {
        this.jedis.hset(key, query, cachedDocument);
    }

    public String getLayeredCache(String key, String query) {
        return this.jedis.hget(key, query);
    }

    public void deleteCache(String key) {
        this.jedis.del(key);
    }

    public Set<String> returnKeys(String key) {
        return jedis.keys(key);
    }

    public Jedis getJedisClient() {
        return this.jedis;
    }
}
