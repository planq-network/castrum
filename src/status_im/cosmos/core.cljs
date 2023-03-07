(ns status-im.cosmos.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.db :as utils.db]
            [status-im.utils.react-native :as react-native-utils]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.cosmos.views.account.governance.proposal-detail :as proposal-detail]
            [status-im.cosmos.views.account.staking.validator-detail :as validator-detail]
            [goog.crypt :as c]
            [oops.core :refer [oget ocall gget oget+ ocall+]]
            ["@keplr-wallet/crypto" :as crypto-lib]
            ["@keplr-wallet/common" :as common-lib]
            ["@keplr-wallet/types" :as keplr-types]
            ["@keplr-wallet/stores" :as keplr-store]
            ["@keplr-wallet/cosmos" :default cosmos :refer (Bech32Address)]

            ;to register events ,effects and subs
            status-im.cosmos.stores.validators
            status-im.cosmos.stores.proposals
            status-im.cosmos.stores.validator-thumbnail-store
            status-im.cosmos.stores.address-type-store
            status-im.cosmos.stores.keplr-effects
            status-im.cosmos.stores.keplr-store))

(defn string-to-bytes [opts]
  (c/hexToByteArray opts))

(defn bech32-address [opts]
  (new Bech32Address (clj->js (string-to-bytes opts))))

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
      (bech32-to-hex (bech32-addr)))))

;

(def screens [{:name      :proposal-detail
               :options   {:topBar {:title {:text (i18n/label :t/proposal-detail)}}}
               :component proposal-detail/proposal-by-id}
              {:name      :validator-detail
               :options   {:topBar {:title {:text (i18n/label :t/validator-detail)}}}
               :component validator-detail/validator-detail}])