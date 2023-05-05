(ns status-im.cosmos.keplrapi.messsaging.router(:require
                                                 ["@keplr-wallet/router" :refer (EnvProducer,Router)]
                                                 [status-im.cosmos.common.assertions :as assertions]
                                                 [status-im.cosmos.utils.creators :as creator]
                                                 ["expo-random" :as expo-random]
                                                 ["typedarray" :as typedarray]
                                                 [oops.core :refer [oget ocall]]
                                                 ))



(defn- unlisten
  [this]
  (aset this "port" "")
  ;(.removeListener event-emitter "message" #(on-message this %))

  )

(defn- listen
  [this port]
  (if (empty? port)
    (throw (js/Error. "Empty port"))
    (do
      (aset this "port" port)
      ;(.addListener event-emitter "message" #(on-message this %))

      )))



(defn handle-message [this message sender]
  (prn "handle-message" message sender)

  (->  (.handleMessage this message sender)
       (.then #(do (prn "then" %)
                   ;call sender.resolver({
                   ;        return: result,
                   ;      });

                   (.resolver sender {:return %1})

                   ))
       (.catch #(do
                  (prn "catch" %)
                  (.resolver sender {:error (.toString %1)})
                  )))


  )

(defn registerMessage [this message]

  (prn "registerMessage" message)
  (ocall this "registerMessage" message)
  )
(defn addHandler [this route-input handler]

  (ocall this "addHandler" route-input handler)
  )
(defn addGuard [this guard]

  (ocall this "addGuard" guard)
  )
( defn build-router [envProducer]
  (let [parent-object (Router. envProducer)
        updated-methods {   :listen #(listen  parent-object %1 )
                          :unlisten #(unlisten  parent-object )
                         :handleMessage #(handle-message parent-object %1 %2)
                         :registerMessage #(registerMessage parent-object %1)
                         :addHandler  #(addHandler parent-object %1 %2)
                         :addGuard  #(addGuard parent-object %1)
                         :parent-object parent-object
                         }]

    (creator/to-extended-object parent-object updated-methods)))


( defn build-env-producer []
  (fn [^js sender]
    (prn sender)
    {
     :isInternalMsg  (and (= (.id sender) "react-native")
                          (= (.url sender) "react-native://internal"))
     :requestInteraction (fn [^js raw ^js msg]

                           ;return a promise
                           (js/Promise. (fn [resolve reject]
                                          (try
                                            (prn "requestInteraction" raw msg)
                                            (resolve  "success")
                                            (catch js/Error e
                                              (prn "thrown" e)
                                              ;return promise reject
                                              (reject e))))))}))




(defn build-rn-router []
  (build-router (build-env-producer)))
(comment

  ;create router is 1
  ; next call init with other things


  (-> (build-rn-router)
      (.-registerMessassge)

     )

  (let [inp (-> (build-router (build-env-producer))
                ;   (clj->js)


                )]

    (prn (js-keys inp)  )

    ; (ocall inp "registerMessage" "hi")
    ;registerMessage
    ;addHandler
    ;addGuard


    )




  )