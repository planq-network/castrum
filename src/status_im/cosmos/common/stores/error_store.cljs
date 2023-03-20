(ns status-im.cosmos.common.stores.error-store
  (:require
   [status-im.cosmos.common.formatters :as formatters]
   [status-im.utils.fx :as fx]
   ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries ObservableQueryProposal)]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]))

(fx/defn handle-cosmos-error
  {:events [:keplr-store/handle-cosmos-error]}
  [{:keys [db]} ks error]
  {:db (formatters/assoc-error-response-in db ks error)})