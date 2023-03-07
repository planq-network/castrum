(ns status-im.cosmos.stores.proposals
  (:require
    [oops.core :refer [gget ocall ocall+ oget oget+]]
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    [status-im.utils.fx :as fx]
    [status-im.utils.datetime :as datetime]
    ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries)]))


(defn format-proposals [response]
  (-> response
      (js->clj :keywordize-keys true)
      (get-in [:response :data :proposals])))

(defn get-governance-observable-query [db]
  (-> (get-in db [:keplr-store :query-store])
      (ocall "get" (get-in db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (oget "queryGovernance")))


(fx/defn update-governance-proposals
         {:events [:keplr-store/updategovernanceproposals]}
         [{:keys [db]} response]
         {:db (assoc-in db [:keplr-store :governance-proposals] (format-proposals response))})

(fx/defn fetch-governance-proposals
         {:events [:keplr-store/fetchgovernanceproposals]}
         [{:keys [db]} _]
         {:keplr-observable/fetchquery {:query      (get-governance-observable-query db)
                                        :on-success [:keplr-store/updategovernanceproposals]
                                        :on-failure [:ui/show-error "Unable to fetch governance proposals"]}})


(defn proposal-status [proposal]
  ;replace ProposalStatus to ""

  (-> proposal
      (get-in [:status])
      (clojure.string/replace  #"PROPOSAL_STATUS_" "")
      )
 )

(defn get-proposal-end-time [proposal]
  (let [status (proposal-status proposal)]
    (if (= status "DEPOSIT_PERIOD")
      (get-in proposal [:deposit_end_time])
      (get-in proposal [:voting_end_time])
      )
    )
  )

(defn proposal-end-detail [proposal]
  (->> proposal
       (get-proposal-end-time)
       (datetime/parse-date-to-ms )
       (datetime/timestamp->long-date)
       (str "Voting ends: ")
       ))


(defn days-left-for-voting [proposal]
  (let [rules {:DEPOSIT_PERIOD :deposit_end_time
               :VOTING_PERIOD :voting_end_time}
        rule (get rules (keyword  (proposal-status proposal)) nil)
        date-time-string (get proposal rule)
        ]
    (if (nil? date-time-string)
      "" (datetime/time-as-duration date-time-string))))

(comment

  (def proposal {:content {:apptype "/ibc.core.client.v1.ClientUpdateProposal",
                           :title "Replace expired IBC client for Osmosis",
                           :description "Replace client 07-tendermint-3 with client 07-tendermint-6",
                           :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"},
                 :total_deposit [{:denom "aplanq", :amount "600000000000000000000"}],
                 :deposit_end_time "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED",
                 :submit_time "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z",
                 :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"},
                 :proposal_id "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"} )

  (days-left-for-voting proposal)                           ; should be empty string

  (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_DEPOSIT_PERIOD" :deposit_end_time "2023-03-17T16:51:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
  (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_DEPOSIT_PERIOD" :deposit_end_time "2023-03-06T22:51:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
  (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_DEPOSIT_PERIOD" :deposit_end_time "2023-03-06T10:21:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left
  (days-left-for-voting (assoc proposal :status "PROPOSAL_STATUS_VOTING_PERIOD" :deposit_end_time "2023-03-17T16:51:17.849865426Z" :voting_end_time "2023-03-27T16:51:17.849865426Z")) ; should be x days left


  )



(defn to-proposal [proposal]
  {:id         (get-in proposal [:proposal_id])
   :title             (get-in proposal [:content :title])
   :description            (get-in proposal [:content :description])
   :voting-start-time            (get-in proposal [:voting_start_time])
   :voting-end-time            (get-in proposal [:voting_end_time])
   :deposit_end_time            (get-in proposal [:deposit_end_time])
   :status           (proposal-status proposal)
   :tally-ratio         (get-in proposal [:final_tally_result])
   :proposal-end-detail (proposal-end-detail proposal)
   :days-left-for-voting (days-left-for-voting proposal)
   })


(comment

  (def proposal {:content {:apptype "/ibc.core.client.v1.ClientUpdateProposal",
                           :title "Replace expired IBC client for Osmosis",
                           :description "Replace client 07-tendermint-3 with client 07-tendermint-6",
                           :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"},
                 :total_deposit [{:denom "aplanq", :amount "600000000000000000000"}],
                 :deposit_end_time "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED",
                 :submit_time "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z",
                 :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"},
                 :proposal_id "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"} )


  (to-proposal proposal)


  )
(re-frame/reg-sub
  :keplr-store/proposals
  :<- [:keplr-store]
  (fn [keplr-store]
    (map to-proposal (:governance-proposals keplr-store))
    ))


;


(re-frame/reg-sub
  :keplr-store/proposal-by-id
  :<- [:keplr-store/proposals]
  (fn [proposals [_ id]]
    (->> proposals
         (filter #(= id (get-in % [:id]))) first)))



(comment

  (def assert-not-nil (fn [x] (assert (not= nil x) (str x " should not be nil"))))


  ;fetch proposals
  (re-frame/dispatch [:keplr-store/fetchgovernanceproposals]) ;wait for response
  (assert-not-nil (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]))
  ;[{:content {:@type "/ibc.core.client.v1.ClientUpdateProposal", :title "Replace expired IBC client for Osmosis", :description "Replace client 07-tendermint-3 with client 07-tendermint-6", :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"}, :total_deposit [{:denom "aplanq", :amount "600000000000000000000"}], :deposit_end_time "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z", :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"} {:content {:@type "/cosmos.params.v1beta1.ParameterChangeProposal", :title "Slashing Param Change", :description "We have noticed that some validators have been jailed for missing block sign. Current slashing SignedBlocksWindow parameter is 100, that means validator must sign 50% of last 100 blocks to avoid being jailed. As discussed on Discord, this is not a proper parameter so we proposed to adjust this parameter from 100 to 20000.", :changes [{:subspace "slashing", :key "SignedBlocksWindow", :value "\"20000\""}]}, :total_deposit [{:denom "aplanq", :amount "500000000000000000000"}], :deposit_end_time "2022-12-22T19:54:00.681421656Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2022-12-20T19:54:00.681421656Z", :voting_end_time "2022-12-22T19:54:00.681421656Z", :final_tally_result {:yes "93559218914954720572290", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "2", :voting_start_time "2022-12-20T19:54:00.681421656Z"} {:content {:@type "/ibc.core.client.v1.ClientUpdateProposal", :title "Replace expired IBC client for Osmosis", :description "Replace client 07-tendermint-3 with client 07-tendermint-10", :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-10"}, :total_deposit [{:denom "aplanq", :amount "500000000000000000000"}], :deposit_end_time "2023-01-14T14:42:18.352018338Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2023-01-12T14:42:18.352018338Z", :voting_end_time "2023-01-14T14:56:37.288954391Z", :final_tally_result {:yes "387965639707841102119509", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "3", :voting_start_time "2023-01-12T14:56:37.288954391Z"}]



  ;set-selected-chain-id should invoke the fetchvalidators and set the validators and also update the proposals and set selected chain id
  (re-frame/dispatch [:keplr-store/updategovernanceproposals (clj->js {:response {:data {:proposals []}}})]) ; should reset proposals
  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]) ; should be empty
  (re-frame/dispatch [:keplr-store/set-selected-chain-id "planq_7070-2"])
  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]) ; should not be empty


  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals])

  )