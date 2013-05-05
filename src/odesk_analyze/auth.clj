(ns odesk_analyze.auth
  (:use [odesk_analyze.utils :only [read-edn write-edn]])
  (:require odesk_analyze.api))

(defn +file-auth-storage [fname]
  (let [data (atom (or (read-edn fname) {}))]
    (reify odesk_analyze.api.KeyValueStorage
      (get-value [this key] (key @data))
      (set-value [this key value]
                 (
                  (swap! data assoc key value)
                  (write-edn fname @data))))))