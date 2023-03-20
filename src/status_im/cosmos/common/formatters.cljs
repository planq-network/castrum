(ns status-im.cosmos.common.formatters)
(defn format-response-based-on-status [m ks formatter-fn]
  (let [input (get-in  m ks {:status :loading :error nil :data nil})
        has-data? (not (nil? (:data input)))]
    (if has-data?
      (assoc input :data (formatter-fn (:data input)))
      input)))

(defn format-subscription-response-from [data & [formatter-fn]]
  (let [input (if (nil? data)  {:status :loading :error nil :data nil}  data)
        function-to-use (if (nil? formatter-fn) identity formatter-fn)
        data  (:data input)]
    (if (empty? data)
      input
      (assoc input :data (function-to-use (:data input))))))

(defn format-success-response [current-status formatted-data]
  (assoc current-status :status :loaded :error nil :data formatted-data))
(defn assoc-success-response-in [db ks formatted-response]
  (let [current-status (get-in db ks)]
    (assoc-in db ks (format-success-response current-status formatted-response))))

(defn format-error-response [current-status error]
  (assoc current-status :status :error :error error))

(defn assoc-error-response-in [db ks error]
  (let [current-status (get-in db ks)]
    (assoc-in db ks (format-error-response current-status error))))

(defn format-initial-data [query]
  {:status :loading
   :reload-query query
   :response nil
   :error nil})

(defn assoc-initial-response-in
  "
# Initialize the response for a query  {:status :loading\n   :query query\n   :response nil\n   :error nil})
It will take two parameters & one optional parameter for reload query
* db - The re-frame db
* ks - The key path where the entire response chain will be stored
* reload-query - what should be dispatched if there is an error
"
  [db ks & [reload-query]]
  (assoc-in db ks (format-initial-data reload-query)))
