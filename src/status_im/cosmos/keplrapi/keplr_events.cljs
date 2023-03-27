(ns status-im.cosmos.keplrapi.keplr-events  (:require
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
                                              ["@keplr-wallet/background" :refer (KeyRingService BIP44HDPath, KeyRingStatus ExportKeyRingData  init, ScryptParams)]

                                              ))



(defn handle-enable-access [db msg resolve reject]
  (let  [chain-ids (oget msg "chainIds" )]
    ;TODO verify Do we need to do anything after enable access been called ?? , I dont think so, as we are not storing any data
    (prn "handle-account-store-effect handle-enable-access chain-ids" chain-ids)
    (resolve)))

;should return
;
;    const key = await service.getKey(msg.chainId);
;                     const ethereumKeyFeatures = await this.chainsService.getChainEthereumKeyFeatures(
;      chainId
;    );
;
;    if (ethereumKeyFeatures.address || ethereumKeyFeatures.signing) {
;      // Check the comment on the method itself.
;      this.keyRing.throwErrorIfEthermintWithLedgerButNotSupported(chainId);
;    }
;
;    return this.keyRing.getKey(
;      chainId,
;      await this.chainsService.getChainCoinType(chainId),
;      ethereumKeyFeatures.address
;    );


;return {
;      name: service.getKeyStoreMeta("name"),
;      algo: "secp256k1",
;      pubKey: key.pubKey,
;      address: key.address,
;      bech32Address: new Bech32Address(key.address).toBech32(
;        (await service.chainsService.getChainInfo(msg.chainId)).bech32Config
;          .bech32PrefixAccAddr
;      ),
;      isNanoLedger: key.isNanoLedger,
;    };
(defn handle-get-key [db msg resolve reject]
  (let  [chain-id (oget msg "chainId" ) ]
    (prn "handle-account-store-effect handle-get-key chain-id" chain-id)
    (prn (get-in  db [:keplr-store :selected-chain-id]))
    (resolve (get-in  db [:keplr-store :selected-chain-id]))))


(comment

(handle-get-key @re-frame.db/app-db #js{:chainId "stargaze-1"} (fn [x] (prn "resolve" x)) (fn [x] (prn "reject" x)))

  )
(re-frame/reg-fx
  :keplr-observable/handle-keplr-effect
  (fn [{:keys [event db]} ]
    (let [{:keys [msg resolve reject]} event
          message-type (ocall msg "type" )]
      (cond
        (= message-type "enable-access")
        (handle-enable-access db msg resolve reject)
        (= message-type "get-key")
        (handle-get-key db msg resolve reject)))))


(fx/defn handle-account-store-event
         {:events [:keplr-store/handle-keplr-event]}
         [{:keys [db]} event ]
         {:keplr-observable/handle-keplr-effect {:event event :db db}})


(comment

  (defn build-enable-access-message [chainIds]
    #js{:chainIds chainIds
        :validateBasic    (fn []
                            (if (or (nil? chainIds) (empty? chainIds))
                              (throw (js/Error. "chain id not set"))))
        :route    (fn []
                    "permission")
        :type    (fn []
                   "enable-access")})


  ; create instance of EnableAccessMsg
  ; new EnableAccessMsg(chainIds)
  (re-frame/dispatch [:keplr-store/init-test])

  ; 07:54:36.969]  LOG      "port" "background"
  ;[Tue Mar 21 2023 07:54:36.970]  LOG      "msg" #object[EnableAccessMsg [object Object]]
  ;[Tue Mar 21 2023 07:54:36.971]  LOG      "msg" #js ["chainIds"]
  ;[Tue Mar 21 2023 07:54:36.972]  LOG      "msg type" #object[EnableAccessMsg]
  ;[Tue Mar 21 2023 07:54:36.972]  LOG      "type response" "enable-access"
  ;[Tue Mar 21 2023 07:54:36.973]  LOG      "route response" "permission"
  ;[Tue Mar 21 2023 07:54:36.973]  LOG      "port" "background"
  ;[Tue Mar 21 2023 07:54:36.974]  LOG      "msg" #object[GetKeyMsg [object Object]]
  ;[Tue Mar 21 2023 07:54:36.975]  LOG      "msg" #js ["chainId"]
  ;[Tue Mar 21 2023 07:54:36.976]  LOG      "msg type" #object[GetKeyMsg]
  ;[Tue Mar 21 2023 07:54:36.976]  LOG      "type response" "get-key"
  ;[Tue Mar 21 2023 07:54:36.976]  LOG      "route response" "keyring"


  ;(require '[status-im.cosmos.common.assertions :refer [expect-promise-to-be-resolved expect-promise-to-be-rejected assert-not-nil]])




  (assertions/expect-promise-to-be-resolved (.sendMessage
                                              (keplr-event-emitter nil)
                                              2000
                                              (clj->js (build-enable-access-message ["stargaze-1" "osmosis-1"]))));=> console should print Success

  (assertions/expect-promise-to-be-rejected (.sendMessage
                                              (keplr-event-emitter nil)
                                              2000
                                              nil))                 ;=> console should print Success





  )