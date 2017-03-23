# cleanclaim

A library for converting Municipal Disaster Recovery Assistance Program (MDRA) claims. The MDRA claim is great for client-facing work, but isn't appropriate for direct import to MDRA's Microsoft Access database.

Using cleanclaim, you can turn MDRA claim forms into flat Excel workbooks that can be imported into Access. It produces perfect results every time.

## Usage

Cleanclaim's public API is two functions: `read-book/read-book` and `write-book/write-book`. Use them thusly:

```clojure
(require '[cleanclaim.read-book :as rb]
         '[cleanclaim.write-book :as wb])

(def in-map {:claimant-id 1
             :input-path "Absolute path to claim.xlsm file"
             :output-path "Absolute path to output file (.xlsx extension will be added)"})

(defn read!
  [{:keys [claimant-id input-path output-path]}]
  (->> (rb/read-book input-path)
       (wb/write-book claimant-id output-path)))

(read! in-map)
```

A [seesaw](https://github.com/daveray/seesaw)-based UI namespace, `cleanclaim.ui`, is also provided. Use it at your own peril.

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
