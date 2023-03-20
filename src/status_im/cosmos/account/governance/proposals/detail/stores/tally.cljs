(ns status-im.cosmos.account.governance.proposals.detail.stores.tally
  (:require
   [oops.core :refer [gget ocall ocall+ oget oget+]]
   [re-frame.core :as re-frame]
   [re-frame.db :as re-frame.db]
   [status-im.cosmos.account.governance.proposals.common.queries :as queries]
   [status-im.cosmos.account.governance.proposals.common.queries :refer [coin-decimals-for-current-stake-currency all-proposals-query proposal-by-id current-stake-currency]]
   [status-im.cosmos.common.formatters :as formatters]
   [status-im.cosmos.utils.fetchers :refer [find-first]]
   [status-im.utils.fx :as fx]
   ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries ObservableQueryProposal)]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]
   [status-im.utils.money :as money]
   [status-im.utils.number :as number-utils]))

; The Query is Constructed by creating
(defn tally-for-proposal-query [id db]
  (let [proposal (clj->js (proposal-by-id id db))
        all-proposal-query-instance (all-proposals-query db)
        kv-store (oget all-proposal-query-instance "kvStore")
        chain-id (oget all-proposal-query-instance "chainId")
        chain-getter (oget all-proposal-query-instance "chainGetter")
        governance-proposals-query all-proposal-query-instance]
    (ObservableQueryProposal. kv-store chain-id chain-getter proposal governance-proposals-query)))

(defn format-tally-response [response]
  (-> response
      (js->clj :keywordize-keys true)
      (get-in [:response :data])
      (:tally)))
(defn compute-tally-based-on-status [proposal tally-response coin-decimals]
  ;if status is not voting period, return the final tally_result
  ;if status is voting period, return the tally
  (let [status (:status proposal)
        tally (if (= status "PROPOSAL_STATUS_VOTING_PERIOD") tally-response (:final_tally_result proposal))
        result  tally]
    result))

(defn to-IntPretty [{:keys [yes no abstain no_with_veto]}]
  {:yes          (IntPretty. yes)
   :abstain      (IntPretty. abstain)
   :no           (IntPretty. no)
   :no_with_veto (IntPretty. no_with_veto)})
(defn sum-of-tally [formatted-tally-result]
  (-> (:yes formatted-tally-result)
      (.add (:no formatted-tally-result))
      (.add (:abstain formatted-tally-result))
      (.add (:no_with_veto formatted-tally-result))
      (.toDec)))

(defn compute-tally-ratio [formatted-tally-result input]

  (let [tally-sum   (-> input
                        (IntPretty.)
                        (.toDec))
        format-key (fn [key]
                     (-> (key formatted-tally-result)
                         (.toDec)
                         (.quoTruncate tally-sum)
                         (.mulTruncate (.getPrecisionDec DecUtils 2))
                         (.toString)
                         (money/bignumber)
                         (.toNumber)
                         (number-utils/naive-round  2)))]

    {:yes          (format-key :yes)
     :no           (format-key :no)
     :abstain      (format-key :abstain)
     :no_with_veto (format-key :no_with_veto)}))

(defn compute-turn-out [tally-sum bonded-tokens]
  (-> tally-sum
      (IntPretty.)
      (.toDec)
      (.quoTruncate (.toDec bonded-tokens))
      (.mulTruncate (.getPrecisionDec DecUtils 2))
      (.toString)
      (money/bignumber)
      (money/to-fixed 2)))

(defn format-proposal-detail [proposal voted tally-raw-response bonded-tokens coin-decimals]
  (let [tally-input-response (format-tally-response tally-raw-response)
        tally (compute-tally-based-on-status proposal tally-input-response coin-decimals)
        formatted-tally-result (to-IntPretty tally)
        total (sum-of-tally formatted-tally-result)
        turnout (compute-turn-out total bonded-tokens)
        tally-ratio (compute-tally-ratio formatted-tally-result total)]
    (assoc proposal :voted voted :tally tally :total (.toString total) :turnout turnout :tally-ratio tally-ratio)))

(fx/defn handle-fetch-tally-for-proposal-success
  {:events [:keplr-store/handle-fetch-tally-for-proposal-success]}
  [{:keys [db]} id voted-response tally-raw-response]
  (let [current-proposal (queries/proposal-by-id id db)
        bonded-tokens (IntPretty. (get-in db [:keplr-store :pool :bonded_tokens]))
        coin-decimals (coin-decimals-for-current-stake-currency db)
        formatted-response (format-proposal-detail current-proposal voted-response tally-raw-response bonded-tokens coin-decimals)]
    {:db (formatters/assoc-success-response-in db [:keplr-store :governance-proposal-details (keyword id)] formatted-response)}))

(fx/defn fetch-tally-for-proposal
  {:events [:keplr-store/fetch-tally-for-proposal]}
  [{:keys [db]} id voted-response]
  {:keplr-observable/fetchquery {:query      (tally-for-proposal-query id db)
                                 :on-success [:keplr-store/handle-fetch-tally-for-proposal-success id voted-response]
                                 :on-failure [:keplr-store/handle-cosmos-error [:keplr-store :governance-proposal-details (keyword id)]]}})

(comment; check error scenario
  (re-frame/dispatch [:keplr-store/handle-cosmos-error [:keplr-store :governance-proposal-details (keyword "1")]  "Unable to fetch Details for proposal"]) (->> (get-in @re-frame.db/app-db [:keplr-store :governance-proposals])
                                                                                                                                                                (map-indexed (fn [i x] (when (= (:proposal_id x) "2") i)))
                                                                                                                                                                (remove nil?)
                                                                                                                                                                first)

  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals 1 :tally])

  (re-frame/dispatch [:keplr-store/fetch-tally-for-proposal "3"]))

