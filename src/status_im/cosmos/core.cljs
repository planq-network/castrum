(ns status-im.cosmos.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.db :as utils.db]
            [status-im.utils.react-native :as react-native-utils]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [goog.crypt :as c]
            [oops.core :refer [oget ocall gget oget+ ocall+]]
            ["@keplr-wallet/cosmos" :default cosmos :refer (Bech32Address)]))

(defn string-to-bytes [opts]
  (c/hexToByteArray opts))

(defn bech32-address [opts]
  (new Bech32Address (clj->js(string-to-bytes opts))))

(defn to-bech32 [object opts]
  (ocall+ object "toBech32" opts))

(defn from-bech32 [opts]
  (ocall+ Bech32Address "fromBech32" (clj->js opts)))

(defn bech32-to-hex [object]
  (ocall+ object "toHex"))

(defn get-bech32-prefix [db]
  (let [networks (get db :networks/networks)
        current-network-id (get db :networks/current-network)
        current-network (get networks current-network-id)]
      (get-in current-network [:config :Bech32Prefix])))

(defn convert-address [address db]
  (if (string/starts-with? address "0x")
    (let [addr (string/replace address "0x" "")
          bech32-prefix (get-bech32-prefix db)
          bech32-addr (bech32-address addr)]
    (to-bech32 bech32-addr bech32-prefix))
    (let [bech32-prefix (get-bech32-prefix db)
          bech32-addr (from-bech32 {:bech32Address address :prefix bech32-prefix})]
           (bech32-to-hex(bech32-addr))
    )))