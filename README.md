# clj-refresh-cache

[![Build Status](https://travis-ci.org/danstone/clj-refresh-cache.png?branch=master)](https://travis-ci.org/danstone/clj-refresh-cache)

A new ttl-style cache for core-cache that will return expired values on lookup.
good if you want to represent a value that changes every 'x' seconds.

## Usage
``` clojure
  [clj-refresh-cache "0.1.0-SNAPSHOT"]
```
## License

Copyright © 2014 Dan Stone

Distributed under the Eclipse Public License, the same as Clojure.
