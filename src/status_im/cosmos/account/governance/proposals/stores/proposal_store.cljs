(ns status-im.cosmos.account.governance.proposals.stores.proposal-store
  (:require
   [re-frame.core :as re-frame]
   [re-frame.db :as re-frame.db]
   [status-im.cosmos.account.governance.proposals.common.queries :as queries]
   [status-im.cosmos.common.formatters :as formatters]
   [status-im.utils.datetime :as datetime]
   [status-im.utils.fx :as fx]
   ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries ObservableQueryProposal)]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]))

(defn format-proposals [response]
  (-> response
      (js->clj :keywordize-keys true)
      (get-in [:response :data :proposals])))

;

(fx/defn update-governance-proposals
  {:events [:keplr-store/updategovernanceproposals]}
  [{:keys [db]} response]
  {:db       (formatters/assoc-success-response-in db [:keplr-store :governance-proposals] (format-proposals response))
   :dispatch [:keplr-store/fetch-pool-data]})

;

(fx/defn fetch-governance-proposals
  {:events [:keplr-store/fetchgovernanceproposals]}
  [{:keys [db]} _]
  {:db (formatters/assoc-initial-response-in db [:keplr-store :governance-proposals] [:keplr-store/fetchgovernanceproposals])
   :keplr-observable/fetchquery {:query      (queries/all-proposals-query db)
                                 :on-success [:keplr-store/updategovernanceproposals]
                                 :on-failure [:keplr-store/handle-cosmos-error [:keplr-store :governance-proposals]]}})

(comment

  (def proposal {:content            {:apptype           "/ibc.core.client.v1.ClientUpdateProposal",
                                      :title             "Replace expired IBC client for Osmosis",
                                      :description       "Replace client 07-tendermint-3 with client 07-tendermint-6",
                                      :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"},
                 :total_deposit      [{:denom "aplanq", :amount "600000000000000000000"}],
                 :deposit_end_time   "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED",
                 :submit_time        "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z",
                 :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"},
                 :proposal_id        "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"}) (to-proposal proposal))
(re-frame/reg-sub
 :keplr-store/proposals
 :<- [:keplr-store]
 (fn [keplr-store]
    ;(map queries/to-proposal (:governance-proposals keplr-store))
    ;governance-proposals
   (formatters/format-subscription-response-from
    (get-in keplr-store [:governance-proposals])
    #(map queries/to-proposal %1))))

;


;  (get-in @re-frame.db/app-db [:keplr-store :pool])


(comment

  @(re-frame/subscribe [:keplr-store/proposals]) @(re-frame/subscribe [:keplr-store/pool])

  (def assert-not-nil (fn [x] (assert (not= nil x) (str x " should not be nil"))));fetch proposals
  (re-frame/dispatch [:keplr-store/fetchgovernanceproposals]) ;wait for response
  (assert-not-nil (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]))
  ;[{:content {:@type "/ibc.core.client.v1.ClientUpdateProposal", :title "Replace expired IBC client for Osmosis", :description "Replace client 07-tendermint-3 with client 07-tendermint-6", :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"}, :total_deposit [{:denom "aplanq", :amount "600000000000000000000"}], :deposit_end_time "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z", :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"} {:content {:@type "/cosmos.params.v1beta1.ParameterChangeProposal", :title "Slashing Param Change", :description "We have noticed that some validators have been jailed for missing block sign. Current slashing SignedBlocksWindow parameter is 100, that means validator must sign 50% of last 100 blocks to avoid being jailed. As discussed on Discord, this is not a proper parameter so we proposed to adjust this parameter from 100 to 20000.", :changes [{:subspace "slashing", :key "SignedBlocksWindow", :value "\"20000\""}]}, :total_deposit [{:denom "aplanq", :amount "500000000000000000000"}], :deposit_end_time "2022-12-22T19:54:00.681421656Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2022-12-20T19:54:00.681421656Z", :voting_end_time "2022-12-22T19:54:00.681421656Z", :final_tally_result {:yes "93559218914954720572290", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "2", :voting_start_time "2022-12-20T19:54:00.681421656Z"} {:content {:@type "/ibc.core.client.v1.ClientUpdateProposal", :title "Replace expired IBC client for Osmosis", :description "Replace client 07-tendermint-3 with client 07-tendermint-10", :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-10"}, :total_deposit [{:denom "aplanq", :amount "500000000000000000000"}], :deposit_end_time "2023-01-14T14:42:18.352018338Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2023-01-12T14:42:18.352018338Z", :voting_end_time "2023-01-14T14:56:37.288954391Z", :final_tally_result {:yes "387965639707841102119509", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "3", :voting_start_time "2023-01-12T14:56:37.288954391Z"}]

  (re-frame/dispatch [:ui/show-error "Unable to fetch governance proposals"]) ;wait for response
;set-selected-chain-id should invoke the fetchvalidators and set the validators and also update the proposals and set selected chain id
  (re-frame/dispatch [:keplr-store/updategovernanceproposals (clj->js {:response {:data {:proposals []}}})]) ; should reset proposals
  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]) ; should be empty
  (re-frame/dispatch [:keplr-store/set-selected-chain-id "planq_7070-2"])
  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]) ; should not be empty


  (get-in @re-frame.db/app-db [:keplr-store :governance-proposals]))