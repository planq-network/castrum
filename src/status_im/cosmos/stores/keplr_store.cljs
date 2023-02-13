(ns status-im.cosmos.stores.keplr-store
  (:require
    [cljs-bean.core :as clj-bean]
    [oops.core :refer [gget ocall ocall+ oget oget+]]
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    [status-im.cosmos.stores.async-kv-store :refer [build-async-kv-store]]
    [status-im.utils.config :refer [cosmos-config]]
    [status-im.utils.fx :as fx]
    ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries)]))

(defn build-query-store [chainStore]
  (QueriesStore.
    (build-async-kv-store "store_queries_fix3")
    chainStore,
    (.use CosmosQueries)
    (.use CosmwasmQueries)))
(defn init-with-chain-info [chain-infos]
  (prn "init-with-chain-info")
  (let [chainStore (ChainStore. (clj-bean/->js chain-infos))]
    {:chain-store          chainStore
     :query-store          (build-query-store chainStore)
     :chain-infos          chain-infos
     :selected-chain-id    (-> chain-infos first :chainId)
     :governance-proposals []}))
(fx/defn init-keplr-store
         {:events [:keplr-store/init]}
         [{:keys [db]} _]
         {:db       (assoc db :keplr-store (init-with-chain-info (cosmos-config)))
          :dispatch [:keplr-store/fetchgovernanceproposals]})

(fx/defn update-chain-store-with-chain-infos
         {:events [:keplr-store/update-chain-infos]}
         [{:keys [db]} update-cosmos-config]
         {:db (assoc db :keplr-store (init-with-chain-info update-cosmos-config))})

(fx/defn set-selected-chain-id
         {:events [:keplr-store/set-selected-chain-id]}
         [{:keys [db]} chain-id]
         {:db       (assoc-in db [:keplr-store :selected-chain-id] chain-id)
          :dispatch [:keplr-store/fetchgovernanceproposals]
          })
(defn format-proposals [response]
  (-> response
      (js->clj :keywordize-keys true)
      (get-in [:response :data :proposals])))
(fx/defn update-governance-proposals
         {:events [:keplr-store/updategovernanceproposals]}
         [{:keys [db]} response]
         {:db (assoc-in db [:keplr-store :governance-proposals] (format-proposals response))})
(defn get-governance-observable-query [db]
  (-> (get-in db [:keplr-store :query-store])
      (ocall "get" (get-in db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (oget "queryGovernance")))
(fx/defn fetch-governance-proposals
         {:events [:keplr-store/fetchgovernanceproposals]}
         [{:keys [db]} _]
         {:keplr-observable/fetchquery {:query      (get-governance-observable-query db)
                                        :on-success [:keplr-store/updategovernanceproposals]
                                        :on-failure [:ui/show-error "Unable to fetch governance proposals"]}})






(re-frame/reg-sub
  :keplr-store/proposals
  :<- [:keplr-store]
  (fn [keplr-store]
    (:governance-proposals keplr-store)))



(comment

  (re-frame/dispatch [:keplr-store/fetchgovernanceproposals])

  ;Test Initialize chain
  (re-frame/dispatch [:keplr-store/init])
  (assert (not= nil (:keplr-store @re-frame.db/app-db)) "chain-store should not be nil")

  ;Test update chain
  (re-frame/dispatch [:keplr-store/update-chain-infos (assoc-in (cosmos-config) [0 :features] ["test-feature"])])

  (assert (= ["test-feature"] (get-in @re-frame.db/app-db [:keplr-store :chain-infos 0 :features])) ":features should have [\"test-feature\"]")
  (prn (:keplr-store @re-frame.db/app-db))                  ; the features should have changed


  ;Test select chain by id
  (re-frame/dispatch [:keplr-store/set-selected-chain-id "chain-1"])
  (assert (= "chain-1" (get-in @re-frame.db/app-db [:keplr-store :selected-chain-id])) "selected chain id should be chain-1")
  ;(prn (get-in  @re-frame.db/app-db [:keplr-store :selected-chain-id ]) )


  ;const queries = queriesStore.get(chainStore.current.chainId);
  ; const proposals = queries.cosmos.queryGovernance.proposals;
  (-> (get-in @re-frame.db/app-db [:keplr-store :query-store])
      (ocall "get" (get-in @re-frame.db/app-db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (oget "queryGovernance")
      )


  (def res (-> (get-in @re-frame.db/app-db [:keplr-store :query-store])
               (ocall "get" (get-in @re-frame.db/app-db [:keplr-store :selected-chain-id]))
               (oget "cosmos")
               (oget "queryGovernance")
               ))



  (-> (get-governance-observable-query @re-frame.db/app-db)
      (.fetchResponse (js/AbortController.))
      (.then #(prn %1))
      )

  ;[{:content {:@type "/ibc.core.client.v1.ClientUpdateProposal", :title "Replace expired IBC client for Osmosis", :description "Replace client 07-tendermint-3 with client 07-tendermint-6", :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-6"}, :total_deposit [{:denom "aplanq", :amount "600000000000000000000"}], :deposit_end_time "2022-12-07T16:51:17.849865426Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2022-12-05T16:51:17.849865426Z", :voting_end_time "2022-12-07T16:51:17.849865426Z", :final_tally_result {:yes "63680000000000000000000", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "1", :voting_start_time "2022-12-05T16:51:17.849865426Z"} {:content {:@type "/cosmos.params.v1beta1.ParameterChangeProposal", :title "Slashing Param Change", :description "We have noticed that some validators have been jailed for missing block sign. Current slashing SignedBlocksWindow parameter is 100, that means validator must sign 50% of last 100 blocks to avoid being jailed. As discussed on Discord, this is not a proper parameter so we proposed to adjust this parameter from 100 to 20000.", :changes [{:subspace "slashing", :key "SignedBlocksWindow", :value "\"20000\""}]}, :total_deposit [{:denom "aplanq", :amount "500000000000000000000"}], :deposit_end_time "2022-12-22T19:54:00.681421656Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2022-12-20T19:54:00.681421656Z", :voting_end_time "2022-12-22T19:54:00.681421656Z", :final_tally_result {:yes "93559218914954720572290", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "2", :voting_start_time "2022-12-20T19:54:00.681421656Z"} {:content {:@type "/ibc.core.client.v1.ClientUpdateProposal", :title "Replace expired IBC client for Osmosis", :description "Replace client 07-tendermint-3 with client 07-tendermint-10", :subject_client_id "07-tendermint-3", :substitute_client_id "07-tendermint-10"}, :total_deposit [{:denom "aplanq", :amount "500000000000000000000"}], :deposit_end_time "2023-01-14T14:42:18.352018338Z", :status "PROPOSAL_STATUS_PASSED", :submit_time "2023-01-12T14:42:18.352018338Z", :voting_end_time "2023-01-14T14:56:37.288954391Z", :final_tally_result {:yes "387965639707841102119509", :abstain "0", :no "0", :no_with_veto "0"}, :proposal_id "3", :voting_start_time "2023-01-12T14:56:37.288954391Z"}]

  )



