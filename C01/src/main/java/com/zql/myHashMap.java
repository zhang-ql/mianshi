package com.zql;

public class myHashMap<K, V> {
    //我们需要定义一个节点来作为具体数据的载体，它不仅要承载键值对，同样还得作为单链表的节点：
    class Node<K, V> {
        //键值对
        private K key;
        private V value;

        //链表，后继
        private Node<K, V> next;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    //容量
    final int capacity = 16;
    final float loadFactor = 0.75f;
    //hashmap的大小
    private  int size;
    //桶数组
    Node<K, V>[] buckets;

    /**
     * 无参构造器，设置桶数组默认容量
     */
    public myHashMap() {
        buckets = new Node[capacity];
        size = 0;
    }

    /**
     * 有参构造器，指定桶数组容量
     *
     * @param capacity
     */
    public myHashMap(int capacity) {
        buckets = new Node[capacity];
        size = 0;
    }


    /**
     * 哈希函数，获取地址
     *
     * @param key
     * @return
     */
    private int getIndex(K key, int length) {
        //获取hash code
        int hashCode = key.hashCode();
        hashCode ^= (hashCode >>> 16);
        //和桶数组长度取余
        int index = hashCode % length;
        return Math.abs(index);
    }

    /**
     * put方法
     * 获取元素插入位置
     * 当前位置为空，直接插入
     * 位置不为空，发生冲突，遍历链表
     * 如果元素key和节点相同，覆盖，否则新建节点插入链表头部 或者 尾部
     */
    public void put(K key, V value) {
        //判断是否需要扩容
        if (size >= buckets.length * loadFactor) resize();
    }
    /**
     * 将元素存入指定的node数组
     *
     * @param key
     * @param value
     * @param table
     */
    private void putVal(K key, V value, Node<K, V>[] table) {
        //获取位置
        int index = getIndex(key, table.length);
        Node node = table[index];
        //插入的位置为空
        if (node == null) {
            table[index] = new Node<>(key, value);
            size++;
            return;
        }
        //插入位置不为空，说明发生冲突，使用链地址法,遍历链表
        while (node != null) {
            //如果key相同，就覆盖掉
            if ((node.key.hashCode() == key.hashCode())
                    && (node.key == key || node.key.equals(key))) {
                node.value = value;
                return;
            }
            node = node.next;
        }
        //当前key不在链表中，插入链表头部
        Node newNode = new Node(key, value, table[index]);
        table[index] = newNode;
        size++;
    }


    /**
     * 扩容
     */
    private void resize() {
        //创建一个两倍容量的桶数组
        Node<K, V>[] newBuckets = new Node[buckets.length * 2];
        //将当前元素重新散列到新的桶数组
        rehash(newBuckets);
        buckets = newBuckets;
    }

    /**
     * 重新散列当前元素
     *
     * @param newBuckets
     */
    private void rehash(Node<K, V>[] newBuckets) {
        //map大小重新计算
        size = 0;
        //将旧的桶数组的元素全部刷到新的桶数组里
        for (int i = 0; i < buckets.length; i++) {
            //为空，跳过
            if (buckets[i] == null) {
                continue;
            }
            Node<K, V> node = buckets[i];
            while (node != null) {
                //将元素放入新数组
                putVal(node.key, node.value, newBuckets);
                node = node.next;
            }
        }
    }


}
