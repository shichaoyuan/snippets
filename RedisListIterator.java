
import java.util.Iterator;
import java.util.List;

import redis.clients.jedis.Jedis;


/**
 * Redis List 迭代器
 * 
 * 适用于较大的List
 * 线程不安全
 * 
 * @author yuanshichao
 *
 */
public class RedisListIterator implements Iterator<byte[]> {
    
    
    private static final int BATCH_SIZE = 1000;
    
    private Jedis client;
    private byte[] key;
    
    //当前读取的list分片
    private List<byte[]> slice;
    
    //当前读取第几个分片。 zero-based
    private int sliceIndex;
    
    //当前读取分片中，元素的位置。 zero-based
    private int index;
    
    public RedisListIterator(Jedis client, byte[] key) {
        this.slice = null;
        this.sliceIndex = -1;
        
        this.client = client;
        this.key = key;
        
        fetchItems();
    }
    
    @Override
    public byte[] next() {
        index++;
        return slice.get(index);
    }
    
    @Override
    public boolean hasNext() {
        if (slice == null) {
            return false;
        }
        
        if ((index+1) >= BATCH_SIZE) {
            fetchItems();
        }
        
        if ((index+1) >= slice.size()) {
            return false;
        }
        
        return true;
    }
    
    private void fetchItems() {
        sliceIndex++;
        slice = client.lrange(key, sliceIndex * BATCH_SIZE, (sliceIndex+1) * BATCH_SIZE -1);
        index = -1;
        //LOG.info("RedisListIterator fetchItems [key: " + new String(key) + ", sliceIndex: " + sliceIndex + ", sliceSize: " + slice.size() + "]" );
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}
