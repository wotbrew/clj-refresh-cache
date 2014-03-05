(ns clj-refresh-cache.core
  (:import (clojure.lang IDeref))
  (:require [clojure.core.cache :refer :all]))

(defn- key-killer
  [ttl expiry now]
  (let [ks (map key (filter #(> (- now (val %)) expiry) ttl))]
    #(apply dissoc % ks)))

(defcache RefreshCache [cache ttl ttl-ms]
          CacheProtocol
          (lookup [this item]
                  (let [ret (lookup this item ::nope)]
                    (when-not (= ret ::nope) ret)))
          (lookup [this item not-found]
                  (get cache item not-found))
          (has? [_ item]
                (let [t (get ttl item (- ttl-ms))]
                  (< (- (System/currentTimeMillis)
                        t)
                     ttl-ms)))
          (hit [this item] this)
          (miss [this item result]
                (let [now  (System/currentTimeMillis)
                      kill-old (key-killer ttl ttl-ms now)]
                  (RefreshCache. (assoc (kill-old cache) item result)
                             (assoc (kill-old ttl) item now)
                             ttl-ms)))
          (seed [_ base]
                (let [now (System/currentTimeMillis)]
                  (RefreshCache. base
                             (into {} (for [x base] [(key x) now]))
                             ttl-ms)))
          (evict [_ key]
                 (RefreshCache. (dissoc cache key)
                            (dissoc ttl key)
                            ttl-ms))
          Object
          (toString [_]  (str cache \, \space ttl \, \space ttl-ms)))


(defn refresh-cache-factory
  "
   * The difference between refresh and ttl is that
     this cache will always return you a value if one has been available.
     It will only evict on misses *
   Returns a TTL cache with the cache and expiration-table initialied to `base` --
   each with the same time-to-live.

   This function also allows an optional `:ttl` argument that defines the default
   time in milliseconds that entries are allowed to reside in the cache."
  [base & {ttl :ttl :or {ttl 2000}}]
  {:pre [(number? ttl) (<= 0 ttl)
         (map? base)]}
  (clojure.core.cache/seed (RefreshCache. {} {} ttl) base))



(defn memoize-refreshing
  "take a function and return a memoized function that
   will re-invoke the function once the ttl is up (every `ttl` milliseconds)"
  [fn ttl]
  (let [cache (atom (refresh-cache-factory {} :ttl ttl))
        swapf #(if (has? % {})
               (hit % {})
               (miss % {} (delay (fn))))]
    #(do
      (swap! cache swapf)
      @(lookup @cache {}))))

(defn refreshing
  "takes a fn and ttl returns a lazy identity which, when deref'd will invoke the function fn.
   the value of which will be cached for `ttl` milliseconds at which point a further deref will
   re-invoke the function (caching again for `ttl`)"
  [fn ttl]
  (let [ref (memoize-refreshing fn ttl)]
    (reify IDeref
      (deref [this] (ref)))))



