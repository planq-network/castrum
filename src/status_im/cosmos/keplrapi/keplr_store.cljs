(ns status-im.cosmos.keplrapi.keplr-store
  (:require
   [cljs-bean.core :as clj-bean]
   [oops.core :refer [gget ocall ocall+ oget oget+]]
   [re-frame.core :as re-frame]
   [re-frame.db :as re-frame.db]
   [status-im.cosmos.keplrapi.keplr-storage :refer [build-async-kv-instance]]
   [status-im.utils.config :refer [cosmos-config]]
   [status-im.cosmos.common.assertions :as assertions]
   [status-im.utils.fx :as fx]
   ["eventemitter3" :as EventEmitter]
   ["@keplr-wallet/stores" :refer (ChainStore AccountStore CosmosQueries QueriesStore CosmwasmQueries CosmosAccount CosmwasmAccount SecretAccount)]
   ["@keplr-wallet/provider" :as KeplrProvider :refer (Keplr)]
   ["@keplr-wallet/router" :refer (Message)]

   ))





(defn build-message [^js type ^js data]
  (Message. type data))

(defn build-query-store [chainStore]
  (QueriesStore.
   (build-async-kv-instance "store_queries_fix3")
   chainStore,
   (.use CosmosQueries)
   (.use CosmwasmQueries)))

(def event-emitter (EventEmitter.))
(defn add-event-listener [^js type fn]
  (prn "add-event-listener" type)
  (.addListener event-emitter type fn))

(defn remove-event-listener [^js type fn]
  (prn "remove-event-listener" type)
  (.removeListener event-emitter type fn))

(defn event-listeners []
  #js{:addEventListener    add-event-listener
      :removeEventListener remove-event-listener})

;based on keplr internal code
(defn msg-opts-creator [chain-id]
  (cond
    (.startsWith chain-id "osmosis")
    {:send {:native {:gas 100000}}
     :undelegate {:gas 350000}
     :redelegate {:gas 550000}
     :withdraw-rewards {:gas 300000}}
    (.startsWith chain-id "stargaze-")
    {:send {:native {:gas 100000}}
     :withdraw-rewards {:gas 200000}}))


;TODO account-store is invalid , Need to findout a better way to get the account store
(defn keplr-event-emitter []
  #js{:sendMessage (fn [^js port msg]
                     (js/Promise. (fn [resolve reject]
                                    (try
                                      ;to validate message or throw error
                                      (ocall msg "validateBasic")
                                      (re-frame/dispatch-sync [:keplr-store/handle-keplr-event {:msg msg :resolve resolve :reject reject} ])
                                      (catch js/Error e
                                        (prn "thrown" e)
                                        ;return promise reject
                                        (reject e)
                                        )))))})

