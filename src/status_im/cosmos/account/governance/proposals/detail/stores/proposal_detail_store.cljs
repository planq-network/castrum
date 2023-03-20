(ns status-im.cosmos.account.governance.proposals.detail.stores.proposal-detail-store
  (:require
   [oops.core :refer [gget ocall ocall+ oget oget+]]
   [re-frame.core :as re-frame]
   [re-frame.db :as re-frame.db]
   [status-im.cosmos.account.governance.proposals.common.queries :as queries]
   [status-im.cosmos.common.formatters :as formatters]
   [status-im.utils.fx :as fx]
   ["@keplr-wallet/stores" :refer (ChainStore CosmosQueries QueriesStore CosmwasmQueries ObservableQueryProposal)]
   ["@keplr-wallet/unit" :refer (CoinPretty, Dec, DecUtils, Int, IntPretty)]))

(comment

  (-> (formatters/assoc-initial-response-in @re-frame.db/app-db
                                            [:keplr-store :governance-proposal-details (keyword "6")]
                                            [:keplr-store/update-proposal-details-for-id "6"])
      (get-in [:keplr-store :governance-proposal-details (keyword "6")])
      (:query))  ;=> [:keplr-store/update-proposal-details-for-id "6"]

  (-> (formatters/assoc-initial-response-in @re-frame.db/app-db
                                            [:keplr-store :governance-proposal-details (keyword "6")])
      (get-in [:keplr-store :governance-proposal-details (keyword "6")])
      (:query))                                                     ;=> nil
)
(fx/defn update-proposal-details-for-id
  {:events [:keplr-store/update-proposal-details-for-id]}
  [{:keys [db]} id]
  (let [status (get-in db [:keplr-store :governance-proposal-details (keyword id) :status])]
    (when (or (nil? status) (= :error status))
      {:db       (formatters/assoc-initial-response-in db [:keplr-store :governance-proposal-details (keyword id)] [:keplr-store/update-proposal-details-for-id id])
       :dispatch [:keplr-store/fetch-vote-for-proposal id]})))

(re-frame/reg-sub
 :keplr-store/proposal-details-by-id
 :<- [:keplr-store]
 (fn [keplr-store [_ id]]
   (formatters/format-response-based-on-status keplr-store [:governance-proposal-details (keyword id)] queries/to-proposal-detail)))

(comment @(re-frame/subscribe [:keplr-store/proposal-details-by-id "6"]) (re-frame/dispatch [:keplr-store/update-proposal-details-for-id "6"])

         (re-frame/dispatch [:keplr-store/update-proposal-details-for-id "7"]) (get-in @re-frame.db/app-db [:keplr-store :governance-proposals 6 :voted]) (let [;current-address (ethereum/default-address @re-frame.db/app-db)
        ;bech32-address (get-in @re-frame.db/app-db [:wallet :accounts current-address :bech32-addr])
                                                                                                                                                                proposal-vote-query (queries/vote-details-query @re-frame.db/app-db "1")]

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
                                                                                                                                                            ))