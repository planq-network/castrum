(ns status-im.cosmos.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.db :as utils.db]
            [status-im.utils.react-native :as react-native-utils]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            ["@keplr-wallet/cosmos" :refer (Bech32Address)]))


(defn get-bech32-prefix
      [{:networks/keys [current-network networks]}]
      (get networks current-network)
      (get-in current-network [:config :Bech32Prefix]))
(defn convert-address [address db]
  (if (string/starts-with? address "0x")
    (let [addr (string/replace address "0x" "")
          bech32-addr (Bech32Address (aget addr 0))]
          (bech32-addr/toBech32(get-bech32-prefix db)))
    (let [bech32-addr (Bech32Address/fromBech32 address get-bech32-prefix)]
           (bech32-addr/toHex true))
    ))