(defn build-account-store [chainStore queryStore]
  (AccountStore.
   (event-listeners)
   chainStore
   (fn [^js chain-id]
     #js{:suggest-chain false
         :auto-init true
         :getKeplr (fn []
                          ;have not seen get-keplr is invoked
                      (prn "get-keplr")
                      (js/console.log "get-keplr response")
                                   (Keplr. "" "core" (keplr-event-emitter)))})
   (.use CosmosAccount #js{:queries-store queryStore
                           :msg-opts-creator msg-opts-creator})
   (.use CosmwasmAccount #js {:queries-store queryStore})
   (.use SecretAccount #js {:queries-store queryStore})))

; an interface which has a sendMessage
(defn init-account-store [account-store chain-id]

  (let [promised   (-> account-store
                       (ocall "getAccount" chain-id)
                       (oget "cosmos")
                       (oget "base")
                       (ocall "init"))]
    (-> promised
        (.then (fn [response]
                 (js/console.log "init-account-store success" response )
                 ))
        (.catch (fn [err]
                  (js/console.error "init-account-store error" err))))

    ))
(defn build-and-init-account-store [ chainStore queryStore selected-chain-id]

  (let [account-store (build-account-store  chainStore queryStore)]
    (do
      (init-account-store account-store selected-chain-id)
      account-store)))

(defn init-with-chain-info [ chain-infos]
  (let [chainStore (ChainStore. (clj-bean/->js chain-infos))
        queryStore (build-query-store chainStore)
        selected-chain-id (-> chain-infos first :chainId)
        accountStore (build-and-init-account-store  chainStore queryStore selected-chain-id)
        ]
    {:chain-store                  chainStore
     :query-store                  queryStore
     :accountStore                 accountStore
     :chain-infos                  chain-infos
     :selected-chain-id            selected-chain-id
     :selected-validator-status    "Bonded"
     :available-validator-statuses ["Bonded" "Unbonded" "Unbonding" "Unspecified"]
     :governance-proposals         []}))

(defn send-delegate-message [db amount validator-address memo fee]
  (-> db
      :keplr-store
      :accountStore
      (ocall "getAccount" (get-in  db [:keplr-store :selected-chain-id]))
      (oget "cosmos")
      (.sendDelegateMsg amount validator-address memo (clj->js fee) #js{:preferNoSetMemo  true :preferNoSetFee  true}
                        {:onBroadcasted (fn [txHash]
                                          (prn "onBroadcasted" txHash))})))
(comment

  (re-frame/dispatch [:keplr-store/init])




  (let [account-store (get-in  @re-frame.db/app-db [:keplr-store :accountStore])
        chain-id (get-in  @re-frame.db/app-db [:keplr-store :selected-chain-id])]
    (-> account-store
        (ocall "getAccount" chain-id)
        (oget "cosmos")
        (oget "base")
        (ocall "init")

        ))

  (let [account-store (get-in  @re-frame.db/app-db [:keplr-store :accountStore])
        chain-id (get-in  @re-frame.db/app-db [:keplr-store :selected-chain-id])

        account-base (-> account-store
                         (ocall "getAccount" chain-id)
                         (oget "cosmos")
                         (oget "base"))]


    (prn (-> account-base      (oget "_walletStatus") ))
    (prn (-> account-base      (oget "_bech32Address") ))
    (prn (-> account-base      (oget "_rejectionReason") ))



    )






  ;throws Account Store is invalid
  (let [validator-address (:operator_address (first  (get-in  @re-frame.db/app-db [:keplr-store :validators :data])))
        amount "0.0008"
        memo "sending things to the validator"
        fee #js {:amount [{:denom "PLANQ"
                           :amount "0.0008"}]
                 :gas 100000}]



    (-> (send-delegate-message @re-frame.db/app-db  amount validator-address memo fee)
        (.then (fn [response]
                 (js/console.log response)))
        (.catch (fn [err]
                  (js/console.error err)))))


  )

(fx/defn init-keplr-store
  {:events [:keplr-store/init]}
  [{:keys [db]} _]
  {:db         (assoc db :keplr-store (init-with-chain-info  (cosmos-config)))
   :dispatch-n [[:keplr-store/fetchgovernanceproposals] [:keplr-store/fetchvalidators]]})



;

(fx/defn update-chain-store-with-chain-infos
  {:events [:keplr-store/update-chain-infos]}
  [{:keys [db]} update-cosmos-config]
  {:db (assoc db :keplr-store (init-with-chain-info  update-cosmos-config))})

(fx/defn set-selected-chain-id
  {:events [:keplr-store/set-selected-chain-id]}
  [{:keys [db]} chain-id]
  {:db         (assoc-in db [:keplr-store :selected-chain-id] chain-id)
   :dispatch-n [[:keplr-store/fetchgovernanceproposals] [:keplr-store/fetchvalidators]]})

(re-frame/reg-sub
 :keplr-store/selected-chain-id
 :<- [:keplr-store]
 (fn [keplr-store]
   (:selected-chain-id keplr-store)))

(re-frame/reg-sub
 :keplr-store/chain-infos
 :<- [:keplr-store]
 (fn [keplr-store]
   (:chain-infos keplr-store)))


(fx/defn init-keplr-store-for-test
         {:events [:keplr-store/init-test]}
         [{:keys [db]} _]
         {:db         (assoc db :keplr-store (init-with-chain-info  (cosmos-config)))
          })



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
  (assert (= "chain-1" (get-in @re-frame.db/app-db [:keplr-store :selected-chain-id])) "selected chain id should be chain-1") (let [selected-chain-id (re-frame/subscribe [:keplr-store/selected-chain-id])
                                                                                                                                    device-chain-id (re-frame/subscribe [:chain-id])
                                                                                                                                    current-network (re-frame/subscribe [:current-network])
                                                                                                                                    all-chains (re-frame/subscribe [:keplr-store/chain-infos])]
                                                                                                                                (prn "selected chain-id " @selected-chain-id)
                                                                                                                                (prn "device-chain-id " @device-chain-id)
                                                                                                                                (prn "current-network " @current-network)
                                                                                                                                (prn "all-chains "  @all-chains)))
