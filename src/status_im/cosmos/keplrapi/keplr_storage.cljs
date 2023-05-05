(ns status-im.cosmos.keplrapi.keplr-storage
  (:require-macros [cljs.core :refer [assert]])
  (:require
   ["@react-native-async-storage/async-storage" :default async-storage]
   ["@keplr-wallet/common" :refer (KVStore)]
   [taoensso.timbre :as log]
   [status-im.cosmos.utils.fetchers :refer (string->json stringify-json)]

   ))



(defn get-item-from-storage [cb key-to-be-fetched]
  (-> ^js async-storage
      (.getItem key-to-be-fetched)
      (.then (fn [^js data]
               (cb (string->json data))))
      (.catch (fn [error]
                (cb (js/undefined))
                (log/error "get-item [async-storage]" error)))))

(defn remove-item-if-empty-key [key value]
  (if (= value nil)
    (.removeItem ^js async-storage key)
    (.resolve js/Promise)))

(defn set-item-to-storage [cb key-to-be-saved value]
  (-> (remove-item-if-empty-key key-to-be-saved value)
      (.then (fn [^js _]
               (.setItem ^js async-storage key-to-be-saved (stringify-json value))))
      (.then (fn [^js data]
               (cb data)))
      (.catch (fn [error]
                (cb (js/undefined))
                (log/error "set-item-to-storage [async-storage]" error)))))

(defn build-async-kv-instance [prefix]
  #js{:get    (fn [key]
                (js/Promise.
                 (fn [resolve _]
                   (get-item-from-storage resolve (str prefix "/" key)))))
      :set    (fn [key value]
                (js/Promise.
                 (fn [resolve _]
                   (set-item-to-storage resolve (str prefix "/" key) value))))
      :prefix #(prefix)})

(comment
  (def testable-async-kv-store (build-async-kv-instance "testable-async-kv-store"))
  (-> (.set testable-async-kv-store "app" "gpt2")
      (.then #(.get testable-async-kv-store "app"))
      (.then #(assert (= % "gpt2") (str "not gpt2, instead got" %)))
      (.catch #(js/console.log %)))) ;expect Error not thrown in console