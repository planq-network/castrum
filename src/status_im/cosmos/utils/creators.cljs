(ns status-im.cosmos.utils.creators  (:require
                                       [cljs.core.async :as async]
                                       [cljs.core.async.interop :as interop]
                                       [status-im.cosmos.common.assertions :as assertions]
                                       ["expo-random" :as expo-random]
                                       ["typedarray" :as typedarray]
                                       ))

(defn get-all-function-names [obj]
  (let [properties  (js-keys obj)]
    (filter #(fn? (aget obj %)) properties)
    ))
(defn existing-functions-to-map [obj]
  (let [ functions (get-all-function-names obj)]
    (reduce (fn [acc fn-name]
              (assoc acc (keyword fn-name)  (aget obj fn-name)))
            {}
            functions)))


(defn jsobj->cljmap [input]
  (->
    input
    js/JSON.stringify
    js/JSON.parse
    (js->clj :keywordize-keys true)))
(defn to-extended-object [parent-object updated-methods]
  (->
    parent-object
    jsobj->cljmap
    (merge (existing-functions-to-map parent-object) updated-methods)
    clj->js )
  )
