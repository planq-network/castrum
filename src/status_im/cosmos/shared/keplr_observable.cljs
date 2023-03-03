(ns status-im.cosmos.shared.keplr-observable
  (:require
   [re-frame.core :as re-frame]))

(defn- dispatch-event [handler data]
  (when handler
    (re-frame/dispatch (conj handler data))))

; An effect to expand the keplr observableQuery
(re-frame/reg-fx
 :keplr-observable/fetchquery
 (fn [{:keys [query on-failure on-success]}]
   (-> (.fetchResponse query (js/AbortController.))
       (.then (fn [response]
                (js/console.log "got response")
                (dispatch-event on-success response)))
       (.catch (fn [err]
                 (dispatch-event on-failure err)
                 (js/console.error err))))))

