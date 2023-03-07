(ns status-im.cosmos.stores.keplr-store
  (:require
    [cljs-bean.core :as clj-bean]
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    [status-im.cosmos.utils.keplr-storage :refer [build-async-kv-instance]]
    [status-im.utils.config :refer [cosmos-config]]
    [status-im.utils.fx :as fx]
    ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries)]))

(defn build-query-store [chainStore]
  (QueriesStore.
    (build-async-kv-instance "store_queries_fix3")
    chainStore,
    (.use CosmosQueries)
    (.use CosmwasmQueries)))

(defn init-with-chain-info [chain-infos]
  (let [chainStore (ChainStore. (clj-bean/->js chain-infos))]
    {:chain-store                  chainStore
     :query-store                  (build-query-store chainStore)
     :chain-infos                  chain-infos
     :selected-chain-id            (-> chain-infos first :chainId)
     :selected-validator-status    "Bonded"
     :available-validator-statuses ["Bonded" "Unbonded" "Unbonding" "Unspecified"]
     :governance-proposals         []}))
(fx/defn init-keplr-store
         {:events [:keplr-store/init]}
         [{:keys [db]} _]
         {:db         (assoc db :keplr-store (init-with-chain-info (cosmos-config)))
          :dispatch-n [[:keplr-store/fetchgovernanceproposals] [:keplr-store/fetchvalidators]]})

(fx/defn update-chain-store-with-chain-infos
         {:events [:keplr-store/update-chain-infos]}
         [{:keys [db]} update-cosmos-config]
         {:db (assoc db :keplr-store (init-with-chain-info update-cosmos-config))})

(fx/defn set-selected-chain-id
         {:events [:keplr-store/set-selected-chain-id]}
         [{:keys [db]} chain-id]
         {:db         (assoc-in db [:keplr-store :selected-chain-id] chain-id)
          :dispatch-n [[:keplr-store/fetchgovernanceproposals] [:keplr-store/fetchvalidators]]})





(comment

  (def assert-not-nil (fn [x] (assert (not= nil x) (str x " should not be nil"))))

  ;Test Initialize chain
  (re-frame/dispatch [:keplr-store/init])
  (assert-not-nil (:keplr-store @re-frame.db/app-db))

  ;update chain
  (re-frame/dispatch [:keplr-store/update-chain-infos (assoc-in (cosmos-config) [0 :features] ["test-feature"])])
  (assert (= ["test-feature"] (get-in @re-frame.db/app-db [:keplr-store :chain-infos 0 :features])) ":features should have [\"test-feature\"]")
  ; the features should have changed


  ;select chain by id
  (re-frame/dispatch [:keplr-store/set-selected-chain-id "chain-1"])
  (assert (= "chain-1" (get-in @re-frame.db/app-db [:keplr-store :selected-chain-id])) "selected chain id should be chain-1")



  )
