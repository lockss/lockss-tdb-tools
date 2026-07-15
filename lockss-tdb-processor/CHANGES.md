# `lockss-tdb-processor` Release Notes

## 1.9.0 (LOCKSS 2.0.91-beta2)

### Features

* Add `titleName` trait to supplant the title block's name.
* Make title sets based on publisher names, not publisher blocks.
* Add AU-level DOI and demote title-level DOI.

### Fixes

* TdbXml set of publisher names now reset between multiple segments (e.g. `--output-dir`).


## Changes Since 1.3.0

### Features

*   Removed dependency on ICU4J.

### Fixes

* TDB tool arguments that are directories now expand to files in sorted order.
