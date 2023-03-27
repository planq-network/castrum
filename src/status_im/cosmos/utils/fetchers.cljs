(ns status-im.cosmos.utils.fetchers (:require
  [cljs.core.async :as async]
  [cljs.core.async.interop :as interop]
  ["expo-random" :as expo-random]
 ))

(defn find-first [pred coll]
  (first (filter pred coll)))



(defn- byte-array->uint8-array [byte-array]
  (js/Uint8Array. (.buffer byte-array)
                  (.byteOffset byte-array)
                  (.byteLength byte-array)))
;
;(defn get-random-bytes-async
;  [^js/Uint8Array array]
;  (async/go
;    (let [random (async/<! (interop/js->cljs (expo-random/getRandomBytesAsync (.-byteLength array))))]
;      (let [bytes (byte-array->uint8-array array)]
;        (dotimes [i (count random)]
;          (aset bytes i (aget random i)))
;        array))))