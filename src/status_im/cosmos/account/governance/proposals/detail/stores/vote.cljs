(ns status-im.cosmos.account.governance.proposals.detail.stores.vote
  (:require
   [oops.core :refer [gget ocall ocall+ oget oget+]]
   [re-frame.core :as re-frame]
   [re-frame.db :as re-frame.db]
   [status-im.cosmos.account.governance.proposals.common.queries :refer [coin-decimals-for-current-stake-currency all-proposals-query proposal-by-id current-stake-currency]]
   [status-im.cosmos.utils.fetchers :refer [find-first]]
   [status-im.ethereum.core :as ethereum]
   [status-im.utils.fx :as fx]
   ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries ObservableQueryProposal)]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]
   [status-im.utils.money :as money]))

(defn format-vote-response [raw-vote-response]
  (-> raw-vote-response
      (js->clj :keywordize-keys true)
      (get-in [:response :data :vote :option] "Unspecified")))

(fx/defn update-vote-for-proposal
  {:events [:keplr-store/update-vote-for-proposal]}
  [{:keys [db]} id response]
  {:dispatch [:keplr-store/fetch-tally-for-proposal id (format-vote-response response)]})

(defn get-current-bech32-address [db]
  (let [current-address (ethereum/default-address db)
        bech32-address (get-in db [:wallet :accounts current-address :bech32-addr])]
    bech32-address))

(defn vote-details-query [db id]
  (-> (get-in db [:keplr-store :query-store])
      (ocall "get" (get-in db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (oget "queryProposalVote")
      (.getVote   id (get-current-bech32-address db))))

(fx/defn fetch-vote-for-proposal
  {:events [:keplr-store/fetch-vote-for-proposal]}
  [{:keys [db]} id]
  (prn "fetching vote for proposal")
  {:keplr-observable/fetchquery {:query      (vote-details-query  db id)
                                 :on-success [:keplr-store/update-vote-for-proposal id]
                                 :on-failure [:keplr-store/update-vote-for-proposal id]}})

(comment (re-frame/dispatch [:keplr-store/fetch-vote-for-proposal "7"]) (get-in @re-frame.db/app-db [:keplr-store :governance-proposals 6 :voted]) (let [;current-address (ethereum/default-address @re-frame.db/app-db)
        ;bech32-address (get-in @re-frame.db/app-db [:wallet :accounts current-address :bech32-addr])
                                                                                                                                                         proposal-vote-query (vote-details-query @re-frame.db/app-db "1")]

    ; (views/letsubs [all-accounts [:wallet/accounts]
    ;                  selected-type [:address-types/selected]
    ;                  showing-bottom-sheet? [:bottom-sheet/show?]]
    ;
    ;    (let [cue-atom     (reagent/atom false)
    ;          show-select-address-type      #(re-frame/dispatch [:bottom-sheet/show-sheet   {:content select-address-type-sheet}])
    ;          label       (if (= selected-type "Ethereum") :t/ethereum-address :t/bech32-address)
    ;          copied-text (if (= selected-type "Ethereum") (eip55/address->checksum address) (get-in all-accounts [address :bech32-addr]))]

    ;(.getVote  status-im.cosmos.account.governance.proposals.detail.stores.vote-query )
    ;(.getVote (vote-details-query @re-frame.db/app-db 1) "proposalId" "account.bech32Address")
    ;plq18jfhprfa9e40nvn6axfw8vpw36p502vuwa6afg

    ;0x3c93708D3d2e6af9b27AE992e3B02e8e8347a99c
    ;(prn bech32-address)
                                                                                                                                                     (prn proposal-vote-query)
                                                                                                                                                     (prn (oget proposal-vote-query "_url"))
                                                                                                                                                     (prn (js-keys proposal-vote-query))

    ; https://rest.planq.network/cosmos/gov/v1beta1/proposals/1/votes/plq18jfhprfa9e40nvn6axfw8vpw36p502vuwa6afg
)

  ;/cosmos/gov/v1beta1/proposals/1/votes/0x3c93708D3d2e6af9b27AE992e3B02e8e8347a99c


  ;proposal id 1
  ; const account = accountStore.getAccount(chainStore.current.chainId);
  ;  const queries = queriesStore.get(chainStore.current.chainId);
  ;
  ;  const proposal = queries.cosmos.queryGovernance.getProposal(proposalId);
         )