(ns status-im.cosmos.account.governance.proposals.stores.pool
  (:require
   [oops.core :refer [gget ocall ocall+ oget oget+]]
   [re-frame.core :as re-frame]
   [re-frame.db :as re-frame.db]
   [status-im.utils.fx :as fx]
   ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries ObservableQueryProposal)]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]))

(defn pool-query [db]
  (-> (get-in db [:keplr-store :query-store])
      (ocall "get" (get-in db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (oget "queryGovernance")
      (.getQueryPool)))

(defn format-pool-data [response]
  (-> response
      (js->clj :keywordize-keys true)
      (get-in [:response :data :pool])))

(fx/defn update-pool-data
  {:events [:keplr-store/update-pool-data]}
  [{:keys [db]} response]
  {:db (assoc-in db [:keplr-store :pool] (format-pool-data response))})

(fx/defn fetch-pool-data
  {:events [:keplr-store/fetch-pool-data]}
  [{:keys [db]} _]
  {:keplr-observable/fetchquery {:query      (pool-query db)
                                 :on-success [:keplr-store/update-pool-data]
                                 :on-failure [:ui/show-error "Unable to fetch governance proposals"]}})

(re-frame/reg-sub
 :keplr-store/pool
 :<- [:keplr-store]
 (fn [store [_ _]]
   (prn "tokens" (:pool store))
   (:pool store)))

(comment

  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals])
  (get-in @re-frame.db/app-db [:keplr-store :pool])
  (re-frame/dispatch [:keplr-store/fetch-pool-data]))

(comment

  (let [pool (get-in @re-frame.db/app-db [:keplr-store :pool])
        proposal (get-in @re-frame.db/app-db [:keplr-store :governance-proposals 0])
        coin-decimals (coin-decimals-for-current-stake-currency @re-frame.db/app-db)
        final-tally-result (:final_tally_result proposal)]

    (prn (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]))
    (prn (get-tally-for final-tally-result coin-decimals));{:yes #object[IntPretty 63,680.000000000000000000], :no #object[IntPretty 0.000000000000000000], :abstain #object[IntPretty 0.000000000000000000], :no_with_veto #object[IntPretty 0.000000000000000000]}
))
