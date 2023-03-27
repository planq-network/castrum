(ns status-im.cosmos.common.assertions)



;(require '[status-im.cosmos.common.assertions :refer [expect-promise-to-be-resolved expect-promise-to-be-rejected assert-not-nil]])
(defn expect-promise-to-be-resolved [promise]
  (-> promise
      (.then (fn [response]
               (prn  "Success" response)
               ))
      (.catch (fn [err]
                (prn"Error in assertion" err))))
  )


(defn expect-promise-to-be-rejected [promise]
  (-> promise
      (.then (fn [response]
               (prn  "Error in assertion" response)
               ))
      (.catch (fn [err]
                (prn"Success" err))))
  )

(def assert-not-nil (fn [x] (assert (not= nil x) (str x " should not be nil"))))