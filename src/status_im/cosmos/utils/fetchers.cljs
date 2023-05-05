(ns status-im.cosmos.utils.fetchers
  (:require
  [cljs.core.async :as async]
  [cljs.core.async.interop :as interop]
  [status-im.cosmos.common.assertions :as assertions]
  ["expo-random" :as expo-random]
  ["typedarray" :as typedarray]
 ))

(defn find-first [pred coll]
  (first (filter pred coll)))




(defn string->json [data]
  (if (nil? data)
    nil
    (js/JSON.parse data)))

(defn stringify-json [data]
  (if (nil? data)
    nil
    (js/JSON.stringify data)))