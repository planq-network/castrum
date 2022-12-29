(ns status-im.cosmos.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.db :as utils.db]
            [status-im.utils.react-native :as react-native-utils]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [oops.core :refer [oget ocall gget oget+]]
            ["@keplr-wallet/cosmos" :refer (Bech32Address)]))

(def bech32-address-ctor (gget "Bech32Address"))
(def bech32-address (bech32-address-ctor. <args>))
(defn to-bech32 [opts] (oget+ Bech32Address "toBech32" (clj->js opts)))
(defn from-bech32 [opts] (oget+ Bech32Address "fromBech32" (clj->js opts)))
(def bech32-to-hex (oget+ Bech32Address "toHex"))



(defn get-bech32-prefix
      [{:networks/keys [current-network networks]}]
      (get networks current-network)
      (get-in current-network [:config :Bech32Prefix]))

(defn convert-address [address db]
  (if (string/starts-with? address "0x")
    (let [addr (string/replace address "0x" "")
          bech32-prefix (get-bech32-prefix db)
          bech32-addr (bech32-address (aget addr 0))]

    (update bech32-addr to-bech32(bech32-addr {:prefix bech32-prefix}))
    (bech32-addr))
    (let [bech32-prefix (get-bech32-prefix db)
          bech32-addr (from-bech32 {:bech32Address address :prefix bech32-prefix})]
           (bech32-to-hex(bech32-addr))
    )))