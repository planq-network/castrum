(ns status-im.cosmos.account.governance.proposals.common.queries
  (:require
   [oops.core :refer [gget ocall ocall+ oget oget+]]
   [status-im.cosmos.utils.fetchers :refer [find-first]]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]

   [status-im.utils.datetime :as datetime]))

(defn current-stake-currency [db]
  (->> (get-in db [:keplr-store :chain-infos])
       (find-first #(= (:chainId %) (get-in db [:keplr-store :selected-chain-id])))
       :stakeCurrency))

(defn coin-decimals-for-current-stake-currency [db]
  (->> (current-stake-currency db)
       :coinDecimals))

(defn all-proposals-query [db]
  (-> (get-in db [:keplr-store :query-store])
      (ocall "get" (get-in db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (oget "queryGovernance")))

(defn available-proposals [db]

  (get-in db [:keplr-store :governance-proposals :data]))

(defn proposal-by-id [id db]
  (find-first #(= (:proposal_id %) id) (available-proposals db)))

(defn proposal-status [proposal]
  ;replace ProposalStatus to ""

  (-> proposal
      (get-in [:status])
      (clojure.string/replace #"PROPOSAL_STATUS_" "")))

(defn get-proposal-end-time [proposal]
  (let [status (proposal-status proposal)]
    (if (= status "DEPOSIT_PERIOD")
      (get-in proposal [:deposit_end_time])
      (get-in proposal [:voting_end_time]))))

(defn proposal-end-detail [proposal]
  (->> proposal
       (get-proposal-end-time)
       (datetime/parse-date-to-ms)
       (datetime/timestamp->long-date)
       (str "Voting ends: ")))

(defn days-left-for-voting [proposal]
  (let [rules {:DEPOSIT_PERIOD :deposit_end_time
               :VOTING_PERIOD  :voting_end_time}
        rule (get rules (keyword (proposal-status proposal)) nil)
        date-time-string (get proposal rule)]
    (if (nil? date-time-string)
      "" (datetime/time-as-duration date-time-string))))

(defn to-proposal [proposal]
  {:id                   (get-in proposal [:proposal_id])
   :title                (get-in proposal [:content :title])
   :description          (get-in proposal [:content :description])
   :voting-start-time    (get-in proposal [:voting_start_time])
   :voting-end-time      (get-in proposal [:voting_end_time])
   :deposit_end_time     (get-in proposal [:deposit_end_time])
   :status               (proposal-status proposal)
   :tally-ratio          (get-in proposal [:tally-ratio])
   :proposal-end-detail  (proposal-end-detail proposal)
   :days-left-for-voting (days-left-for-voting proposal)})

(defn to-proposal-detail [proposal]
  (prn "to-proposal-detail" proposal)

  {:id                   (get-in proposal [:proposal_id])
   :title                (get-in proposal [:content :title])
   :description          (get-in proposal [:content :description])
   :voting-start-time    (get-in proposal [:voting_start_time])
   :voting-end-time      (get-in proposal [:voting_end_time])
   :deposit_end_time     (get-in proposal [:deposit_end_time])
   :status               (proposal-status proposal)
   :tally-ratio          (get-in proposal [:tally-ratio])
   :tally          (get-in proposal [:tally])
   :voted               (get-in proposal [:voted])
   :turnout              (-> (get-in proposal [:turnout])
                             (js/parseFloat))
   :proposal-end-detail  (proposal-end-detail proposal)
   :days-left-for-voting (days-left-for-voting proposal)})

(comment (def proposal {:content            {:apptype           "/ibc.core.client.v1.ClientUpdateProposal",
                                             :title             "Replace expired IBC client for Osmosis",
                                             :description       "Replace client 07-tendermint-3 with client 07-tendermint-6",
                                             :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"},
                        :total_deposit      [{:denom "aplanq", :amount "600000000000000000000"}],
                        :deposit_end_time   "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED",
                        :submit_time        "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z",
                        :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"},
                        :proposal_id        "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"})

         (days-left-for-voting proposal)                           ; should be empty string

         (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_DEPOSIT_PERIOD" :deposit_end_time "2023-03-17T16:51:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
         (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_DEPOSIT_PERIOD" :deposit_end_time "2023-03-06T22:51:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
         (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_DEPOSIT_PERIOD" :deposit_end_time "2023-03-06T10:21:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
         (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_VOTING_PERIOD" :deposit_end_time "2023-03-17T16:51:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
         )