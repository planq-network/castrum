(ns status-im.cosmos.utils.fetchers)

(defn find-first [pred coll]
  (first (filter pred coll)))
