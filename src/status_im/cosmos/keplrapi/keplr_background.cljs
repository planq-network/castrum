(ns status-im.cosmos.keplrapi.keplr-background
  (:require
    ["@keplr-wallet/router" :refer (Message)]
    [status-im.cosmos.utils.crypto-helper :refer [build-common-crypto]]
    [status-im.cosmos.keplrapi.messsaging.router :refer [build-rn-router]]
    [status-im.utils.config :refer [cosmos-config]]
    [status-im.cosmos.common.assertions :as assertions]
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    [oops.core :refer [oget ocall]]
    [status-im.cosmos.keplrapi.keplr-storage :refer [build-async-kv-instance]]
    ["@keplr-wallet/background" :refer ( init)]))


(defn build-message-requester []
  #js{:sendMessage (fn [^js port msg]
                     (js/Promise. (fn [resolve reject]
                                    (try
                                      ;to validate message or throw error
                                      (ocall msg "validateBasic")
                                      (prn "sendMessage" msg)
                                      ;do something with router as router knows how to handle the message
                                      ; the handler is available in background
                                      ;   (re-frame/dispatch-sync [:keplr-store/handle-keplr-event {:msg msg :resolve resolve :reject reject} ])
                                      (catch js/Error e
                                        (prn "thrown" e)
                                        ;return promise reject
                                        (reject e)
                                        )))))})
(defn build-message-requester-internal-to-ui []

  (build-message-requester)
  )
(defn build-message-requester-internal []

  (build-message-requester)
  )

(defn build-notification []
  ; Notification
  #js{:create (fn [params]

                (prn "create notification" params)
                )
      })
(defn build-experimental-options []
  ; Notification
  #js{:suggestChain {

                     :useMemoryKVStore true
                     }
      })

(defn build-ledger-options []
  ; Notification
  #js{:defaultMode      "ble"
      :transportIniters {
                         :ble (fn [deviceId]
                                (prn "create :ble" deviceId)
                                ; const lastDeviceId = await getLastUsedLedgerDeviceId();
                                ;
                                ;        if (!deviceId && !lastDeviceId) {
                                ;          throw new Error("Device id is empty");
                                ;        }
                                ;
                                ;        if (!deviceId) {
                                ;          deviceId = lastDeviceId;
                                ;        }
                                ;
                                ;        if (deviceId && deviceId !== lastDeviceId) {
                                ;          await setLastUsedLedgerDeviceId(deviceId);
                                ;        }
                                ;
                                ;        return await TransportBLE.open(deviceId);

                                )} })


(defn build-background-router []

  (let [router (build-rn-router)
        store-creator #(build-async-kv-instance %1)
        rn-message-requester-internal-to-ui (build-message-requester-internal-to-ui)
        embed-chain-infos (cosmos-config)
        privileged-origins #js[
                            "https://app.osmosis.zone",
                            "https://www.stargaze.zone",
                            "https://app.umee.cc",
                            "https://junoswap.com",
                            "https://frontier.osmosis.zone",
                            ]
        analytic-privileged-origins #js["https://wallet.keplr.app"]
        community-chain-info-repo #js{
                                   :organizationName "chainapsis",
                                   :repoName         "keplr-chain-registry",
                                   :branchName       "main",
                                   }
        common-crypto (build-common-crypto)
        notification (build-notification)
        ledger-options (build-ledger-options)
        experimental-options (build-experimental-options)]

    (do

      (init router
                               store-creator
                               rn-message-requester-internal-to-ui
                               embed-chain-infos
                               privileged-origins
                               analytic-privileged-origins
                               community-chain-info-repo
                               common-crypto
                               notification
                               ledger-options
                               experimental-options)
      (.listen router "background")
      router)))

(comment

  (let [input-router (build-bg-router)]

    (js-keys input-router)

    )
  (re-frame/dispatch [:keplr-store/init])





  (let [message-handler   (-> (get-in  @re-frame.db/app-db [:keplr-store :background-router ])
                      (js->clj :keywordize-keys true)
                      :parent-object
                              (oget "msgRegistry")
                              (oget "registeredMsgType")
                      (js/Array.from)
                      )
        ]

    (prn "handler" message-handler)



    )




  (let [route-handler   (-> (get-in  @re-frame.db/app-db [:keplr-store :background-router ])
                      (js->clj :keywordize-keys true)
                      :parent-object
                      (oget "registeredHandler")
                      (js/Array.from)
                      )
        ]

    (prn "handler" route-handler)



    )



  (-> (get-in  @re-frame.db/app-db [:keplr-store :background-router ])
      (js->clj :keywordize-keys true)
      :parent-object
      (ocall "handleMessage" "some-dummy" nil)

      ;  (assertions/print-map-data)
      )

  )