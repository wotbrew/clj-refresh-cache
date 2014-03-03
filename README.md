# clj-refresh-cache

[![Build Status](https://travis-ci.org/danstone/clj-refresh-cache.png?branch=master)](https://travis-ci.org/danstone/clj-refresh-cache)

A new ttl-style cache for core-cache that will return expired values on lookup.
good if you want to represent a value that changes every 'x' seconds.

## Usage

### Lein

``` clojure
  [clj-refresh-cache "0.1.0-SNAPSHOT"]
```

```clojure
    (defn really-expensive [] ...)
    ;;the refreshing function will return a value that can be de-referenced in the usual way.
    (def every-second (refreshing really-expensive 1000))

    ;;deref as so
    @every-second

    ;;you can also use the function form
    (def memoized (memoize-refreshing really-expensive 1000))
    ;;call as so
    (memoized)
```

## License

Copyright © 2014 Dan Stone

Distributed under the Eclipse Public License, the same as Clojure.
