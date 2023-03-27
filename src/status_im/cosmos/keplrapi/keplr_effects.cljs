(ns status-im.cosmos.keplrapi.keplr-effects
  (:require
   [re-frame.core :as re-frame]
   [oops.core :refer [oget ocall]]))

(defn- dispatch-event [handler data]
  (when handler
    (re-frame/dispatch (conj handler data))))

; An effect to expand the keplr observableQuery
  ;TODO Only for DEV purposes
; To simulate error and success
(defn should-allow? [query]
  (let [url (.-_url query)]
    (and (not (nil? url))
         (not  (clojure.string/includes? url "###/v1beta1/validators")))))

(comment
  ;/cosmos/gov/v1beta1/proposals/5/tally
  (should-allow? #js {:_url "https://lcd.keplr.app/cosmos/gov/v1beta1/proposals/1"}) ;=> true
  (should-allow? #js {:_url "https://lcd.keplr.app/cosmos/gov/v1beta1/proposals/2"}) ; => false
)
(re-frame/reg-fx
 :keplr-observable/fetchquery
 (fn [{:keys [query on-failure on-success]}]

   (if (not (should-allow? query))
     (dispatch-event on-failure "sending mock error")
     (-> (.fetchResponse query (js/AbortController.))
         (.then (fn [response]
                  (js/console.log "Url" (.-_url query) "got response")
                  (dispatch-event on-success response)))
         (.catch (fn [err]
                   (dispatch-event on-failure err)
                   (js/console.error err)))))))


